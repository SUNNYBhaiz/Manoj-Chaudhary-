package com.example.voxel.engine.render

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders celestial sky elements (Sun, Moon, procedural drifting voxel Clouds) and sky colors.
 */
class SkyRenderer {

    private var skyProgram = 0
    private var uMVPMatrixHandle = 0
    private var uColorHandle = 0
    private var aPositionHandle = 0

    private var sunMoonBuffer: FloatBuffer? = null
    private var cloudBuffer: FloatBuffer? = null
    private var cloudVertexCount = 0

    // Sun & Sky color states
    val skyColor = FloatArray(4)
    val fogColor = FloatArray(4)
    val sunLight = FloatArray(3) // ambient, sun, unused

    fun init() {
        val vertexShaderCode = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """.trimIndent()

        val vs = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        skyProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
        }

        uMVPMatrixHandle = GLES20.glGetUniformLocation(skyProgram, "u_MVPMatrix")
        uColorHandle = GLES20.glGetUniformLocation(skyProgram, "u_Color")
        aPositionHandle = GLES20.glGetAttribLocation(skyProgram, "a_Position")

        // Quad for Sun / Moon (10x10 units)
        val quadVerts = floatArrayOf(
            -5f, -5f, 0f,
             5f, -5f, 0f,
             5f,  5f, 0f,
            -5f, -5f, 0f,
             5f,  5f, 0f,
            -5f,  5f, 0f
        )
        sunMoonBuffer = ByteBuffer.allocateDirect(quadVerts.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(quadVerts)
                flip()
            }

        generateClouds()
    }

    private fun generateClouds() {
        val verts = mutableListOf<Float>()
        val cloudHeight = 58f
        val cloudRadius = 120
        val step = 8

        for (z in -cloudRadius..cloudRadius step step) {
            for (x in -cloudRadius..cloudRadius step step) {
                // Procedural cloud density
                val d = (sin(x * 0.04) * cos(z * 0.04) + sin(x * 0.08 + z * 0.05) * 0.5)
                if (d > 0.25) {
                    val fx = x.toFloat()
                    val fz = z.toFloat()
                    val s = step.toFloat()

                    // Horizontal cloud plane quad
                    verts.addAll(listOf(
                        fx, cloudHeight, fz,
                        fx + s, cloudHeight, fz,
                        fx + s, cloudHeight, fz + s,
                        fx, cloudHeight, fz,
                        fx + s, cloudHeight, fz + s,
                        fx, cloudHeight, fz + s
                    ))
                }
            }
        }

        cloudVertexCount = verts.size / 3
        val array = verts.toFloatArray()
        cloudBuffer = ByteBuffer.allocateDirect(array.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(array)
                flip()
            }
    }

    /**
     * Updates celestial lighting and sky colors based on time of day (0.0 = Sunrise, 0.25 = Noon, 0.5 = Sunset, 0.75 = Midnight).
     */
    fun updateTime(timeOfDay: Float) {
        val angle = timeOfDay * 2f * Math.PI.toFloat()
        val sunHeight = sin(angle)

        when {
            sunHeight > 0.2f -> {
                // Daytime (Clear bright sky)
                val t = ((sunHeight - 0.2f) / 0.8f).coerceIn(0f, 1f)
                skyColor[0] = 0.45f * t + 0.75f * (1f - t) // R
                skyColor[1] = 0.65f * t + 0.45f * (1f - t) // G
                skyColor[2] = 0.95f * t + 0.35f * (1f - t) // B
                skyColor[3] = 1.0f

                sunLight[0] = 0.35f // Ambient
                sunLight[1] = 0.65f * sunHeight // Sun directional
            }
            sunHeight in -0.2f..0.2f -> {
                // Sunrise / Sunset (Golden Orange / Pink)
                val t = ((sunHeight + 0.2f) / 0.4f).coerceIn(0f, 1f)
                skyColor[0] = 0.85f * t + 0.12f * (1f - t)
                skyColor[1] = 0.45f * t + 0.12f * (1f - t)
                skyColor[2] = 0.35f * t + 0.22f * (1f - t)
                skyColor[3] = 1.0f

                sunLight[0] = 0.25f
                sunLight[1] = 0.3f * t
            }
            else -> {
                // Night (Deep starry navy blue)
                skyColor[0] = 0.05f
                skyColor[1] = 0.07f
                skyColor[2] = 0.15f
                skyColor[3] = 1.0f

                sunLight[0] = 0.22f // Moon ambient light
                sunLight[1] = 0.05f
            }
        }

        // Fog matches sky
        fogColor[0] = skyColor[0]
        fogColor[1] = skyColor[1]
        fogColor[2] = skyColor[2]
        fogColor[3] = 1.0f
    }

    fun render(viewMatrix: FloatArray, projectionMatrix: FloatArray, playerX: Float, playerY: Float, playerZ: Float, timeOfDay: Float) {
        GLES20.glUseProgram(skyProgram)
        GLES20.glEnableVertexAttribArray(aPositionHandle)

        val vpMatrix = FloatArray(16)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // 1. Render Clouds
        if (cloudVertexCount > 0 && cloudBuffer != null) {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            val cloudModel = FloatArray(16)
            Matrix.setIdentityM(cloudModel, 0)
            // Drift clouds slowly
            val cloudOffset = (System.currentTimeMillis() % 100000L) / 1000f * 0.8f
            Matrix.translateM(cloudModel, 0, playerX + cloudOffset, 0f, playerZ)

            val mvp = FloatArray(16)
            Matrix.multiplyMM(mvp, 0, vpMatrix, 0, cloudModel, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvp, 0)
            // Semi-transparent fluffy white cloud
            GLES20.glUniform4f(uColorHandle, 1f, 1f, 1f, 0.75f)

            cloudBuffer?.position(0)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, cloudBuffer)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, cloudVertexCount)
        }

        // 2. Render Sun & Moon
        sunMoonBuffer?.let { buf ->
            buf.position(0)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, buf)

            val angle = timeOfDay * 2f * Math.PI.toFloat()
            val dist = 80f

            // Sun position
            val sunX = playerX + cos(angle) * dist
            val sunY = playerY + sin(angle) * dist
            val sunZ = playerZ

            val sunModel = FloatArray(16)
            Matrix.setIdentityM(sunModel, 0)
            Matrix.translateM(sunModel, 0, sunX, sunY, sunZ)
            // Face player
            Matrix.rotateM(sunModel, 0, -timeOfDay * 360f - 90f, 0f, 0f, 1f)

            val sunMvp = FloatArray(16)
            Matrix.multiplyMM(sunMvp, 0, vpMatrix, 0, sunModel, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, sunMvp, 0)
            GLES20.glUniform4f(uColorHandle, 1.0f, 0.95f, 0.4f, 1.0f) // Bright Golden Sun
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)

            // Moon position (opposite sun)
            val moonX = playerX - cos(angle) * dist
            val moonY = playerY - sin(angle) * dist
            val moonZ = playerZ

            val moonModel = FloatArray(16)
            Matrix.setIdentityM(moonModel, 0)
            Matrix.translateM(moonModel, 0, moonX, moonY, moonZ)
            Matrix.rotateM(moonModel, 0, -timeOfDay * 360f + 90f, 0f, 0f, 1f)

            val moonMvp = FloatArray(16)
            Matrix.multiplyMM(moonMvp, 0, vpMatrix, 0, moonModel, 0)

            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, moonMvp, 0)
            GLES20.glUniform4f(uColorHandle, 0.85f, 0.9f, 1.0f, 1.0f) // Soft Silver Moon
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
        }

        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
