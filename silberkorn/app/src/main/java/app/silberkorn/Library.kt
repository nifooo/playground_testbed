package app.silberkorn

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import app.silberkorn.core.ArgbSource
import app.silberkorn.core.BayerInfo
import app.silberkorn.core.BayerSource
import app.silberkorn.core.LinearSource
import app.silberkorn.core.Look
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** What we know about a negative besides its pixels. */
data class NegMeta(
    val raw: Boolean,
    val width: Int,
    val height: Int,
    /** Clockwise degrees needed to show the stored pixels upright. */
    val orientation: Int = 0,
    val mirror: Boolean = false,
    val baseEv: Float = 0f,
    val time: Long = System.currentTimeMillis(),
    val iso: Int = 0,
    val exposureNs: Long = 0,
    val aperture: Float = 0f,
    val focalEq: Float = 0f,
    val cfa: IntArray = IntArray(4),
    val black: FloatArray = FloatArray(4),
    val white: Float = 1023f,
    val wb: FloatArray = floatArrayOf(1f, 1f, 1f),
    val camToSrgb: FloatArray = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
    val shading: FloatArray? = null,
    val shadingCols: Int = 0,
    val shadingRows: Int = 0,
) {
    fun bayerInfo() = BayerInfo(width, height, cfa, black, white, wb, camToSrgb, shading, shadingCols, shadingRows)

    fun toJson() = JSONObject().apply {
        put("raw", raw); put("width", width); put("height", height)
        put("orientation", orientation); put("mirror", mirror); put("baseEv", baseEv.toDouble())
        put("time", time); put("iso", iso); put("exposureNs", exposureNs)
        put("aperture", aperture.toDouble()); put("focalEq", focalEq.toDouble())
        put("cfa", JSONArray(cfa.toList())); put("black", floats(black)); put("white", white.toDouble())
        put("wb", floats(wb)); put("camToSrgb", floats(camToSrgb))
        if (shading != null) {
            put("shading", floats(shading)); put("shadingCols", shadingCols); put("shadingRows", shadingRows)
        }
    }

    companion object {
        private fun floats(a: FloatArray) = JSONArray().apply { a.forEach { put(it.toDouble()) } }
        private fun floats(a: JSONArray) = FloatArray(a.length()) { a.getDouble(it).toFloat() }

        fun fromJson(o: JSONObject) = NegMeta(
            raw = o.getBoolean("raw"), width = o.getInt("width"), height = o.getInt("height"),
            orientation = o.optInt("orientation"), mirror = o.optBoolean("mirror"),
            baseEv = o.optDouble("baseEv", 0.0).toFloat(), time = o.optLong("time"),
            iso = o.optInt("iso"), exposureNs = o.optLong("exposureNs"),
            aperture = o.optDouble("aperture", 0.0).toFloat(), focalEq = o.optDouble("focalEq", 0.0).toFloat(),
            cfa = o.optJSONArray("cfa")?.let { a -> IntArray(a.length()) { a.getInt(it) } } ?: IntArray(4),
            black = o.optJSONArray("black")?.let(::floats) ?: FloatArray(4),
            white = o.optDouble("white", 1023.0).toFloat(),
            wb = o.optJSONArray("wb")?.let(::floats) ?: floatArrayOf(1f, 1f, 1f),
            camToSrgb = o.optJSONArray("camToSrgb")?.let(::floats) ?: floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
            shading = o.optJSONArray("shading")?.let(::floats),
            shadingCols = o.optInt("shadingCols"), shadingRows = o.optInt("shadingRows"),
        )
    }
}

data class Shot(
    val id: String,
    val meta: NegMeta,
    val look: Look,
    /** Bumped whenever preview.jpg is rewritten. */
    val version: Int = 0,
    val developing: Boolean = false,
) {
    val hasPreview get() = version > 0
}

fun Look.toJson() = JSONObject().apply {
    put("exposure", exposure.toDouble()); put("contrast", contrast.toDouble())
    put("highlights", highlights.toDouble()); put("shadows", shadows.toDouble())
    put("temperature", temperature.toDouble()); put("tint", tint.toDouble())
    put("saturation", saturation.toDouble()); put("monochrome", monochrome)
    put("grain", grain.toDouble()); put("grainSize", grainSize.toDouble())
    put("halation", halation.toDouble()); put("aspect", aspect); put("quarterTurns", quarterTurns)
}

fun lookFromJson(o: JSONObject): Look {
    val d = Look()
    fun f(k: String, def: Float) = o.optDouble(k, def.toDouble()).toFloat()
    return Look(
        f("exposure", d.exposure), f("contrast", d.contrast), f("highlights", d.highlights),
        f("shadows", d.shadows), f("temperature", d.temperature), f("tint", d.tint),
        f("saturation", d.saturation), o.optBoolean("monochrome", d.monochrome),
        f("grain", d.grain), f("grainSize", d.grainSize), f("halation", d.halation),
        o.optInt("aspect", d.aspect), o.optInt("quarterTurns", d.quarterTurns),
    )
}

/**
 * The negatives on disk. Each shot is a folder holding the untouched sensor
 * data (or the imported picture), its metadata, the current look and a
 * developed preview. Nothing here is ever baked in.
 */
object Library {
    private lateinit var root: File
    private val state = MutableStateFlow<List<Shot>>(emptyList())
    val shots: StateFlow<List<Shot>> = state

    fun init(context: Context) {
        if (::root.isInitialized) return
        root = File(context.filesDir, "negatives").apply { mkdirs() }
        state.value = root.listFiles().orEmpty().filter { it.isDirectory }.mapNotNull { dir ->
            runCatching {
                val meta = NegMeta.fromJson(JSONObject(File(dir, "meta.json").readText()))
                val look = File(dir, "look.json").takeIf { it.exists() }
                    ?.let { lookFromJson(JSONObject(it.readText())) } ?: Look()
                Shot(dir.name, meta, look, if (File(dir, "preview.jpg").exists()) 1 else 0)
            }.getOrElse {
                // A folder without readable metadata is a capture that never finished writing.
                dir.deleteRecursively()
                null
            }
        }.sortedByDescending { it.meta.time }
    }

    fun dir(id: String) = File(root, id)
    fun previewFile(id: String) = File(dir(id), "preview.jpg")
    fun find(id: String) = state.value.firstOrNull { it.id == id }

    private fun newId(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        return "SK_$stamp"
    }

    /** Metadata is written last, so a half-written shot is never listed. */
    fun addRaw(raw: ShortArray, meta: NegMeta, look: Look): Shot {
        val id = newId()
        val dir = dir(id).apply { mkdirs() }
        RandomAccessFile(File(dir, "raw.bin"), "rw").use { f ->
            val map = f.channel.map(FileChannel.MapMode.READ_WRITE, 0, raw.size * 2L)
            map.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(raw)
            map.force()
        }
        return register(id, meta, look)
    }

    fun addJpeg(bytes: ByteArray, look: Look, base: NegMeta? = null): Shot? {
        val id = newId()
        val dir = dir(id).apply { mkdirs() }
        val file = File(dir, "source.jpg")
        file.writeBytes(bytes)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0) {
            dir.deleteRecursively()
            return null
        }
        val orientation = when (ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        val meta = (base ?: NegMeta(false, 0, 0)).copy(
            raw = false, width = bounds.outWidth, height = bounds.outHeight, orientation = orientation,
        )
        return register(id, meta, look)
    }

    fun importUri(context: Context, uri: Uri, look: Look): Shot? {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return addJpeg(bytes, look)
    }

    private fun register(id: String, meta: NegMeta, look: Look): Shot {
        File(dir(id), "look.json").writeText(look.toJson().toString())
        File(dir(id), "meta.json").writeText(meta.toJson().toString())
        val shot = Shot(id, meta, look)
        state.update { listOf(shot) + it }
        return shot
    }

    fun update(id: String, change: (Shot) -> Shot) {
        state.update { list -> list.map { if (it.id == id) change(it) else it } }
    }

    fun setLook(id: String, look: Look) {
        File(dir(id), "look.json").writeText(look.toJson().toString())
        update(id) { it.copy(look = look) }
    }

    fun setBaseEv(id: String, ev: Float) {
        val shot = find(id) ?: return
        val meta = shot.meta.copy(baseEv = ev)
        File(dir(id), "meta.json").writeText(meta.toJson().toString())
        update(id) { it.copy(meta = meta) }
    }

    fun delete(id: String) {
        state.update { list -> list.filterNot { it.id == id } }
        dir(id).deleteRecursively()
    }

    fun loadRaw(shot: Shot): ShortArray {
        val raw = ShortArray(shot.meta.width * shot.meta.height)
        RandomAccessFile(File(dir(shot.id), "raw.bin"), "r").use { f ->
            f.channel.map(FileChannel.MapMode.READ_ONLY, 0, raw.size * 2L)
                .order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(raw)
        }
        return raw
    }

    /** Decoded pixels of an imported picture, reduced so the long side is at most [maxSide]. */
    fun loadArgb(shot: Shot, maxSide: Int): LinearSource {
        val opts = BitmapFactory.Options()
        var sample = 1
        while (maxOf(shot.meta.width, shot.meta.height) / (sample * 2) >= maxSide) sample *= 2
        opts.inSampleSize = sample
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bmp = BitmapFactory.decodeFile(File(dir(shot.id), "source.jpg").path, opts)
            ?: error("Bild nicht lesbar")
        val px = IntArray(bmp.width * bmp.height)
        bmp.getPixels(px, 0, bmp.width, 0, 0, bmp.width, bmp.height)
        val w = bmp.width
        val h = bmp.height
        bmp.recycle()
        val step = maxOf(1, Math.ceil(maxOf(w, h) / maxSide.toDouble()).toInt())
        return ArgbSource(px, w, h, step)
    }

    /** Smallest even binning factor that brings the long side down to [maxSide], or 1. */
    fun binFor(meta: NegMeta, maxSide: Int): Int {
        val long = maxOf(meta.width, meta.height)
        if (long <= maxSide) return 1
        var bin = 2
        while (long / bin > maxSide) bin += 2
        return bin
    }

    fun bayer(shot: Shot, raw: ShortArray, bin: Int) = BayerSource(raw, shot.meta.bayerInfo(), bin)
}
