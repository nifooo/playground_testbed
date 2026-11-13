package de.nifo.macroraw

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.DngCreator
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.OrientationEventListener
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowInsets
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Makro-Kamera: manueller Fokus bis an die Nahgrenze jeder Linse, Focus Peaking, Ausgabe als DNG.
 */
class MainActivity : Activity() {

    /** Eine waehlbare Linse. physId == null bedeutet: die logische (automatische) Kamera. */
    class Lens(
        val openId: String,
        val physId: String?,
        val chars: CameraCharacteristics,
        val logicalChars: CameraCharacteristics,
        val equivFocal: Float,
        val minFocusDiopters: Float,
        /** Bildbreite am Motiv an der Nahgrenze in mm (kleiner = staerkere Vergroesserung). */
        val fieldWidthMm: Float,
        var zoomForLens: Float = 1f,
        var label: String = ""
    )

    private val tag = "MacroRaw"
    private lateinit var cm: CameraManager
    private val lenses = ArrayList<Lens>()
    private var current: Lens? = null

    // Kamera-Thread-Zustand
    private var camThread: HandlerThread? = null
    private var camHandler: Handler? = null
    private var camExecutor: Executor? = null
    private val ioExecutor = Executors.newSingleThreadExecutor()
    private var device: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var previewSurface: Surface? = null
    private var rawReader: ImageReader? = null
    private var yuvReader: ImageReader? = null
    private var direct = false          // aktuelle Session nutzt physische Streams
    private var openGeneration = 0
    private var pendingImage: Image? = null
    private var pendingResult: TotalCaptureResult? = null
    @Volatile private var busy = false
    private var frameCount = 0

    // Einstellungen (vom UI-Thread gesetzt)
    @Volatile private var manualFocus = false
    @Volatile private var focusDiopters = 0f
    @Volatile private var maxDiopters = 10f
    @Volatile private var zoom = 1f
    @Volatile private var torch = false
    @Volatile private var peaking = true
    @Volatile private var wantDirect = false
    @Volatile private var afRegion: MeteringRectangle? = null
    @Volatile private var lastFocusDiopters = 0f
    @Volatile private var sharpness = 0
    @Volatile private var deviceOrientation = 0
    private var zoomMin = 1f
    private var zoomMax = 1f

    // Views
    private lateinit var texture: TextureView
    private lateinit var peakView: PeakingView
    private lateinit var status: TextView
    private lateinit var info: TextView
    private lateinit var chipRow: LinearLayout
    private lateinit var focusBar: SeekBar
    private lateinit var zoomBar: SeekBar
    private lateinit var zoomLabel: TextView
    private lateinit var afButton: Button
    private lateinit var shutter: Button
    private lateinit var directBox: CheckBox
    private lateinit var orientationListener: OrientationEventListener

    // ------------------------------------------------------------------ Lebenszyklus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cm = getSystemService(CameraManager::class.java)
        buildUi()
        orientationListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(o: Int) {
                if (o != ORIENTATION_UNKNOWN) deviceOrientation = ((o + 45) / 90 * 90) % 360
            }
        }
    }

    override fun onResume() {
        super.onResume()
        orientationListener.enable()
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            return
        }
        startCamera()
    }

    override fun onPause() {
        orientationListener.disable()
        stopCamera()
        super.onPause()
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, results: IntArray) {
        if (results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) {
            // onResume wird nach dem Dialog erneut aufgerufen und startet die Kamera.
        } else {
            status.text = "Ohne Kamera-Berechtigung geht es nicht."
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (event.repeatCount == 0) takePhoto()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun startCamera() {
        if (camThread != null) return
        val t = HandlerThread("camera").also { it.start() }
        camThread = t
        val h = Handler(t.looper)
        camHandler = h
        camExecutor = Executor { r -> h.post(r) }
        if (lenses.isEmpty()) {
            try {
                discoverLenses()
            } catch (e: Exception) {
                Log.e(tag, "discover", e)
                status.text = "Kameras konnten nicht gelesen werden: ${e.message}"
                return
            }
            buildChips()
        }
        if (lenses.isEmpty()) {
            status.text = "Keine RAW-faehige Rueckkamera gefunden."
            return
        }
        val lens = current ?: lenses.first()
        val keep = current != null
        current = lens
        if (texture.isAvailable) selectLens(lens, keepZoom = keep)
    }

    private fun stopCamera() {
        val t = camThread ?: return
        openGeneration++
        camHandler?.post { closeCameraOnThread() }
        t.quitSafely()
        try { t.join(2000) } catch (_: InterruptedException) {}
        camThread = null
        camHandler = null
        camExecutor = null
    }

    // ------------------------------------------------------------------ Linsen

    private fun hasCap(c: CameraCharacteristics, cap: Int) =
        c.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)?.contains(cap) == true

    private fun rawSize(c: CameraCharacteristics): Size? =
        c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(ImageFormat.RAW_SENSOR)
            ?.maxByOrNull { it.width.toLong() * it.height }

    private fun makeLens(openId: String, physId: String?, c: CameraCharacteristics, logical: CameraCharacteristics): Lens {
        val f = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.firstOrNull() ?: 4f
        val sensor = c.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val sw = sensor?.width ?: 6f
        val sh = sensor?.height ?: 4.5f
        val equiv = f * 43.27f / hypot(sw, sh)
        val minD = c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0f
        // Duenne Linse: Abbildungsmassstab m = f / (d - f); Bildbreite am Motiv = Sensorbreite / m
        val field = if (minD > 0f) {
            val dMm = 1000f / minD
            sw * (dMm - f).coerceAtLeast(f * 0.1f) / f
        } else Float.MAX_VALUE
        return Lens(openId, physId, c, logical, equiv, minD, field)
    }

    private fun discoverLenses() {
        val ids = cm.cameraIdList
        for (id in ids) {
            val c = cm.getCameraCharacteristics(id)
            if (c.get(CameraCharacteristics.LENS_FACING) != CameraCharacteristics.LENS_FACING_BACK) continue
            if (!hasCap(c, CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW) || rawSize(c) == null) continue
            val logical = hasCap(c, CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA)
            val auto = makeLens(id, null, c, c)
            auto.label = if (logical) "Auto" else "Kamera $id"
            lenses += auto
            if (!logical) continue
            val phys = ArrayList<Lens>()
            for (p in c.physicalCameraIds) {
                try {
                    val pc = cm.getCameraCharacteristics(p)
                    phys += makeLens(id, p, pc, c)
                } catch (e: Exception) {
                    Log.w(tag, "phys $p", e)
                }
            }
            // Hauptlinse = groesster Sensor; Zoomfaktor der anderen Linsen relativ dazu.
            val main = phys.maxByOrNull {
                val s = it.chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                (s?.width ?: 0f) * (s?.height ?: 0f)
            }
            val range = c.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
            val best = phys.minByOrNull { it.fieldWidthMm }
            for (l in phys.sortedBy { it.equivFocal }) {
                val ratio = if (main != null) l.equivFocal / main.equivFocal else 1f
                var z = if (l === main) 1f else if (ratio < 1f) ratio else ratio * 1.02f
                if (range != null) z = z.coerceIn(range.lower, range.upper)
                l.zoomForLens = z
                l.label = (if (l === best) "★ " else "") + "${l.equivFocal.roundToInt()} mm"
                lenses += l
            }
        }
    }

    private fun lensDetail(l: Lens): String {
        if (l.minFocusDiopters <= 0f) return "Fixfokus"
        val cm = 100f / l.minFocusDiopters
        return String.format(Locale.GERMAN, "Nahgrenze %.1f cm → kleinste Bildbreite ca. %.0f mm", cm, l.fieldWidthMm)
    }

    private fun buildChips() {
        chipRow.removeAllViews()
        for (l in lenses) {
            val b = Button(this).apply {
                text = l.label
                isAllCaps = false
                setOnClickListener { selectLens(l, keepZoom = false) }
            }
            b.tag = l
            chipRow.addView(b)
        }
    }

    private fun selectLens(l: Lens, keepZoom: Boolean) {
        current = l
        for (i in 0 until chipRow.childCount) {
            val v = chipRow.getChildAt(i) as Button
            v.setTextColor(if (v.tag === l) Color.parseColor("#FFC107") else Color.WHITE)
        }
        val useDirect = wantDirect && l.physId != null
        val range = l.logicalChars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
        zoomMin = if (useDirect) 1f else range?.lower ?: 1f
        zoomMax = (range?.upper ?: 1f).coerceAtMost(if (useDirect) 8f else 30f).coerceAtLeast(zoomMin)
        if (!keepZoom) zoom = if (useDirect) 1f else l.zoomForLens
        zoom = zoom.coerceIn(zoomMin, zoomMax)
        syncZoomBar()
        // Im Auto-Modus kann jede Linse aktiv werden: Regler bis zur kuerzesten Nahgrenze aller Linsen.
        maxDiopters = if (l.physId == null) {
            lenses.filter { it.openId == l.openId }.maxOf { it.minFocusDiopters }
        } else l.minFocusDiopters
        if (maxDiopters <= 0f) maxDiopters = 10f
        focusDiopters = focusDiopters.coerceIn(0f, maxDiopters)
        focusBar.progress = (focusDiopters / maxDiopters * focusBar.max).roundToInt()
        afRegion = null
        directBox.isEnabled = l.physId != null
        info.text = if (l.physId == null) {
            "Auto: Zoom-Regler wechselt die Linse. " + lenses.filter { it.physId != null && it.openId == l.openId }
                .joinToString(" · ") { String.format(Locale.GERMAN, "%s ab %.1f cm", it.label, if (it.minFocusDiopters > 0) 100f / it.minFocusDiopters else 0f) }
        } else "${l.label}: ${lensDetail(l)}" + if (useDirect) " · direkt" else ""
        val gen = ++openGeneration
        camHandler?.post { openOnThread(l, useDirect, 0, gen) }
    }

    // ------------------------------------------------------------------ Kamera oeffnen / Session

    private fun closeCameraOnThread() {
        try { session?.close() } catch (_: Exception) {}
        session = null
        try { device?.close() } catch (_: Exception) {}
        device = null
        try { rawReader?.close() } catch (_: Exception) {}
        rawReader = null
        try { yuvReader?.close() } catch (_: Exception) {}
        yuvReader = null
        previewSurface?.release()
        previewSurface = null
        try { pendingImage?.close() } catch (_: Exception) {}
        pendingImage = null
        pendingResult = null
        busy = false
    }

    private fun openOnThread(l: Lens, useDirect: Boolean, attempt: Int, gen: Int) {
        if (gen != openGeneration) return
        closeCameraOnThread()
        val ex = camExecutor ?: return
        try {
            cm.openCamera(l.openId, ex, object : CameraDevice.StateCallback() {
                override fun onOpened(d: CameraDevice) {
                    if (gen != openGeneration) { d.close(); return }
                    device = d
                    createSession(l, useDirect, attempt, gen)
                }
                override fun onDisconnected(d: CameraDevice) {
                    d.close()
                    if (device === d) device = null
                }
                override fun onError(d: CameraDevice, error: Int) {
                    d.close()
                    if (device === d) device = null
                    ui { status.text = "Kamerafehler $error" }
                }
            })
        } catch (e: SecurityException) {
            ui { status.text = "Keine Kamera-Berechtigung." }
        } catch (e: Exception) {
            Log.e(tag, "open", e)
            ui { status.text = "Kamera laesst sich nicht oeffnen: ${e.message}" }
        }
    }

    private fun pick43(sizes: Array<Size>?, maxWidth: Int): Size? {
        val list = sizes?.filter { abs(it.width * 3 - it.height * 4) <= 16 && it.width <= maxWidth }
        return list?.maxByOrNull { it.width } ?: sizes?.filter { it.width <= maxWidth }?.maxByOrNull { it.width }
    }

    /** attempt 0: Vorschau + Peaking + RAW, attempt 1: ohne Peaking. Danach Rueckfall auf nicht-direkt. */
    private fun createSession(l: Lens, useDirect: Boolean, attempt: Int, gen: Int) {
        val d = device ?: return
        val ex = camExecutor ?: return
        val h = camHandler ?: return
        val fail = { why: String ->
            Log.w(tag, "session failed (direct=$useDirect attempt=$attempt): $why")
            if (attempt == 0) openOnThread(l, useDirect, 1, gen)
            else if (useDirect) {
                ui {
                    toast("Direkter Zugriff auf diese Linse wird nicht unterstuetzt – nutze Zoom-Umschaltung.")
                    wantDirect = false
                    directBox.isChecked = false
                }
            } else ui { status.text = "Kamera-Session fehlgeschlagen: $why" }
        }
        try {
            val streamChars = if (useDirect) l.chars else l.logicalChars
            val map = streamChars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val pSize = pick43(map?.getOutputSizes(SurfaceTexture::class.java), 1920) ?: Size(1440, 1080)
            val rSize = rawSize(streamChars) ?: return fail("kein RAW")
            val st = texture.surfaceTexture ?: return fail("keine Vorschauflaeche")
            st.setDefaultBufferSize(pSize.width, pSize.height)
            val ps = Surface(st)
            previewSurface = ps
            val raw = ImageReader.newInstance(rSize.width, rSize.height, ImageFormat.RAW_SENSOR, 2)
            raw.setOnImageAvailableListener({ r ->
                try {
                    val img = r.acquireNextImage() ?: return@setOnImageAvailableListener
                    pendingImage?.close()
                    pendingImage = img
                    tryWrite()
                } catch (e: Exception) {
                    Log.w(tag, "raw image", e)
                }
            }, h)
            rawReader = raw
            val surfaces = arrayListOf(ps, raw.surface)
            if (attempt == 0) {
                val ySize = map?.getOutputSizes(ImageFormat.YUV_420_888)
                    ?.filter { abs(it.width * 3 - it.height * 4) <= 16 && it.width in 480..1024 }
                    ?.minByOrNull { it.width } ?: Size(640, 480)
                val yuv = ImageReader.newInstance(ySize.width, ySize.height, ImageFormat.YUV_420_888, 2)
                yuv.setOnImageAvailableListener({ r -> onYuv(r, l) }, h)
                yuvReader = yuv
                surfaces += yuv.surface
            }
            val outputs = surfaces.map { s ->
                OutputConfiguration(s).apply { if (useDirect) setPhysicalCameraId(l.physId) }
            }
            d.createCaptureSession(SessionConfiguration(SessionConfiguration.SESSION_REGULAR, outputs, ex,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(s: CameraCaptureSession) {
                        if (gen != openGeneration || device !== d) { s.close(); return }
                        session = s
                        direct = useDirect
                        if (attempt != 0) ui { peakView.clear() }
                        updateRepeatingOnThread()
                    }
                    override fun onConfigureFailed(s: CameraCaptureSession) {
                        if (gen == openGeneration) fail("Konfiguration abgelehnt")
                    }
                }))
        } catch (e: Exception) {
            Log.e(tag, "createSession", e)
            fail(e.message ?: e.javaClass.simpleName)
        }
    }

    // ------------------------------------------------------------------ Requests

    private fun newBuilder(template: Int): CaptureRequest.Builder {
        val d = device!!
        val l = current
        if (direct && l?.physId != null) {
            val keys = l.logicalChars.availablePhysicalCameraRequestKeys
            if (keys != null && keys.contains(CaptureRequest.LENS_FOCUS_DISTANCE)) {
                try {
                    return d.createCaptureRequest(template, setOf(l.physId))
                } catch (e: Exception) {
                    Log.w(tag, "physical request", e)
                }
            }
        }
        return d.createCaptureRequest(template)
    }

    private fun applySettings(b: CaptureRequest.Builder, afTrigger: Boolean = false) {
        val l = current
        b.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        b.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON)
        b.set(CaptureRequest.CONTROL_ZOOM_RATIO, zoom)
        b.set(CaptureRequest.FLASH_MODE,
            if (torch) CameraMetadata.FLASH_MODE_TORCH else CameraMetadata.FLASH_MODE_OFF)
        val region = afRegion
        val afMode = when {
            manualFocus -> CameraMetadata.CONTROL_AF_MODE_OFF
            region != null -> CameraMetadata.CONTROL_AF_MODE_AUTO
            else -> CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        }
        b.set(CaptureRequest.CONTROL_AF_MODE, afMode)
        if (manualFocus) b.set(CaptureRequest.LENS_FOCUS_DISTANCE, focusDiopters)
        if (region != null && !manualFocus) {
            b.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(region))
            b.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(region))
        }
        if (afTrigger) b.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
        // Bei direktem Linsenzugriff die Fokuswerte zusaetzlich gezielt an die physische Kamera geben.
        if (direct && l?.physId != null) {
            val keys = l.logicalChars.availablePhysicalCameraRequestKeys ?: return
            try {
                if (keys.contains(CaptureRequest.CONTROL_AF_MODE))
                    b.setPhysicalCameraKey(CaptureRequest.CONTROL_AF_MODE, afMode, l.physId)
                if (manualFocus && keys.contains(CaptureRequest.LENS_FOCUS_DISTANCE))
                    b.setPhysicalCameraKey(CaptureRequest.LENS_FOCUS_DISTANCE, focusDiopters, l.physId)
            } catch (e: Exception) {
                Log.w(tag, "physical keys", e)
            }
        }
    }

    private fun updateRepeating() {
        camHandler?.post { updateRepeatingOnThread() }
    }

    private fun updateRepeatingOnThread() {
        val s = session ?: return
        val ps = previewSurface ?: return
        try {
            val b = newBuilder(CameraDevice.TEMPLATE_PREVIEW)
            b.addTarget(ps)
            yuvReader?.let { if (peaking) b.addTarget(it.surface) }
            applySettings(b)
            s.setRepeatingRequest(b.build(), previewCallback, camHandler)
        } catch (e: Exception) {
            Log.e(tag, "repeating", e)
            ui { status.text = "Vorschau-Fehler: ${e.message}" }
        }
    }

    private fun triggerAfOnThread() {
        val s = session ?: return
        val ps = previewSurface ?: return
        try {
            val b = newBuilder(CameraDevice.TEMPLATE_PREVIEW)
            b.addTarget(ps)
            applySettings(b, afTrigger = true)
            s.capture(b.build(), null, camHandler)
        } catch (e: Exception) {
            Log.w(tag, "af trigger", e)
        }
    }

    private val previewCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(s: CameraCaptureSession, req: CaptureRequest, result: TotalCaptureResult) {
            val l = current
            val r: CaptureResult = if (direct && l?.physId != null)
                result.physicalCameraResults[l.physId] ?: result else result
            r.get(CaptureResult.LENS_FOCUS_DISTANCE)?.let { lastFocusDiopters = it }
            if (++frameCount % 6 != 0) return
            val active = result.get(CaptureResult.LOGICAL_MULTI_CAMERA_ACTIVE_PHYSICAL_ID)
            val iso = r.get(CaptureResult.SENSOR_SENSITIVITY)
            val exp = r.get(CaptureResult.SENSOR_EXPOSURE_TIME)
            val sb = StringBuilder()
            val fd = lastFocusDiopters
            sb.append(if (fd > 0f) String.format(Locale.GERMAN, "Fokus %.1f cm", 100f / fd) else "Fokus ∞")
            val activeLens = if (direct) l else lenses.firstOrNull { it.physId != null && it.physId == active }
            if (activeLens != null) sb.append(" · Linse ${activeLens.label.removePrefix("★ ")}")
            else if (active != null) sb.append(" · Linse #$active")
            if (iso != null) sb.append(" · ISO $iso")
            if (exp != null && exp > 0) sb.append(
                if (exp >= 250_000_000L) String.format(Locale.GERMAN, " · %.1f s", exp / 1e9)
                else " · 1/${(1e9 / exp).roundToInt()} s")
            if (peaking && yuvReader != null) sb.append(" · Schaerfe $sharpness")
            val text = sb.toString()
            ui { status.text = text }
        }
    }

    // ------------------------------------------------------------------ Focus Peaking

    private var yBuf = ByteArray(0)
    private var peakPixels = IntArray(0)

    private fun onYuv(reader: ImageReader, l: Lens) {
        val img = try { reader.acquireLatestImage() } catch (e: Exception) { null } ?: return
        try {
            if (!peaking) return
            val plane = img.planes[0]
            val stride = plane.rowStride
            val w = img.width
            val h = img.height
            val buf = plane.buffer
            val n = buf.remaining()
            if (yBuf.size < n) yBuf = ByteArray(n)
            buf.get(yBuf, 0, n)
            val ow = w / 2
            val oh = h / 2
            if (peakPixels.size != ow * oh) peakPixels = IntArray(ow * oh)
            val y = yBuf
            val out = peakPixels
            var sum = 0L
            var count = 0
            val threshold = 34
            for (oy in 1 until oh - 1) {
                val row = oy * 2 * stride
                var o = oy * ow + 1
                for (ox in 1 until ow - 1) {
                    val i = row + ox * 2
                    if (i + stride + 1 >= n) { out[o++] = 0; continue }
                    val gx = (y[i + 1].toInt() and 0xFF) - (y[i - 1].toInt() and 0xFF)
                    val gy = (y[i + stride].toInt() and 0xFF) - (y[i - stride].toInt() and 0xFF)
                    val g = abs(gx) + abs(gy)
                    out[o++] = if (g > threshold) 0xFF00FF40.toInt() else 0
                    sum += (g * g).toLong()
                    count++
                }
            }
            sharpness = if (count > 0) (sum / count).toInt() else 0
            val rot = l.logicalChars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
            peakView.update(out, ow, oh, rot)
        } catch (e: Exception) {
            Log.w(tag, "peaking", e)
        } finally {
            img.close()
        }
    }

    // ------------------------------------------------------------------ Aufnahme

    private fun takePhoto() {
        camHandler?.post {
            val s = session ?: return@post
            val raw = rawReader ?: return@post
            if (busy) return@post
            busy = true
            ui { shutter.isEnabled = false }
            try {
                val b = newBuilder(CameraDevice.TEMPLATE_STILL_CAPTURE)
                b.addTarget(raw.surface)
                previewSurface?.let { b.addTarget(it) }
                applySettings(b)
                b.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE,
                    CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_ON)
                s.capture(b.build(), object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(s: CameraCaptureSession, req: CaptureRequest, result: TotalCaptureResult) {
                        pendingResult = result
                        tryWrite()
                    }
                    override fun onCaptureFailed(s: CameraCaptureSession, req: CaptureRequest, failure: CaptureFailure) {
                        captureDone("Aufnahme fehlgeschlagen")
                    }
                }, camHandler)
            } catch (e: Exception) {
                Log.e(tag, "capture", e)
                captureDone("Aufnahme fehlgeschlagen: ${e.message}")
            }
        }
    }

    private fun captureDone(message: String) {
        busy = false
        ui {
            shutter.isEnabled = true
            toast(message)
        }
    }

    private fun exifOrientation(sensorOrientation: Int): Int =
        when ((sensorOrientation + deviceOrientation) % 360) {
            90 -> ExifInterface.ORIENTATION_ROTATE_90
            180 -> ExifInterface.ORIENTATION_ROTATE_180
            270 -> ExifInterface.ORIENTATION_ROTATE_270
            else -> ExifInterface.ORIENTATION_NORMAL
        }

    /** Laeuft auf dem Kamera-Thread, sobald Bild und Metadaten beide da sind. */
    private fun tryWrite() {
        val img = pendingImage ?: return
        val result = pendingResult ?: return
        pendingImage = null
        pendingResult = null
        val l = current
        val wasDirect = direct
        ioExecutor.execute {
            var message: String
            try {
                // Passende Sensor-Kenndaten waehlen: bei aktiver Nebenlinse deren Kalibrierung.
                val candidates = ArrayList<Pair<CameraCharacteristics, CaptureResult>>()
                if (l != null) {
                    if (wasDirect && l.physId != null) {
                        candidates += l.chars to (result.physicalCameraResults[l.physId] ?: result)
                    } else {
                        val active = result.get(CaptureResult.LOGICAL_MULTI_CAMERA_ACTIVE_PHYSICAL_ID)
                        val al = lenses.firstOrNull { it.physId != null && it.physId == active && it.openId == l.openId }
                        val px = al?.chars?.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                        if (al != null && px != null && px.width == img.width && px.height == img.height)
                            candidates += al.chars to result
                    }
                    candidates += l.logicalChars to result
                }
                val name = "MACRO_" + SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date()) + ".dng"
                var saved = false
                var lastError: Exception? = null
                for ((chars, res) in candidates) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, name)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/x-adobe-dng")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/MacroRaw")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: throw IllegalStateException("MediaStore lehnt Datei ab")
                    try {
                        DngCreator(chars, res).use { dng ->
                            dng.setOrientation(exifOrientation(chars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90))
                            contentResolver.openOutputStream(uri)!!.use { os -> dng.writeImage(os, img) }
                        }
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        contentResolver.update(uri, values, null, null)
                        saved = true
                        break
                    } catch (e: Exception) {
                        Log.w(tag, "dng write", e)
                        lastError = e
                        contentResolver.delete(uri, null, null)
                    }
                }
                message = if (saved) "Gespeichert: DCIM/MacroRaw/$name"
                else "DNG konnte nicht geschrieben werden: ${lastError?.message}"
            } catch (e: Exception) {
                Log.e(tag, "save", e)
                message = "Speichern fehlgeschlagen: ${e.message}"
            } finally {
                try { img.close() } catch (_: Exception) {}
            }
            captureDone(message)
        }
    }

    // ------------------------------------------------------------------ UI

    private fun ui(block: () -> Unit) = runOnUiThread(block)
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    private fun dp(v: Int) = (v * resources.displayMetrics.density).roundToInt()

    private fun syncZoomBar() {
        val t = if (zoomMax > zoomMin) Math.log((zoom / zoomMin).toDouble()) / Math.log((zoomMax / zoomMin).toDouble()) else 0.0
        zoomBar.progress = (t * zoomBar.max).roundToInt()
        zoomLabel.text = String.format(Locale.GERMAN, "%.1f×", zoom)
    }

    private fun setManual(on: Boolean, adoptAfPosition: Boolean = true) {
        if (on && !manualFocus && adoptAfPosition) {
            // Aktuelle AF-Position als Startwert uebernehmen.
            focusDiopters = lastFocusDiopters.coerceIn(0f, maxDiopters)
            focusBar.progress = (focusDiopters / maxDiopters * focusBar.max).roundToInt()
        }
        manualFocus = on
        afRegion = null
        afButton.text = if (on) "MF" else "AF"
        afButton.setTextColor(if (on) Color.parseColor("#FFC107") else Color.WHITE)
        updateRepeating()
    }

    private fun onPreviewTap(x: Float, y: Float) {
        if (manualFocus) return
        val l = current ?: return
        val arr: Rect = l.logicalChars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return
        val fx = (x / texture.width).coerceIn(0f, 1f)
        val fy = (y / texture.height).coerceIn(0f, 1f)
        val rot = l.logicalChars.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
        val (sx, sy) = when (rot) {
            90 -> fy to 1f - fx
            270 -> 1f - fy to fx
            180 -> 1f - fx to 1f - fy
            else -> fx to fy
        }
        val half = arr.width() / 16
        val cx = (sx * arr.width()).toInt().coerceIn(half, arr.width() - half)
        val cy = (sy * arr.height()).toInt().coerceIn(half, arr.height() - half)
        afRegion = MeteringRectangle(cx - half, cy - half, half * 2, half * 2, MeteringRectangle.METERING_WEIGHT_MAX)
        camHandler?.post {
            updateRepeatingOnThread()
            triggerAfOnThread()
        }
    }

    private fun label(s: String) = TextView(this).apply {
        text = s
        setTextColor(Color.LTGRAY)
        textSize = 13f
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }
        root.setOnApplyWindowInsetsListener { v, insets ->
            val b = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout())
            v.setPadding(b.left, b.top, b.right, b.bottom)
            insets
        }

        chipRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        root.addView(HorizontalScrollView(this).apply { addView(chipRow) },
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        info = label("").apply { setPadding(dp(10), 0, dp(10), dp(4)) }
        root.addView(info)

        val frame = AspectFrame(this)
        texture = TextureView(this)
        texture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(s: SurfaceTexture, w: Int, h: Int) {
                val l = current
                if (camThread != null && l != null) selectLens(l, keepZoom = true)
            }
            override fun onSurfaceTextureSizeChanged(s: SurfaceTexture, w: Int, h: Int) {}
            override fun onSurfaceTextureDestroyed(s: SurfaceTexture): Boolean {
                openGeneration++
                camHandler?.post { closeCameraOnThread() }
                return true
            }
            override fun onSurfaceTextureUpdated(s: SurfaceTexture) {}
        }
        peakView = PeakingView(this)
        status = TextView(this).apply {
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setBackgroundColor(0x99000000.toInt())
            setPadding(dp(8), dp(4), dp(8), dp(4))
        }
        frame.addView(texture, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        frame.addView(peakView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        frame.addView(status, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.BOTTOM))
        texture.setOnTouchListener { v, e ->
            if (e.action == MotionEvent.ACTION_UP) {
                v.performClick()
                onPreviewTap(e.x, e.y)
            }
            true
        }
        root.addView(frame, LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f).apply { gravity = Gravity.CENTER_HORIZONTAL })

        // Fokus
        val focusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(6), 0, dp(6), 0)
        }
        afButton = Button(this).apply {
            text = "AF"
            setOnClickListener { setManual(!manualFocus) }
        }
        focusBar = SeekBar(this).apply {
            max = 1000
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                    if (!fromUser) return
                    focusDiopters = p / 1000f * maxDiopters
                    if (!manualFocus) setManual(true, adoptAfPosition = false) else updateRepeating()
                }
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) {}
            })
        }
        val nearButton = Button(this).apply {
            text = "Nahgrenze"
            isAllCaps = false
            setOnClickListener {
                if (!manualFocus) setManual(true, adoptAfPosition = false)
                focusDiopters = maxDiopters
                focusBar.progress = focusBar.max
                updateRepeating()
            }
        }
        focusRow.addView(afButton, LinearLayout.LayoutParams(dp(64), WRAP_CONTENT))
        focusRow.addView(label("∞"))
        focusRow.addView(focusBar, LinearLayout.LayoutParams(0, dp(44), 1f))
        focusRow.addView(nearButton)
        root.addView(focusRow)

        // Zoom
        val zoomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
        }
        zoomLabel = label("1,0×").apply { minWidth = dp(44); gravity = Gravity.END }
        zoomBar = SeekBar(this).apply {
            max = 1000
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                    if (!fromUser) return
                    zoom = (zoomMin * (zoomMax / zoomMin).toDouble().pow(p / 1000.0).toFloat()).coerceIn(zoomMin, zoomMax)
                    zoomLabel.text = String.format(Locale.GERMAN, "%.1f×", zoom)
                    updateRepeating()
                }
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) {}
            })
        }
        zoomRow.addView(label("Zoom"))
        zoomRow.addView(zoomBar, LinearLayout.LayoutParams(0, dp(40), 1f))
        zoomRow.addView(zoomLabel)
        root.addView(zoomRow)

        // Schalter + Ausloeser
        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(6), 0, dp(6), dp(6))
        }
        val toggles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        fun box(text: String, checked: Boolean, onChange: (Boolean) -> Unit) = CheckBox(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            isChecked = checked
            setOnCheckedChangeListener { _, c -> onChange(c) }
        }
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(box("Peaking", true) {
            peaking = it
            if (!it) peakView.clear()
            updateRepeating()
        })
        row1.addView(box("Licht", false) {
            torch = it
            updateRepeating()
        })
        directBox = box("Linse erzwingen", false) {
            wantDirect = it
            current?.let { l -> if (l.physId != null) selectLens(l, keepZoom = false) }
        }
        toggles.addView(row1)
        toggles.addView(directBox)
        shutter = Button(this).apply {
            text = "RAW"
            textSize = 18f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#FFC107"))
            setOnClickListener { takePhoto() }
        }
        bottom.addView(toggles, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
        bottom.addView(shutter, LinearLayout.LayoutParams(dp(120), dp(64)))
        root.addView(bottom)

        setContentView(root)
    }
}
