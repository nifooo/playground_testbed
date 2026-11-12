package app.silberkorn.core

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** A finished 8-bit picture, pixels as 0xAARRGGBB. */
class Developed(val width: Int, val height: Int, val pixels: IntArray)

/**
 * The darkroom: takes scene-linear light and prints it with the Silberkorn
 * look. One fixed rendering (tone curve, colour, halation, grain) that the
 * [Look] parameters nudge but never replace.
 */
object Developer {
    private const val MID_GREY = 0.18f

    /**
     * Suggests an exposure offset (in stops) that puts the picture's average
     * brightness near mid grey. Measured once per negative and then kept, so
     * the exposure slider always starts from the same place.
     */
    fun meter(src: LinearSource): Float {
        val w = src.width
        val h = src.height
        val reader = src.reader()
        val row = FloatArray(w * 3)
        var sum = 0.0
        var weight = 0.0
        val stepY = max(1, h / 96)
        val stepX = max(1, w / 128)
        var y = stepY / 2
        while (y < h) {
            reader.read(y, row)
            val wy = 1.0 - 0.5 * sq(2.0 * y / h - 1.0)
            var x = stepX / 2
            while (x < w) {
                val o = x * 3
                val l = luma(row[o], row[o + 1], row[o + 2])
                val wt = wy * (1.0 - 0.5 * sq(2.0 * x / w - 1.0))
                sum += ln(max(l, 1e-4f).toDouble()) * wt
                weight += wt
                x += stepX
            }
            y += stepY
        }
        val mean = exp(sum / weight)
        // Scenes are rarely an exact mid grey on average; only go most of the way.
        val ev = (ln(0.16 / mean) / ln(2.0)) * 0.8
        return ev.toFloat().coerceIn(-2f, 4.5f)
    }

    /**
     * @param baseEv exposure from [meter]
     * @param seed makes the grain pattern unique per picture but stable across edits
     * @param orientation clockwise degrees (0, 90, 180, 270) to turn the sensor image upright
     * @param halationSource optional cheaper, smaller version of [src] for the glow
     */
    fun develop(
        src: LinearSource,
        look: Look,
        baseEv: Float,
        seed: Int,
        orientation: Int = 0,
        mirror: Boolean = false,
        halationSource: LinearSource = src,
    ): Developed {
        val turns = (((orientation / 90) + look.quarterTurns) % 4 + 4) % 4
        val sideways = turns % 2 == 1

        // Centre crop, expressed in sensor coordinates.
        val sw = src.width
        val sh = src.height
        var cw = sw
        var ch = sh
        val ratio = Aspect.ALL[look.aspect.coerceIn(0, Aspect.ALL.lastIndex)].ratio
        if (ratio > 0f) {
            if (sw >= sh) {
                if (sw.toFloat() / sh > ratio) cw = (sh * ratio).roundToInt() else ch = (sw / ratio).roundToInt()
            } else {
                if (sh.toFloat() / sw > ratio) ch = (sw * ratio).roundToInt() else cw = (sh / ratio).roundToInt()
            }
        }
        cw = cw.coerceIn(1, sw)
        ch = ch.coerceIn(1, sh)
        val cx0 = (sw - cw) / 2
        val cy0 = (sh - ch) / 2
        val outW = if (sideways) ch else cw
        val outH = if (sideways) cw else ch
        val out = IntArray(outW * outH)

        val gain = 2f.pow(baseEv + look.exposure)
        // White balance trims on top of the as-shot balance, plus the look's own warmth.
        val wr = gain * 1.045f * exp(0.30f * look.temperature + 0.10f * look.tint)
        val wg = gain * exp(-0.16f * look.tint)
        val wb = gain * 0.925f * exp(-0.30f * look.temperature + 0.10f * look.tint)

        val white = (src.clip * gain * 0.92f).coerceIn(1f, 12f)
        val curve = ToneCurve(1.38f * 2f.pow(look.contrast * 0.55f), white)
        val halo = if (look.halation > 0.001f) {
            Halation.build(halationSource, wr, wg, wb, min(white, 1.6f))
        } else null
        val haloAmount = look.halation * 1.15f
        val haloScaleX = if (halo != null) halo.width.toFloat() / sw else 0f
        val haloScaleY = if (halo != null) halo.height.toFloat() / sh else 0f

        val saturation = if (look.monochrome) 0f else 1.02f * (1f + look.saturation * 0.85f)
        val shadows = look.shadows
        val highlights = look.highlights

        // Grain is sized against the picture, not the pixel grid, so previews
        // and full-size exports show the same texture.
        val grainCell = (0.6f + look.grainSize * 2.6f) * max(outW, outH) / 4000f
        val grainAmp = look.grain * 0.085f * (if (grainCell < 1f) grainCell else 1f)
        val grain = Grain(seed, max(grainCell, 1f))

        Parallel.rows(ch) { from, to ->
            val reader = src.reader()
            val row = FloatArray(sw * 3)
            for (cy in from until to) {
                val sy = cy0 + cy
                reader.read(sy, row)
                for (cx in 0 until cw) {
                    val sx = cx0 + cx
                    val o = sx * 3
                    var r = max(row[o], 0f) * wr
                    var g = max(row[o + 1], 0f) * wg
                    var b = max(row[o + 2], 0f) * wb

                    var l = luma(r, g, b)

                    // Shadows / highlights: a brightness-dependent exposure change.
                    if (shadows != 0f || highlights != 0f) {
                        val t = l / (l + MID_GREY)
                        val k = 2f.pow(shadows * 1.3f * sq(1f - t) * (1f - t) + highlights * 1.1f * t * t * t)
                        r *= k; g *= k; b *= k; l *= k
                    }

                    // Halation: light that scattered back through the film base
                    // and exposed the red-sensitive layer a second time.
                    if (halo != null) {
                        val hv = halo.sample(sx * haloScaleX, sy * haloScaleY)
                        if (hv > 1e-4f) {
                            val a = hv * haloAmount / (1f + 1.5f * l * l)
                            r += a
                            g += a * 0.26f
                            b += a * 0.07f
                            l = luma(r, g, b)
                        }
                    }

                    if (look.monochrome) {
                        l = 0.30f * r + 0.56f * g + 0.14f * b
                        r = l; g = l; b = l
                    }

                    // Tone. Curving each channel on its own gives the print-like
                    // behaviour (colours bleach toward white as they get bright);
                    // mixing in a hue-preserving version keeps it from going too far.
                    val tl = curve.apply(l)
                    val s = if (l > 1e-6f) tl / l else 0f
                    r = mix(r * s, curve.apply(r), 0.62f)
                    g = mix(g * s, curve.apply(g), 0.62f)
                    b = mix(b * s, curve.apply(b), 0.62f)

                    // Colour: slightly restrained saturation, with saturated
                    // colours printing darker and denser rather than brighter.
                    var pl = luma(r, g, b)
                    r = pl + (r - pl) * saturation
                    g = pl + (g - pl) * saturation
                    b = pl + (b - pl) * saturation
                    val hi = max(r, max(g, b))
                    val lo = min(r, min(g, b))
                    val density = 1f - 0.13f * (hi - lo)
                    r *= density; g *= density; b *= density
                    pl *= density

                    // Split toning: golden highlights, faintly cool shadows.
                    val warm = pl * pl * (3f - 2f * pl)
                    val cool = sq(sq(1f - pl))
                    if (look.monochrome) {
                        r *= 1f + 0.012f * warm
                        b *= 1f - 0.03f * warm
                    } else {
                        g *= 1f - 0.012f * warm
                        b = b * (1f - 0.065f * warm) + 0.0008f * cool
                        r -= 0.0003f * cool
                    }

                    var er = encode(r)
                    var eg = encode(g)
                    var eb = encode(b)

                    val ox: Int
                    val oy: Int
                    when (turns) {
                        0 -> { ox = cx; oy = cy }
                        1 -> { ox = ch - 1 - cy; oy = cx }
                        2 -> { ox = cw - 1 - cx; oy = ch - 1 - cy }
                        else -> { ox = cy; oy = cw - 1 - cx }
                    }
                    val fx = if (mirror) outW - 1 - ox else ox

                    if (grainAmp > 0f) {
                        // Strongest in the mid tones, fading out toward paper white and black.
                        val e = 0.2126f * er + 0.7152f * eg + 0.0722f * eb
                        val a = grainAmp * (0.25f + 3f * e * (1f - e))
                        val n = grain.at(fx, oy, 0)
                        if (look.monochrome) {
                            er += a * n; eg += a * n; eb += a * n
                        } else {
                            er += a * (0.8f * n + 0.45f * grain.at(fx, oy, 1))
                            eg += a * (0.8f * n + 0.45f * grain.at(fx, oy, 2))
                            eb += a * (0.8f * n + 0.45f * grain.at(fx, oy, 3))
                        }
                    }

                    out[oy * outW + fx] = (0xff shl 24) or (to8(er) shl 16) or (to8(eg) shl 8) or to8(eb)
                }
            }
        }
        return Developed(outW, outH, out)
    }

    private fun to8(v: Float): Int {
        val i = (v * 255f + 0.5f).toInt()
        return if (i < 0) 0 else if (i > 255) 255 else i
    }

    private val ENCODE = FloatArray(4097) {
        val v = it / 4096f
        if (v <= 0.0031308f) v * 12.92f else 1.055f * v.pow(1f / 2.4f) - 0.055f
    }

    /** sRGB transfer curve. */
    private fun encode(v: Float): Float {
        if (v <= 0f) return 0f
        if (v >= 1f) return 1f
        val f = v * 4096f
        val i = f.toInt()
        return ENCODE[i] + (ENCODE[i + 1] - ENCODE[i]) * (f - i)
    }

    internal fun luma(r: Float, g: Float, b: Float) = 0.2126f * r + 0.7152f * g + 0.0722f * b
    private fun mix(a: Float, b: Float, t: Float) = a + (b - a) * t
    private fun sq(v: Float) = v * v
    private fun sq(v: Double) = v * v
}

/**
 * The print curve: v^c / (v^c + k), the S-shape of a photographic emulsion in
 * log exposure. Mid grey stays mid grey, [white] lands on paper white, and
 * everything between rolls off smoothly instead of clipping.
 */
internal class ToneCurve(contrast: Float, white: Float) {
    private val table = FloatArray(SIZE + 2)

    init {
        val target = 0.165
        val black = 0.0012
        val a = 0.18.pow(contrast.toDouble())
        val wc = white.toDouble().pow(contrast.toDouble())
        val k = max(a * wc * (1 - target) / (target * wc - a), 1e-6)
        val norm = (wc + k) / wc
        for (i in table.indices) {
            val t = i.toDouble() / SIZE
            // Inverse of the index mapping t = sqrt(v / (v + 1)).
            val t2 = min(t * t, 0.999999)
            val v = t2 / (1 - t2)
            val vc = v.pow(contrast.toDouble())
            val y = min(vc / (vc + k) * norm, 1.0)
            table[i] = (black + (1 - black) * y).toFloat()
        }
    }

    fun apply(v: Float): Float {
        if (v <= 0f) return table[0]
        val f = sqrt(v / (v + 1f)) * SIZE
        val i = f.toInt()
        return table[i] + (table[i + 1] - table[i]) * (f - i)
    }

    private companion object {
        const val SIZE = 4096
    }
}

/** A small blurred map of the brightest parts of the picture. */
internal class Halation(val width: Int, val height: Int, private val data: FloatArray) {
    fun sample(x: Float, y: Float): Float {
        val fx = (x - 0.5f).coerceIn(0f, width - 1.001f)
        val fy = (y - 0.5f).coerceIn(0f, height - 1.001f)
        val ix = fx.toInt()
        val iy = fy.toInt()
        val tx = fx - ix
        val ty = fy - iy
        val i = iy * width + ix
        val top = data[i] + (data[i + 1] - data[i]) * tx
        val bottom = data[i + width] + (data[i + width + 1] - data[i + width]) * tx
        return top + (bottom - top) * ty
    }

    companion object {
        private const val TARGET = 420

        fun build(src: LinearSource, wr: Float, wg: Float, wb: Float, ceiling: Float): Halation {
            val cell = max(1, max(src.width, src.height) / TARGET)
            val w = max(2, src.width / cell)
            val h = max(2, src.height / cell)
            val map = FloatArray(w * h)
            // Only light well above diffuse white bleeds visibly.
            val threshold = ceiling * 0.55f
            val step = max(1, cell / 3)
            Parallel.rows(h) { from, to ->
                val reader = src.reader()
                val row = FloatArray(src.width * 3)
                for (my in from until to) {
                    var rowsRead = 0
                    var dy = step / 2
                    while (dy < cell) {
                        reader.read(my * cell + dy, row)
                        rowsRead++
                        for (mx in 0 until w) {
                            var acc = 0f
                            var n = 0
                            var dx = step / 2
                            while (dx < cell) {
                                val o = (mx * cell + dx) * 3
                                val l = Developer.luma(
                                    max(row[o], 0f) * wr, max(row[o + 1], 0f) * wg, max(row[o + 2], 0f) * wb
                                )
                                acc += max(min(l, ceiling * 1.6f) - threshold, 0f)
                                n++
                                dx += step
                            }
                            map[my * w + mx] += acc / n
                        }
                        dy += step
                    }
                    for (mx in 0 until w) map[my * w + mx] /= rowsRead
                }
            }
            // A tight core plus a wide, faint skirt.
            val size = max(w, h)
            val core = blur(map, w, h, max(1, (size * 0.006f).roundToInt()))
            val skirt = blur(map, w, h, max(2, (size * 0.022f).roundToInt()))
            for (i in map.indices) map[i] = 0.6f * core[i] + 0.4f * skirt[i]
            return Halation(w, h, map)
        }

        /** Three box blurs, which is close enough to a Gaussian. */
        private fun blur(src: FloatArray, w: Int, h: Int, radius: Int): FloatArray {
            var a = src.copyOf()
            var b = FloatArray(src.size)
            repeat(3) {
                box(a, b, w, h, radius, horizontal = true)
                box(b, a, w, h, radius, horizontal = false)
            }
            return a
        }

        private fun box(src: FloatArray, dst: FloatArray, w: Int, h: Int, radius: Int, horizontal: Boolean) {
            val len = if (horizontal) w else h
            val lines = if (horizontal) h else w
            val stride = if (horizontal) 1 else w
            val norm = 1f / (2 * radius + 1)
            for (line in 0 until lines) {
                val start = if (horizontal) line * w else line
                var sum = 0f
                for (i in -radius..radius) sum += src[start + i.coerceIn(0, len - 1) * stride]
                for (i in 0 until len) {
                    dst[start + i * stride] = sum * norm
                    val add = (i + radius + 1).coerceAtMost(len - 1)
                    val drop = (i - radius).coerceAtLeast(0)
                    sum += src[start + add * stride] - src[start + drop * stride]
                }
            }
        }
    }
}

/**
 * Film grain as smoothed random clumps of [cell] pixels plus a finer layer on
 * top. Deterministic, so re-developing a picture gives the same grain.
 */
internal class Grain(private val seed: Int, private val cell: Float) {
    private val inv = 1f / cell
    private val fine = cell <= 1.01f

    fun at(x: Int, y: Int, channel: Int): Float {
        val s = seed + channel * 0x632be5ab
        if (fine) return noise(x, y, s)
        val fx = x * inv
        val fy = y * inv
        val ix = fx.toInt()
        val iy = fy.toInt()
        var tx = fx - ix
        var ty = fy - iy
        tx = tx * tx * (3f - 2f * tx)
        ty = ty * ty * (3f - 2f * ty)
        val a = noise(ix, iy, s)
        val b = noise(ix + 1, iy, s)
        val c = noise(ix, iy + 1, s)
        val d = noise(ix + 1, iy + 1, s)
        val top = a + (b - a) * tx
        val bottom = c + (d - c) * tx
        // Interpolation flattens the noise between lattice points; the 1.5 restores it.
        return (top + (bottom - top) * ty) * 1.5f * 0.75f + noise(x, y, s xor 0x5bd1e995) * 0.5f
    }

    /** Roughly bell-shaped noise with unit variance. */
    private fun noise(x: Int, y: Int, s: Int): Float {
        var h = x * 0x27d4eb2d + y * 0x165667b1 + s
        h = h xor (h ushr 15)
        h *= 0x2c1b3c6d
        h = h xor (h ushr 12)
        h *= 0x297a2d39
        h = h xor (h ushr 15)
        val u1 = (h and 0xffff) - 32767.5f
        val u2 = (h ushr 16) - 32767.5f
        return (u1 + u2) * (1f / 26754f)
    }
}
