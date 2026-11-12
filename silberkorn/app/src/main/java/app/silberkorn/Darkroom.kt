package app.silberkorn

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import app.silberkorn.core.Developed
import app.silberkorn.core.Developer
import app.silberkorn.core.FloatImage
import app.silberkorn.core.LinearSource
import app.silberkorn.core.Look
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Develops negatives. Heavy work is serialised through one mutex so that two
 * full-size pictures are never held in memory at once.
 */
object Darkroom {
    private const val PREVIEW_SIDE = 2000
    private const val EDIT_SIDE = 1300
    private const val MAX_EXPORT_PIXELS = 26_000_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Mutex()
    private lateinit var app: Context

    // The most recently used sensor data, so opening the editor right after a shot is instant.
    private var cachedId: String? = null
    private var cachedRaw: ShortArray? = null

    fun init(context: Context) {
        app = context.applicationContext
        // Pick up anything that was captured but not developed before the app was closed.
        Library.shots.value.filter { !it.hasPreview }.forEach { enqueue(it.id, export = false) }
    }

    private fun raw(shot: Shot): ShortArray {
        cachedRaw?.let { if (cachedId == shot.id) return it }
        cachedRaw = null
        val raw = Library.loadRaw(shot)
        cachedId = shot.id
        cachedRaw = raw
        return raw
    }

    /** Hands freshly captured sensor data to the cache so it is not read back from disk. */
    fun prime(id: String, raw: ShortArray) {
        cachedId = id
        cachedRaw = raw
    }

    private fun source(shot: Shot, maxSide: Int): LinearSource =
        if (shot.meta.raw) Library.bayer(shot, raw(shot), Library.binFor(shot.meta, maxSide))
        else Library.loadArgb(shot, maxSide)

    private fun Developed.toBitmap(): Bitmap =
        Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

    fun enqueue(id: String, export: Boolean) {
        Library.update(id) { it.copy(developing = true) }
        scope.launch {
            runCatching {
                lock.withLock { developPreview(id) }
                if (export) export(id)
            }.onFailure { it.printStackTrace() }
            Library.update(id) { it.copy(developing = false) }
        }
    }

    private fun developPreview(id: String) {
        var shot = Library.find(id) ?: return
        if (shot.meta.raw && !shot.hasPreview && shot.meta.baseEv == 0f) {
            val ev = Developer.meter(Library.bayer(shot, raw(shot), Library.binFor(shot.meta, 500)))
            Library.setBaseEv(id, ev)
            shot = Library.find(id) ?: return
        }
        val src = source(shot, PREVIEW_SIDE)
        val d = Developer.develop(src, shot.look, shot.meta.baseEv, id.hashCode(), shot.meta.orientation, shot.meta.mirror)
        val bmp = d.toBitmap()
        val tmp = java.io.File(Library.dir(id), "preview.tmp")
        tmp.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        bmp.recycle()
        tmp.renameTo(Library.previewFile(id))
        Library.update(id) { it.copy(version = it.version + 1) }
    }

    /** Full-resolution JPEG into the phone's gallery (Pictures/Silberkorn). */
    suspend fun export(id: String): Uri? = withContext(Dispatchers.Default) {
        lock.withLock {
            val shot = Library.find(id) ?: return@withLock null
            val m = shot.meta
            val bmp = if (m.raw) {
                val raw = raw(shot)
                val bin = if (m.width.toLong() * m.height > MAX_EXPORT_PIXELS) 2 else 1
                Developer.develop(
                    Library.bayer(shot, raw, bin), shot.look, m.baseEv, id.hashCode(), m.orientation, m.mirror,
                    halationSource = Library.bayer(shot, raw, Library.binFor(m, 500)),
                ).toBitmap()
            } else {
                Developer.develop(Library.loadArgb(shot, 6200), shot.look, m.baseEv, id.hashCode(), m.orientation, m.mirror)
                    .toBitmap()
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$id.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Silberkorn")
                put(MediaStore.Images.Media.DATE_TAKEN, m.time)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = app.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use {
                    bmp.compress(Bitmap.CompressFormat.JPEG, Settings.prefs.value.jpegQuality, it)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            bmp.recycle()
            uri
        }
    }

    /**
     * A small linear copy of one negative for the editor: demosaiced once,
     * then re-printed with a new look on every slider movement.
     */
    class Session(private val shot: Shot, private val image: FloatImage) {
        fun render(look: Look): Bitmap = Developer.develop(
            image, look, shot.meta.baseEv, shot.id.hashCode(), shot.meta.orientation, shot.meta.mirror,
        ).toBitmap()
    }

    suspend fun openSession(id: String): Session? = withContext(Dispatchers.Default) {
        lock.withLock {
            val shot = Library.find(id) ?: return@withLock null
            Session(shot, FloatImage.from(source(shot, EDIT_SIDE)))
        }
    }
}
