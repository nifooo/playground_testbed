package de.nifo.macroraw

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.view.View
import android.widget.FrameLayout

/** Container mit festem Seitenverhaeltnis 3:4 (Hochformat-Ansicht eines 4:3-Sensors). */
class AspectFrame(context: Context) : FrameLayout(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var w = MeasureSpec.getSize(widthMeasureSpec)
        var h = w * 4 / 3
        val maxH = MeasureSpec.getSize(heightMeasureSpec)
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED && h > maxH) {
            h = maxH
            w = h * 3 / 4
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
        )
    }
}

/** Zeichnet die Focus-Peaking-Maske (in Sensor-Ausrichtung) gedreht ueber die Vorschau. */
class PeakingView(context: Context) : View(context) {
    private val lock = Any()
    private var bitmap: Bitmap? = null
    private var rotation = 90
    private val matrix = Matrix()
    private val paint = Paint().apply { isFilterBitmap = false }

    fun update(pixels: IntArray, w: Int, h: Int, sensorRotation: Int) {
        synchronized(lock) {
            var b = bitmap
            if (b == null || b.width != w || b.height != h) {
                b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                bitmap = b
            }
            b.setPixels(pixels, 0, w, 0, 0, w, h)
            rotation = sensorRotation
        }
        postInvalidate()
    }

    fun clear() {
        synchronized(lock) { bitmap = null }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        synchronized(lock) {
            val b = bitmap ?: return
            val swap = rotation % 180 != 0
            val rw = if (swap) b.height else b.width
            val rh = if (swap) b.width else b.height
            matrix.reset()
            matrix.postTranslate(-b.width / 2f, -b.height / 2f)
            matrix.postRotate(rotation.toFloat())
            matrix.postScale(width.toFloat() / rw, height.toFloat() / rh)
            matrix.postTranslate(width / 2f, height / 2f)
            canvas.drawBitmap(b, matrix, paint)
        }
    }
}
