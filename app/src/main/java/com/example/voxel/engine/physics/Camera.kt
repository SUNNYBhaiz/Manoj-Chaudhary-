package com.example.voxel.engine.physics

import android.opengl.Matrix
import kotlin.math.cos
import kotlin.math.sin

class Camera {
    var posX = 0f
    var posY = 32f
    var posZ = 0f

    var yaw = 0f   // degrees, 0 = facing north (-Z)
    var pitch = 0f // degrees, -89 to +89

    val viewMatrix = FloatArray(16)
    val projectionMatrix = FloatArray(16)

    val forwardX: Float
        get() = sin(Math.toRadians(yaw.toDouble())).toFloat() * cos(Math.toRadians(pitch.toDouble())).toFloat()
    val forwardY: Float
        get() = -sin(Math.toRadians(pitch.toDouble())).toFloat()
    val forwardZ: Float
        get() = -cos(Math.toRadians(yaw.toDouble())).toFloat() * cos(Math.toRadians(pitch.toDouble())).toFloat()

    val rightX: Float
        get() = cos(Math.toRadians(yaw.toDouble())).toFloat()
    val rightZ: Float
        get() = sin(Math.toRadians(yaw.toDouble())).toFloat()

    fun updateProjection(aspectRatio: Float, fovDegrees: Float = 75f) {
        Matrix.perspectiveM(projectionMatrix, 0, fovDegrees, aspectRatio, 0.1f, 150f)
    }

    fun updateView() {
        val eyeX = posX
        val eyeY = posY
        val eyeZ = posZ

        val targetX = eyeX + forwardX
        val targetY = eyeY + forwardY
        val targetZ = eyeZ + forwardZ

        Matrix.setLookAtM(
            viewMatrix, 0,
            eyeX, eyeY, eyeZ,
            targetX, targetY, targetZ,
            0f, 1f, 0f
        )
    }

    fun rotate(deltaYaw: Float, deltaPitch: Float) {
        yaw = (yaw + deltaYaw) % 360f
        if (yaw < 0f) yaw += 360f
        pitch = (pitch + deltaPitch).coerceIn(-89f, 89f)
    }
}
