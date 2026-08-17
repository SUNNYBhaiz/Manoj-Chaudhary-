package com.example.voxel.engine.render

import android.opengl.GLES20
import android.util.Log

class VoxelShader {

    var programId: Int = 0
        private set

    var uMVPMatrixHandle: Int = 0
        private set
    var uTextureHandle: Int = 0
        private set
    var uFogColorHandle: Int = 0
        private set
    var uFogDistHandle: Int = 0
        private set
    var uSunLightHandle: Int = 0
        private set
    var aPositionHandle: Int = 0
        private set
    var aColorHandle: Int = 0
        private set
    var aTexCoordHandle: Int = 0
        private set

    fun init() {
        val vertexShaderCode = """
            uniform mat4 u_MVPMatrix;
            uniform vec3 u_SunLight; // x: ambient, y: sun intensity, z: unused
            attribute vec4 a_Position;
            attribute vec3 a_Color; // AO + face lighting
            attribute vec2 a_TexCoordinate;
            
            varying vec2 v_TexCoordinate;
            varying vec3 v_Color;
            varying float v_Distance;
            
            void main() {
                v_TexCoordinate = a_TexCoordinate;
                // Calculate vertex lighting combined with sun intensity
                v_Color = a_Color * (u_SunLight.x + u_SunLight.y);
                vec4 pos = u_MVPMatrix * a_Position;
                v_Distance = pos.z;
                gl_Position = pos;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform sampler2D u_Texture;
            uniform vec4 u_FogColor;
            uniform vec2 u_FogDist; // x: start, y: end
            
            varying vec2 v_TexCoordinate;
            varying vec3 v_Color;
            varying float v_Distance;
            
            void main() {
                vec4 texColor = texture2D(u_Texture, v_TexCoordinate);
                if (texColor.a < 0.1) {
                    discard;
                }
                
                vec3 litColor = texColor.rgb * v_Color;
                
                // Distance fog blending
                float fogFactor = clamp((u_FogDist.y - v_Distance) / (u_FogDist.y - u_FogDist.x), 0.0, 1.0);
                vec3 finalColor = mix(u_FogColor.rgb, litColor, fogFactor);
                
                gl_FragColor = vec4(finalColor, texColor.a * u_FogColor.a);
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)

            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(it, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                Log.e("VoxelShader", "Error linking shader: " + GLES20.glGetProgramInfoLog(it))
                GLES20.glDeleteProgram(it)
                programId = 0
            }
        }

        uMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        uTextureHandle = GLES20.glGetUniformLocation(programId, "u_Texture")
        uFogColorHandle = GLES20.glGetUniformLocation(programId, "u_FogColor")
        uFogDistHandle = GLES20.glGetUniformLocation(programId, "u_FogDist")
        uSunLightHandle = GLES20.glGetUniformLocation(programId, "u_SunLight")

        aPositionHandle = GLES20.glGetAttribLocation(programId, "a_Position")
        aColorHandle = GLES20.glGetAttribLocation(programId, "a_Color")
        aTexCoordHandle = GLES20.glGetAttribLocation(programId, "a_TexCoordinate")
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e("VoxelShader", "Could not compile shader $type: ${GLES20.glGetShaderInfoLog(shader)}")
                GLES20.glDeleteShader(shader)
            }
        }
    }
}
