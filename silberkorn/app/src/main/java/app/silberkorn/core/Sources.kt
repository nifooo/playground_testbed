package app.silberkorn.core

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * A picture in scene-linear light with sRGB primaries, readable row by row.
 * A value of 0.18 is meant to be a mid grey; values above 1 are allowed.
 */
interface LinearSource {
    val width: Int
    val height: Int

    /** The highest value this source can deliver (sensor clipping point). */
    val clip: Float

    /** Readers are not thread safe; create one per worker. */
    fun reader(): RowReader
}

fun interface RowReader {
    /** Writes `width * 3` floats (r, g, b interleaved) for row [y] into [out]. */
    fun read(y: Int, out: FloatArray)
}

class FloatImage(
    override val width: Int,
    override val height: Int,
    val data: FloatArray,
    override val clip: Float,
) : LinearSource {
    override fun reader() = RowReader { y, out ->
        System.arraycopy(data, y * width * 3, out, 0, width * 3)
    }

    companion object {
        fun from(src: LinearSource): FloatImage {
            val w = src.width
            val h = src.height
            val data = FloatArray(w * h * 3)
            Parallel.rows(h) { from, to ->
                val reader = src.reader()
                val row = FloatArray(w * 3)
                for (y in from until to) {
                    reader.read(y, row)
                    System.arraycopy(row, 0, data, y * w * 3, w * 3)
                }
            }
            return FloatImage(w, h, data, src.clip)
        }
    }
}

/**
 * An already finished 8-bit sRGB picture (imported photo, or a camera without
 * RAW support). It is display referred, so highlights are stretched back out
 * to give the tone curve something to roll off.
 */
class ArgbSource(
    private val pixels: IntArray,
    private val fullWidth: Int,
    private val fullHeight: Int,
    private val step: Int = 1,
) : LinearSource {
    override val width = fullWidth / step
    override val height = fullHeight / step
    override val clip = LUT[255]

    override fun reader() = RowReader { y, out ->
        var p = y * step * fullWidth
        var o = 0
        for (x in 0 until width) {
            val c = pixels[p]
            out[o] = LUT[(c shr 16) and 0xff]
            out[o + 1] = LUT[(c shr 8) and 0xff]
            out[o + 2] = LUT[c and 0xff]
            o += 3
            p += step
        }
    }

    companion object {
        private val LUT = FloatArray(256) {
            val e = it / 255f
            val d = if (e <= 0.04045f) e / 12.92f else ((e + 0.055f) / 1.055f).pow(2.4f)
            d + 3f * d * d * d * d
        }
    }
}

/** Everything needed to turn the sensor's Bayer mosaic into linear RGB. */
class BayerInfo(
    val width: Int,
    val height: Int,
    /** Colour (0 = R, 1 = G, 2 = B) at mosaic positions (0,0) (1,0) (0,1) (1,1). */
    val cfa: IntArray,
    /** Black level for the same four positions. */
    val black: FloatArray,
    val white: Float,
    /** White balance gains for R, G, B. */
    val wb: FloatArray,
    /** Row-major 3x3: white balanced camera RGB to linear sRGB. */
    val camToSrgb: FloatArray,
    /** Lens shading gains, 4 per cell in R, G(even rows), G(odd rows), B order. */
    val shading: FloatArray? = null,
    val shadingCols: Int = 0,
    val shadingRows: Int = 0,
)

/**
 * Demosaics on the fly. With [bin] = 1 it runs Malvar-He-Cutler at full
 * resolution; with an even [bin] it averages bin x bin blocks, which is fast
 * and clean and used for previews.
 */
class BayerSource(
    private val raw: ShortArray,
    private val info: BayerInfo,
    private val bin: Int = 1,
) : LinearSource {
    init {
        require(bin == 1 || bin % 2 == 0)
    }

    private val rw = info.width and 1.inv()
    private val rh = info.height and 1.inv()
    override val width = rw / bin
    override val height = rh / bin
    override val clip = 1f

    private val gains: FloatArray
    private val scale = 1f / (info.white - info.black.average().toFloat())
    private val shadeIx: IntArray?
    private val shadeFx: FloatArray?

    init {
        val g = info.wb
        val lowest = min(g[0], min(g[1], g[2]))
        gains = floatArrayOf(g[0] / lowest, g[1] / lowest, g[2] / lowest)
        if (info.shading != null && info.shadingCols > 1 && info.shadingRows > 1) {
            shadeIx = IntArray(rw)
            shadeFx = FloatArray(rw)
            for (x in 0 until rw) {
                val fx = x * (info.shadingCols - 1f) / (rw - 1f)
                val ix = min(fx.toInt(), info.shadingCols - 2)
                shadeIx[x] = ix
                shadeFx[x] = fx - ix
            }
        } else {
            shadeIx = null
            shadeFx = null
        }
    }

    override fun reader(): RowReader = if (bin == 1) Mhc() else Binned()

    /**
     * Black-subtracted, shading-corrected, white-balanced row, clipped to 1 so
     * that blown highlights stay neutral. Written to dst[pad + x].
     */
    private fun normalize(y: Int, dst: FloatArray, pad: Int, shadeTmp: FloatArray?) {
        val rowPos = (y and 1) * 2
        val base = y * info.width
        val c0 = info.cfa[rowPos]
        val c1 = info.cfa[rowPos + 1]
        val b0 = info.black[rowPos]
        val b1 = info.black[rowPos + 1]
        val g0 = gains[c0] * scale
        val g1 = gains[c1] * scale
        if (shadeTmp != null && shadeIx != null && shadeFx != null) {
            val cols = info.shadingCols
            val map = info.shading!!
            val fy = y * (info.shadingRows - 1f) / (rh - 1f)
            val iy = min(fy.toInt(), info.shadingRows - 2)
            val ty = fy - iy
            val evenRow = (y and 1) == 0
            val ch0 = shadeChannel(c0, evenRow)
            val ch1 = shadeChannel(c1, evenRow)
            for (i in 0 until cols) {
                val a = (iy * cols + i) * 4
                val b = a + cols * 4
                shadeTmp[i] = map[a + ch0] + (map[b + ch0] - map[a + ch0]) * ty
                shadeTmp[cols + i] = map[a + ch1] + (map[b + ch1] - map[a + ch1]) * ty
            }
            var x = 0
            while (x < rw) {
                var ix = shadeIx[x]
                var s = shadeTmp[ix] + (shadeTmp[ix + 1] - shadeTmp[ix]) * shadeFx[x]
                var v = ((raw[base + x].toInt() and 0xffff) - b0) * g0 * s
                dst[pad + x] = if (v > 1f) 1f else v
                ix = cols + shadeIx[x + 1]
                s = shadeTmp[ix] + (shadeTmp[ix + 1] - shadeTmp[ix]) * shadeFx[x + 1]
                v = ((raw[base + x + 1].toInt() and 0xffff) - b1) * g1 * s
                dst[pad + x + 1] = if (v > 1f) 1f else v
                x += 2
            }
        } else {
            var x = 0
            while (x < rw) {
                var v = ((raw[base + x].toInt() and 0xffff) - b0) * g0
                dst[pad + x] = if (v > 1f) 1f else v
                v = ((raw[base + x + 1].toInt() and 0xffff) - b1) * g1
                dst[pad + x + 1] = if (v > 1f) 1f else v
                x += 2
            }
        }
    }

    private fun shadeChannel(color: Int, evenRow: Boolean) = when (color) {
        0 -> 0
        2 -> 3
        else -> if (evenRow) 1 else 2
    }

    private fun newShadeTmp() = if (shadeIx != null) FloatArray(info.shadingCols * 2) else null

    private inner class Binned : RowReader {
        private val line = FloatArray(rw)
        private val shadeTmp = newShadeTmp()
        private val sum = FloatArray(width * 3)
        private val m = info.camToSrgb

        override fun read(y: Int, out: FloatArray) {
            sum.fill(0f)
            for (dy in 0 until bin) {
                val yy = y * bin + dy
                normalize(yy, line, 0, shadeTmp)
                val rowPos = (yy and 1) * 2
                val c0 = info.cfa[rowPos]
                val c1 = info.cfa[rowPos + 1]
                var x = 0
                for (ox in 0 until width) {
                    val o = ox * 3
                    var a = 0f
                    var b = 0f
                    var i = 0
                    while (i < bin) {
                        a += line[x]
                        b += line[x + 1]
                        x += 2
                        i += 2
                    }
                    sum[o + c0] += a
                    sum[o + c1] += b
                }
            }
            // Per bin x bin block: bin²/4 red, bin²/2 green, bin²/4 blue samples.
            val nrb = 4f / (bin * bin)
            val ng = nrb * 0.5f
            for (ox in 0 until width) {
                val o = ox * 3
                val r = max(sum[o] * nrb, 0f)
                val g = max(sum[o + 1] * ng, 0f)
                val b = max(sum[o + 2] * nrb, 0f)
                out[o] = m[0] * r + m[1] * g + m[2] * b
                out[o + 1] = m[3] * r + m[4] * g + m[5] * b
                out[o + 2] = m[6] * r + m[7] * g + m[8] * b
            }
        }
    }

    private inner class Mhc : RowReader {
        private val pad = 2
        private val rows = Array(5) { FloatArray(rw + 2 * pad) }
        private val rowIndex = IntArray(5) { -1 }
        private val shadeTmp = newShadeTmp()
        private val m = info.camToSrgb

        private fun row(y: Int): FloatArray {
            // Mirror around the edge without changing the mosaic phase.
            val yy = if (y < 0) -y else if (y >= rh) 2 * rh - 2 - y else y
            val slot = yy % 5
            val r = rows[slot]
            if (rowIndex[slot] != yy) {
                normalize(yy, r, pad, shadeTmp)
                r[1] = r[pad + 1]
                r[0] = r[pad + 2]
                r[pad + rw] = r[pad + rw - 2]
                r[pad + rw + 1] = r[pad + rw - 3]
                rowIndex[slot] = yy
            }
            return r
        }

        override fun read(y: Int, out: FloatArray) {
            val n2 = row(y - 2)
            val n1 = row(y - 1)
            val c = row(y)
            val s1 = row(y + 1)
            val s2 = row(y + 2)
            val rowPos = (y and 1) * 2
            for (x in 0 until rw) {
                val i = x + pad
                val color = info.cfa[rowPos + (x and 1)]
                val v = c[i]
                val cross = n1[i] + s1[i] + c[i - 1] + c[i + 1]
                val diag = n1[i - 1] + n1[i + 1] + s1[i - 1] + s1[i + 1]
                val far = n2[i] + s2[i] + c[i - 2] + c[i + 2]
                var r: Float
                var g: Float
                var b: Float
                if (color == 1) {
                    g = v
                    val horiz = (5f * v + 4f * (c[i - 1] + c[i + 1]) - diag -
                        (c[i - 2] + c[i + 2]) + 0.5f * (n2[i] + s2[i])) * 0.125f
                    val vert = (5f * v + 4f * (n1[i] + s1[i]) - diag -
                        (n2[i] + s2[i]) + 0.5f * (c[i - 2] + c[i + 2])) * 0.125f
                    // Which colour sits beside this green on its own row?
                    if (info.cfa[rowPos + ((x + 1) and 1)] == 0) {
                        r = horiz; b = vert
                    } else {
                        b = horiz; r = vert
                    }
                } else {
                    g = (4f * v + 2f * cross - far) * 0.125f
                    val other = (6f * v + 2f * diag - 1.5f * far) * 0.125f
                    if (color == 0) {
                        r = v; b = other
                    } else {
                        b = v; r = other
                    }
                }
                if (r < 0f) r = 0f
                if (g < 0f) g = 0f
                if (b < 0f) b = 0f
                if (r > 1f) r = 1f
                if (g > 1f) g = 1f
                if (b > 1f) b = 1f
                val o = x * 3
                out[o] = m[0] * r + m[1] * g + m[2] * b
                out[o + 1] = m[3] * r + m[4] * g + m[5] * b
                out[o + 2] = m[6] * r + m[7] * g + m[8] * b
            }
        }
    }
}
