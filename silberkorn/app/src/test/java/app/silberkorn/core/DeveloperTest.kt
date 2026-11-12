package app.silberkorn.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class DeveloperTest {
    private val w = 1600
    private val h = 1200

    /** A dusk street scene in linear light: sky, sun, lamps, colour chart, grey ramp. */
    private fun scene(x: Int, y: Int, out: FloatArray) {
        val u = x.toFloat() / w
        val v = y.toFloat() / h
        var r: Float; var g: Float; var b: Float
        if (v < 0.55f) {
            val t = v / 0.55f
            r = 0.25f + 0.9f * t; g = 0.35f + 0.55f * t; b = 0.75f - 0.2f * t
            val d = hypot((u - 0.72f) * w / h, v - 0.33f)
            val sun = 60f * exp(-(d * d) / 0.0009f) + 1.2f * exp(-d / 0.08f)
            r += sun; g += sun * 0.9f; b += sun * 0.7f
        } else {
            val t = (v - 0.55f) / 0.45f
            r = 0.05f - 0.03f * t; g = 0.045f - 0.03f * t; b = 0.05f - 0.03f * t
        }
        // Building silhouette with lit windows.
        if (u < 0.35f && v > 0.2f && v < 0.8f) {
            r = 0.02f; g = 0.02f; b = 0.025f
            val wx = ((u * 40f) % 2f) < 1f && ((v * 30f) % 2f) < 1.2f
            if (wx && ((x / 40 + y / 40) % 3 == 0)) { r = 9f; g = 6.5f; b = 3f }
        }
        // Colour chart.
        if (v > 0.62f && v < 0.82f && u > 0.4f && u < 0.95f) {
            val i = ((u - 0.4f) / 0.55f * 8).toInt()
            val c = CHART[min(i, 7)]
            r = c[0]; g = c[1]; b = c[2]
        }
        // Grey ramp from black to 4x diffuse white.
        if (v > 0.86f && v < 0.96f) {
            val t = u * u * 4f
            r = t; g = t; b = t
        }
        out[0] = r; out[1] = g; out[2] = b
    }

    private fun mosaic(): Pair<ShortArray, BayerInfo> {
        val raw = ShortArray(w * h)
        val wb = floatArrayOf(2.0f, 1f, 1.6f)
        val px = FloatArray(3)
        val rnd = Random(7)
        val exposure = 0.22f // phones underexpose raw to protect highlights
        for (y in 0 until h) for (x in 0 until w) {
            scene(x, y, px)
            val c = if (y % 2 == 0) (if (x % 2 == 0) 0 else 1) else (if (x % 2 == 0) 1 else 2)
            val lin = px[c] * exposure / wb[c]
            val noisy = lin * 959f + 64f + rnd.nextFloat() * 2f - 1f
            raw[y * w + x] = min(max(noisy, 0f), 1023f).toInt().toShort()
        }
        val info = BayerInfo(
            w, h, intArrayOf(0, 1, 1, 2), floatArrayOf(64f, 64f, 64f, 64f), 1023f, wb,
            floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
        )
        return raw to info
    }

    /** Writes a PPM so the renders can be looked at; nothing asserts on them. */
    private fun save(d: Developed, name: String) {
        val dir = File("build/renders").apply { mkdirs() }
        val bytes = ByteArray(d.pixels.size * 3)
        for (i in d.pixels.indices) {
            val p = d.pixels[i]
            bytes[i * 3] = (p shr 16).toByte()
            bytes[i * 3 + 1] = (p shr 8).toByte()
            bytes[i * 3 + 2] = p.toByte()
        }
        File(dir, "$name.ppm").outputStream().use {
            it.write("P6\n${d.width} ${d.height}\n255\n".toByteArray())
            it.write(bytes)
        }
    }

    @Test
    fun rendersScene() {
        val (raw, info) = mosaic()
        val full = BayerSource(raw, info, 1)
        val half = BayerSource(raw, info, 2)
        val ev = Developer.meter(BayerSource(raw, info, 8))
        println("metered ev = $ev")
        assertTrue(ev > 0.5f && ev < 4f)

        val t0 = System.nanoTime()
        val d = Developer.develop(full, Look(), ev, 42, halationSource = half)
        println("full develop ${(System.nanoTime() - t0) / 1e6} ms")
        assertEquals(w, d.width)
        save(d, "default")

        save(Developer.develop(full, Look(grain = 0f, halation = 0f), ev, 42), "clean")
        save(Developer.develop(half, Look(), ev, 42), "default_half")
        save(Developer.develop(full, Look(monochrome = true, grain = 0.8f, grainSize = 0.8f), ev, 42), "mono")
        save(Developer.develop(full, Look(aspect = 6, halation = 1f, temperature = -0.4f, contrast = 0.4f), ev, 42), "scope")
        save(Developer.develop(full, Look(shadows = 0.8f, highlights = -0.8f, saturation = 0.5f), ev, 42), "flat")

        // Grey ramp must stay near neutral and increase monotonically.
        val clean = Developer.develop(full, Look(grain = 0f, halation = 0f), ev, 42)
        var last = -1
        val y = (h * 0.91f).toInt()
        for (x in 40 until w - 40 step 20) {
            val p = clean.pixels[y * w + x]
            val r = (p shr 16) and 0xff; val g = (p shr 8) and 0xff; val b = p and 0xff
            assertTrue("monotonic at $x", g >= last - 1)
            assertTrue("neutral-ish at $x: $r $g $b", abs(r - g) < 14 && b <= g + 8 && g - b < 30)
            last = g
        }
        assertTrue("reaches white", last > 245)
    }

    @Test
    fun orientationAndCrop() {
        val img = FloatImage(40, 30, FloatArray(40 * 30 * 3) { 0.18f }, 1f)
        val look = Look(grain = 0f, halation = 0f)
        var d = Developer.develop(img, look, 0f, 1, orientation = 90)
        assertEquals(30, d.width); assertEquals(40, d.height)
        d = Developer.develop(img, look.copy(aspect = 1), 0f, 1)
        assertEquals(30, d.width); assertEquals(30, d.height)
        d = Developer.develop(img, look.copy(aspect = 4), 0f, 1, orientation = 270)
        assertEquals(23, d.width); assertEquals(40, d.height)

        // A marked corner must travel correctly: top-left -> top-right at 90° clockwise.
        val data = FloatArray(40 * 30 * 3) { 0.01f }
        data[0] = 5f; data[1] = 5f; data[2] = 5f
        val m = FloatImage(40, 30, data, 5f)
        d = Developer.develop(m, look, 0f, 1, orientation = 90)
        assertTrue((d.pixels[29] and 0xff) > 200)
        d = Developer.develop(m, look, 0f, 1, orientation = 0, mirror = true)
        assertTrue((d.pixels[39] and 0xff) > 200)
    }

    @Test
    fun demosaicReproducesFlatColour() {
        val n = 16
        val raw = ShortArray(n * n)
        for (y in 0 until n) for (x in 0 until n) {
            val c = if (y % 2 == 0) (if (x % 2 == 0) 1 else 2) else (if (x % 2 == 0) 0 else 1) // GBRG
            raw[y * n + x] = (100 + intArrayOf(400, 200, 100)[c]).toShort()
        }
        val info = BayerInfo(
            n, n, intArrayOf(1, 2, 0, 1), floatArrayOf(100f, 100f, 100f, 100f), 1100f,
            floatArrayOf(1f, 1f, 1f), floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
        )
        for (bin in intArrayOf(1, 2, 4)) {
            val src = BayerSource(raw, info, bin)
            val row = FloatArray(src.width * 3)
            val reader = src.reader()
            for (y in 0 until src.height) {
                reader.read(y, row)
                for (x in 0 until src.width) {
                    assertEquals("r bin$bin $x,$y", 0.4f, row[x * 3], 1e-4f)
                    assertEquals("g bin$bin $x,$y", 0.2f, row[x * 3 + 1], 1e-4f)
                    assertEquals("b bin$bin $x,$y", 0.1f, row[x * 3 + 2], 1e-4f)
                }
            }
        }
    }

    private companion object {
        val CHART = arrayOf(
            floatArrayOf(0.45f, 0.03f, 0.03f), floatArrayOf(0.03f, 0.3f, 0.05f),
            floatArrayOf(0.03f, 0.05f, 0.45f), floatArrayOf(0.6f, 0.5f, 0.03f),
            floatArrayOf(0.5f, 0.3f, 0.22f), floatArrayOf(0.05f, 0.35f, 0.4f),
            floatArrayOf(0.4f, 0.05f, 0.35f), floatArrayOf(0.18f, 0.18f, 0.18f),
        )
    }
}
