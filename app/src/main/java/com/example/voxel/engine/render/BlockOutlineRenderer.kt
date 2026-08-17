package com.example.voxel.engine.render

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class BlockOutlineRenderer {

    private var program = 0
    private var uMVPMatrixHandle = 0
    private var uColorHandle = 0
    private var aPositionHandle = 0

    private var lineBuffer: FloatBuffer? = null

    fun init() {
        val vs = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

        val fs = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """.trimIndent()

        val vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also {
            GLES20.glShaderSource(it, vs)
            GLES20.glCompileShader(it)
        }
        val fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also {
            GLES20.glShaderSource(it, fs)
            GLES20.glCompileShader(it)
        }

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vShader)
            GLES20.glAttachShader(it, fShader)
            GLES20.glLinkProgram(it)
        }

        uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix")
        uColorHandle = GLES20.glGetUniformLocation(program, "u_Color")
        aPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")

        // 12 edges of a 1x1 cube (slightly offset by 0.002 to avoid z-fighting)
        val s = 0.002f
        val e = 1.002f
        val lines = floatArrayOf(
            // Bottom 4
            -s, -s, -s,  e, -s, -s,
             e, -s, -s,  e, -s,  e,
             e, -s,  e, -s, -s,  e,
            -s, -s,  e, -s, -s, -s,
            // Top 4
            -s,  e, -s,  e,  e, -s,
             e,  e, -s,  e,  e,  e,
             e,  e,  e, -s,  e,  e,
            -s,  e,  e, -s,  e, -s,
            // 4 Vertical Pillars
            -s, -s, -s, -s,  e, -s,
             e, -s, -s,  e,  e, -s,
             e, -s,  e,  e,  e,  e,
            -s, -s,  e, -s,  e,  e
        )

        lineBuffer = ByteBuffer.allocateDirect(lines.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(lines)
                flip()
            }
    }

    fun render(viewMatrix: FloatArray, projectionMatrix: FloatArray, blockX: Int, blockY: Int, blockZ: Int) {
        val buf = lineBuffer ?: return
        GLES20.glUseProgram(program)
        GLES20.glEnableVertexAttribArray(aPositionHandle)

        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, blockX.toFloat(), blockY.toFloat(), blockZ.toFloat())

        val mvpMatrix = FloatArray(16)
        val vpMatrix = FloatArray(16)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        // Distinct semi-black outline with crisp contrast
        GLES20.glUniform4f(uColorHandle, 0.05f, 0.05f, 0.05f, 0.7f)

        GLES20.glLineWidth(3.0f)
        buf.position(0)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, buf)
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 24)
    }
}
