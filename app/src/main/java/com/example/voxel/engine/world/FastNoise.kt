package com.example.voxel.engine.world

import kotlin.math.floor

/**
 * Fast 2D and 3D Perlin / Simplex noise generator optimized for mobile voxel terrain.
 */
class FastNoise(val seed: Long = 1337L) {

    private val perm = IntArray(512)
    private val permMod12 = IntArray(512)

    init {
        val p = IntArray(256)
        for (i in 0 until 256) p[i] = i
        // Shuffle using seed
        var currentSeed = seed
        for (i in 255 downTo 1) {
            currentSeed = currentSeed * 6364136223846793005L + 1442695040888963407L
            val j = ((currentSeed ushr 32) and 0x7FFFFFFF).toInt() % (i + 1)
            val tmp = p[i]
            p[i] = p[j]
            p[j] = tmp
        }
        for (i in 0 until 512) {
            perm[i] = p[i and 255]
            permMod12[i] = (perm[i] % 12)
        }
    }

    private fun fade(t: Float): Float = t * t * t * (t * (t * 6f - 15f) + 10f)

    private fun grad2(hash: Int, x: Float, y: Float): Float {
        val h = hash and 7
        val u = if (h < 4) x else y
        val v = if (h < 4) y else x
        return (if ((h and 1) != 0) -u else u) + (if ((h and 2) != 0) -2.0f * v else 2.0f * v)
    }

    private fun grad3(hash: Int, x: Float, y: Float, z: Float): Float {
        val h = hash and 15
        val u = if (h < 8) x else y
        val v = if (h < 4) y else if (h == 12 || h == 14) x else z
        return (if ((h and 1) == 0) u else -u) + (if ((h and 2) == 0) v else -v)
    }

    fun noise2D(x: Float, y: Float): Float {
        val X = floor(x).toInt() and 255
        val Y = floor(y).toInt() and 255

        val xf = x - floor(x)
        val yf = y - floor(y)

        val u = fade(xf)
        val v = fade(yf)

        val a = perm[X] + Y
        val aa = perm[a]
        val ab = perm[a + 1]
        val b = perm[X + 1] + Y
        val ba = perm[b]
        val bb = perm[b + 1]

        val res = lerp(
            v,
            lerp(u, grad2(perm[aa], xf, yf), grad2(perm[ba], xf - 1f, yf)),
            lerp(u, grad2(perm[ab], xf, yf - 1f), grad2(perm[bb], xf - 1f, yf - 1f))
        )
        return (res + 1f) * 0.5f // normalized 0..1
    }

    fun noise3D(x: Float, y: Float, z: Float): Float {
        val X = floor(x).toInt() and 255
        val Y = floor(y).toInt() and 255
        val Z = floor(z).toInt() and 255

        val xf = x - floor(x)
        val yf = y - floor(y)
        val zf = z - floor(z)

        val u = fade(xf)
        val v = fade(yf)
        val w = fade(zf)

        val a = perm[X] + Y
        val aa = perm[a] + Z
        val ab = perm[a + 1] + Z
        val b = perm[X + 1] + Y
        val ba = perm[b] + Z
        val bb = perm[b + 1] + Z

        val res = lerp(
            w,
            lerp(
                v,
                lerp(u, grad3(perm[aa], xf, yf, zf), grad3(perm[ba], xf - 1f, yf, zf)),
                lerp(u, grad3(perm[ab], xf, yf - 1f, zf), grad3(perm[bb], xf - 1f, yf - 1f, zf))
            ),
            lerp(
                v,
                lerp(u, grad3(perm[aa + 1], xf, yf, zf - 1f), grad3(perm[ba + 1], xf - 1f, yf, zf - 1f)),
                lerp(u, grad3(perm[ab + 1], xf, yf - 1f, zf - 1f), grad3(perm[bb + 1], xf - 1f, yf - 1f, zf - 1f))
            )
        )
        return (res + 1f) * 0.5f
    }

    /**
     * Fractal Brownian Motion (FBM) multi-octave noise
     */
    fun fbm2D(x: Float, y: Float, octaves: Int = 4, persistence: Float = 0.5f, lacunarity: Float = 2.0f): Float {
        var total = 0f
        var frequency = 1f
        var amplitude = 1f
        var maxValue = 0f
        for (i in 0 until octaves) {
            total += noise2D(x * frequency, y * frequency) * amplitude
            maxValue += amplitude
            amplitude *= persistence
            frequency *= lacunarity
        }
        return total / maxValue
    }

    private fun lerp(t: Float, a: Float, b: Float): Float = a + t * (b - a)
}
