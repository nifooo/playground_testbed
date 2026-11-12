package app.silberkorn

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.DngCreator
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.ColorSpaceTransform
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import app.silberkorn.core.Look
import java.nio.ByteOrder
import java.util.concurrent.Executor
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

data class Lens(
    val id: String,
    val front: Boolean,
    val label: String,
    val raw: Boolean,
    val previewSize: Size,
    val focalEq: Float,
    /** Exposure compensation range in steps, and the size of one step in EV. */
    val evRange: IntRange,
    val evStep: Float,
)

/**
 * Thin Camera2 wrapper: a viewfinder stream plus single RAW captures. All
 * camera work happens on its own thread; results arrive via the callbacks.
 */
class CameraController(
    context: Context,
    private val onShot: (Shot) -> Unit,
    private val onError: (String) -> Unit,
) {
    private val app = context.applicationContext
    private val manager = app.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val thread = HandlerThread("camera").apply { start() }
    private val handler = Handler(thread.looper)
    private val executor = Executor { handler.post(it) }

    private var device: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var reader: ImageReader? = null
    private var preview: Surface? = null
    private var lens: Lens? = null
    private var chars: CameraCharacteristics? = null
    private var evSteps = 0

    // The two halves of one capture, which arrive in either order.
    private var pendingImage: Image? = null
    private var pendingResult: TotalCaptureResult? = null
    private var pendingRotation = 0
    @Volatile
    var busy = false
        private set

    val lenses: List<Lens> by lazy { findLenses() }

    private fun findLenses(): List<Lens> {
        val found = manager.cameraIdList.mapNotNull { id ->
            runCatching {
                val c = manager.getCameraCharacteristics(id)
                val facing = c.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraMetadata.LENS_FACING_EXTERNAL) return@runCatching null
                val caps = c.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: intArrayOf()
                val map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: return@runCatching null
                val cfa = c.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT) ?: -1
                val raw = CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_RAW in caps &&
                    !map.getOutputSizes(ImageFormat.RAW_SENSOR).isNullOrEmpty() && cfa in 0..3
                val focal = c.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.firstOrNull() ?: 4f
                val sensor = c.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                val diagonal = if (sensor != null) sqrt(sensor.width * sensor.width + sensor.height * sensor.height) else 7f
                val range = c.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
                val step = c.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)?.toFloat() ?: 0f
                // The viewfinder shows the whole 4:3 sensor frame.
                val sizes = map.getOutputSizes(android.view.SurfaceHolder::class.java).orEmpty()
                val size = sizes.filter { abs(it.width * 3 - it.height * 4) <= 16 && it.width <= 1920 }
                    .maxByOrNull { it.width } ?: sizes.maxByOrNull { it.width * it.height } ?: Size(1280, 960)
                Lens(
                    id, facing == CameraMetadata.LENS_FACING_FRONT, "", raw, size, focal * 43.27f / diagonal,
                    if (range != null) range.lower..range.upper else 0..0, step,
                )
            }.getOrNull()
        }
        val main = found.firstOrNull { !it.front }?.focalEq ?: 26f
        return found.map { l ->
            val label = if (l.front) "Selfie" else {
                val x = l.focalEq / main
                if (abs(x - Math.round(x)) < 0.08f) "${Math.round(x)}×" else String.format("%.1f×", x).replace('.', ',')
            }
            l.copy(label = label)
        }.distinctBy { if (it.front) "f${it.label}" else it.label }
    }

    @SuppressLint("MissingPermission")
    fun open(lens: Lens, surface: Surface) = handler.post {
        closeNow()
        this.lens = lens
        preview = surface
        evSteps = 0
        try {
            val c = manager.getCameraCharacteristics(lens.id)
            chars = c
            val map = c.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            val format = if (lens.raw) ImageFormat.RAW_SENSOR else ImageFormat.JPEG
            val size = map.getOutputSizes(format).maxByOrNull { it.width.toLong() * it.height }!!
            reader = ImageReader.newInstance(size.width, size.height, format, 2).apply {
                setOnImageAvailableListener({ r ->
                    val image = r.acquireNextImage() ?: return@setOnImageAvailableListener
                    pendingImage?.close()
                    pendingImage = image
                    finishCapture()
                }, handler)
            }
            manager.openCamera(lens.id, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    device = camera
                    startSession(camera, surface)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    if (device === camera) device = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    if (device === camera) device = null
                    onError("Kamera konnte nicht geöffnet werden (Fehler $error)")
                }
            }, handler)
        } catch (e: Exception) {
            Log.e(TAG, "open", e)
            onError("Kamera konnte nicht geöffnet werden")
        }
    }

    private fun startSession(camera: CameraDevice, surface: Surface) {
        try {
            val outputs = listOf(OutputConfiguration(surface), OutputConfiguration(reader!!.surface))
            camera.createCaptureSession(
                SessionConfiguration(
                    SessionConfiguration.SESSION_REGULAR, outputs, executor,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(s: CameraCaptureSession) {
                            if (device !== camera) return
                            session = s
                            startPreview()
                        }

                        override fun onConfigureFailed(s: CameraCaptureSession) {
                            onError("Kamera-Sitzung konnte nicht gestartet werden")
                        }
                    },
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "session", e)
            onError("Kamera-Sitzung konnte nicht gestartet werden")
        }
    }

    private fun CaptureRequest.Builder.common() {
        set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON)
        set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, evSteps)
    }

    private fun startPreview() {
        val s = session ?: return
        val surface = preview ?: return
        try {
            val request = s.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
                common()
            }
            s.setRepeatingRequest(request.build(), null, handler)
        } catch (e: Exception) {
            Log.e(TAG, "preview", e)
        }
    }

    fun setExposureCompensation(steps: Int) = handler.post {
        evSteps = steps
        startPreview()
    }

    /** @param deviceRotation how far the phone is turned clockwise from upright: 0, 90, 180 or 270 */
    fun capture(deviceRotation: Int) = handler.post {
        val s = session ?: return@post
        val r = reader ?: return@post
        if (busy) return@post
        busy = true
        pendingRotation = deviceRotation
        pendingImage?.close()
        pendingImage = null
        pendingResult = null
        try {
            val request = s.device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(r.surface)
                common()
                set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CameraMetadata.STATISTICS_LENS_SHADING_MAP_MODE_ON)
                if (lens?.raw != true) {
                    set(CaptureRequest.JPEG_QUALITY, 98.toByte())
                    set(CaptureRequest.JPEG_ORIENTATION, uprightRotation())
                }
            }
            s.capture(request.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    pendingResult = result
                    finishCapture()
                }

                override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: android.hardware.camera2.CaptureFailure) {
                    busy = false
                    onError("Aufnahme fehlgeschlagen")
                }
            }, handler)
        } catch (e: Exception) {
            Log.e(TAG, "capture", e)
            busy = false
            onError("Aufnahme fehlgeschlagen")
        }
    }

    private fun uprightRotation(): Int {
        val sensor = chars?.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
        return if (lens?.front == true) (sensor - pendingRotation + 360) % 360 else (sensor + pendingRotation) % 360
    }

    private fun finishCapture() {
        val image = pendingImage ?: return
        val result = pendingResult ?: return
        pendingImage = null
        pendingResult = null
        try {
            val look = Settings.prefs.value.defaultLook
            val shot = if (image.format == ImageFormat.RAW_SENSOR) saveRaw(image, result, look) else saveJpeg(image, result, look)
            if (shot != null) onShot(shot) else onError("Aufnahme konnte nicht gespeichert werden")
        } catch (e: Throwable) {
            Log.e(TAG, "save", e)
            onError("Aufnahme konnte nicht gespeichert werden")
        } finally {
            image.close()
            busy = false
        }
    }

    private fun baseMeta(result: CaptureResult, raw: Boolean, w: Int, h: Int) = NegMeta(
        raw = raw, width = w, height = h,
        orientation = uprightRotation(), mirror = raw && lens?.front == true,
        iso = result.get(CaptureResult.SENSOR_SENSITIVITY) ?: 0,
        exposureNs = result.get(CaptureResult.SENSOR_EXPOSURE_TIME) ?: 0,
        aperture = result.get(CaptureResult.LENS_APERTURE) ?: 0f,
        focalEq = lens?.focalEq ?: 0f,
    )

    private fun saveJpeg(image: Image, result: TotalCaptureResult, look: Look): Shot? {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
        return Library.addJpeg(bytes, look, baseMeta(result, false, image.width, image.height))
    }

    private fun saveRaw(image: Image, result: TotalCaptureResult, look: Look): Shot {
        val c = chars!!
        val w = image.width
        val h = image.height
        val plane = image.planes[0]
        val shorts = plane.buffer.order(ByteOrder.nativeOrder()).asShortBuffer()
        val stride = plane.rowStride / 2
        val raw = ShortArray(w * h)
        for (y in 0 until h) {
            shorts.position(y * stride)
            shorts.get(raw, y * w, w)
        }

        val cfa = when (c.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT)) {
            CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GRBG -> intArrayOf(1, 0, 2, 1)
            CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_GBRG -> intArrayOf(1, 2, 0, 1)
            CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_BGGR -> intArrayOf(2, 1, 1, 0)
            else -> intArrayOf(0, 1, 1, 2)
        }
        val black = result.get(CaptureResult.SENSOR_DYNAMIC_BLACK_LEVEL)?.takeIf { it.size == 4 }
            ?: c.get(CameraCharacteristics.SENSOR_BLACK_LEVEL_PATTERN)?.let { p ->
                floatArrayOf(
                    p.getOffsetForIndex(0, 0).toFloat(), p.getOffsetForIndex(1, 0).toFloat(),
                    p.getOffsetForIndex(0, 1).toFloat(), p.getOffsetForIndex(1, 1).toFloat(),
                )
            } ?: FloatArray(4)
        val white = (result.get(CaptureResult.SENSOR_DYNAMIC_WHITE_LEVEL)
            ?: c.get(CameraCharacteristics.SENSOR_INFO_WHITE_LEVEL) ?: 1023).toFloat()

        val neutral = result.get(CaptureResult.SENSOR_NEUTRAL_COLOR_POINT)?.map { it.toFloat() }
            ?.takeIf { n -> n.size == 3 && n.all { it > 0.01f } }
        val wb = if (neutral != null) {
            floatArrayOf(1f / neutral[0], 1f / neutral[1], 1f / neutral[2])
        } else {
            result.get(CaptureResult.COLOR_CORRECTION_GAINS)?.let {
                floatArrayOf(it.red, (it.greenEven + it.greenOdd) / 2f, it.blue)
            } ?: floatArrayOf(2f, 1f, 1.6f)
        }

        val map = result.get(CaptureResult.STATISTICS_LENS_SHADING_CORRECTION_MAP)
        val shading = map?.let { m -> FloatArray(m.gainFactorCount).also { m.copyGainFactors(it, 0) } }

        val meta = baseMeta(result, true, w, h).copy(
            cfa = cfa, black = black, white = white, wb = wb,
            camToSrgb = cameraToSrgb(c, neutral),
            shading = shading, shadingCols = map?.columnCount ?: 0, shadingRows = map?.rowCount ?: 0,
        )
        val shot = Library.addRaw(raw, meta, look)
        Darkroom.prime(shot.id, raw)
        if (Settings.prefs.value.saveDng) runCatching { saveDng(c, result, image, shot.id, meta.orientation) }
            .onFailure { Log.e(TAG, "dng", it) }
        return shot
    }

    private fun saveDng(c: CameraCharacteristics, result: TotalCaptureResult, image: Image, name: String, orientation: Int) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.dng")
            put(MediaStore.Images.Media.MIME_TYPE, "image/x-adobe-dng")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Silberkorn")
        }
        val resolver = app.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return
        image.planes[0].buffer.rewind()
        resolver.openOutputStream(uri)?.use { out ->
            DngCreator(c, result).use { dng ->
                dng.setOrientation(
                    when (orientation) {
                        90 -> ExifInterface.ORIENTATION_ROTATE_90
                        180 -> ExifInterface.ORIENTATION_ROTATE_180
                        270 -> ExifInterface.ORIENTATION_ROTATE_270
                        else -> ExifInterface.ORIENTATION_NORMAL
                    }
                )
                dng.writeImage(out, image)
            }
        }
    }

    /**
     * Closes the camera. When [surface] is given, only if that is still the
     * surface in use: a viewfinder being torn down must not close the camera
     * its replacement has just opened.
     */
    fun close(surface: Surface? = null) = handler.post {
        if (surface == null || surface === preview) closeNow()
    }

    private fun closeNow() {
        runCatching { session?.close() }
        runCatching { device?.close() }
        runCatching { reader?.close() }
        pendingImage?.close()
        pendingImage = null
        pendingResult = null
        session = null
        device = null
        reader = null
        preview = null
        busy = false
    }

    fun release() {
        close()
        thread.quitSafely()
    }

    companion object {
        private const val TAG = "Silberkorn"

        // XYZ (D50) to linear sRGB (D65), Bradford adapted.
        private val XYZ_TO_SRGB = floatArrayOf(
            3.1338561f, -1.6168667f, -0.4906146f,
            -0.9787684f, 1.9161415f, 0.0334540f,
            0.0719453f, -0.2289914f, 1.4052427f,
        )

        private fun ColorSpaceTransform.toFloats(): FloatArray {
            val r = arrayOfNulls<Rational>(9)
            copyElements(r, 0)
            return FloatArray(9) { r[it]!!.toFloat() }
        }

        private fun illuminantXyz(code: Int?) = when (code) {
            CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_STANDARD_A, CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_TUNGSTEN ->
                floatArrayOf(1.0985f, 1f, 0.3558f)
            CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D50 -> floatArrayOf(0.9642f, 1f, 0.8251f)
            CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D55 -> floatArrayOf(0.9568f, 1f, 0.9214f)
            CameraMetadata.SENSOR_REFERENCE_ILLUMINANT1_D75 -> floatArrayOf(0.9497f, 1f, 1.2264f)
            else -> floatArrayOf(0.9504f, 1f, 1.0888f)
        }

        /** log(red / blue) of what the sensor records for a calibration light. */
        private fun warmth(colorMatrix: FloatArray, xyz: FloatArray): Float {
            val r = colorMatrix[0] * xyz[0] + colorMatrix[1] * xyz[1] + colorMatrix[2] * xyz[2]
            val b = colorMatrix[6] * xyz[0] + colorMatrix[7] * xyz[1] + colorMatrix[8] * xyz[2]
            return ln((r / b).coerceAtLeast(1e-3f))
        }

        /**
         * The sensor is calibrated under two lights (usually tungsten and
         * daylight). Blend the two calibrations by where this shot's white
         * point sits between them, then go XYZ -> sRGB.
         */
        fun cameraToSrgb(c: CameraCharacteristics, neutral: List<Float>?): FloatArray {
            val f1 = c.get(CameraCharacteristics.SENSOR_FORWARD_MATRIX1)?.toFloats()
            val f2 = c.get(CameraCharacteristics.SENSOR_FORWARD_MATRIX2)?.toFloats()
            val forward = when {
                f1 == null && f2 == null -> return floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
                f2 == null -> f1!!
                f1 == null -> f2
                else -> {
                    val c1 = c.get(CameraCharacteristics.SENSOR_COLOR_TRANSFORM1)?.toFloats()
                    val c2 = c.get(CameraCharacteristics.SENSOR_COLOR_TRANSFORM2)?.toFloats()
                    var t = 0.5f
                    if (c1 != null && c2 != null && neutral != null) {
                        val w1 = warmth(c1, illuminantXyz(c.get(CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1)))
                        val w2 = warmth(c2, illuminantXyz(c.get(CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT2)?.toInt()))
                        val w = ln((neutral[0] / neutral[2]).coerceAtLeast(1e-3f))
                        if (abs(w2 - w1) > 1e-3f) t = ((w - w1) / (w2 - w1)).coerceIn(0f, 1f)
                    }
                    FloatArray(9) { f1[it] + (f2[it] - f1[it]) * t }
                }
            }
            val m = FloatArray(9)
            for (row in 0..2) for (col in 0..2) {
                var sum = 0f
                for (k in 0..2) sum += XYZ_TO_SRGB[row * 3 + k] * forward[k * 3 + col]
                m[row * 3 + col] = sum
            }
            // White in must be white out, whatever rounding the calibration carries.
            for (row in 0..2) {
                val sum = m[row * 3] + m[row * 3 + 1] + m[row * 3 + 2]
                if (abs(sum) > 1e-3f) for (col in 0..2) m[row * 3 + col] /= sum
            }
            return m
        }
    }
}
