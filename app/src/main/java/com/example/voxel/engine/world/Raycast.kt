package com.example.voxel.engine.world

import com.example.voxel.engine.blocks.BlockType
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign
import kotlin.math.sqrt

data class RaycastHit(
    val blockX: Int,
    val blockY: Int,
    val blockZ: Int,
    val faceNormalX: Int,
    val faceNormalY: Int,
    val faceNormalZ: Int,
    val blockType: BlockType,
    val distance: Float
)

object Raycast {

    /**
     * Fast 3D Amanatides & Woo voxel traversal algorithm (DDA).
     */
    fun cast(
        world: World,
        originX: Float, originY: Float, originZ: Float,
        dirX: Float, dirY: Float, dirZ: Float,
        maxDist: Float = WorldConstants.REACH_DISTANCE
    ): RaycastHit? {
        val len = sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)
        if (len < 0.0001f) return null

        val dx = dirX / len
        val dy = dirY / len
        val dz = dirZ / len

        var x = floor(originX).toInt()
        var y = floor(originY).toInt()
        var z = floor(originZ).toInt()

        val stepX = sign(dx).toInt()
        val stepY = sign(dy).toInt()
        val stepZ = sign(dz).toInt()

        val deltaX = if (dx != 0f) abs(1f / dx) else Float.MAX_VALUE
        val deltaY = if (dy != 0f) abs(1f / dy) else Float.MAX_VALUE
        val deltaZ = if (dz != 0f) abs(1f / dz) else Float.MAX_VALUE

        var maxX = if (dx > 0) (floor(originX) + 1f - originX) * deltaX else (originX - floor(originX)) * deltaX
        var maxY = if (dy > 0) (floor(originY) + 1f - originY) * deltaY else (originY - floor(originY)) * deltaY
        var maxZ = if (dz > 0) (floor(originZ) + 1f - originZ) * deltaZ else (originZ - floor(originZ)) * deltaZ

        var faceX = 0
        var faceY = 0
        var faceZ = 0
        var dist = 0f

        while (dist <= maxDist) {
            val b = world.getBlock(x, y, z)
            if (b != BlockType.AIR && b != BlockType.WATER) {
                return RaycastHit(
                    blockX = x,
                    blockY = y,
                    blockZ = z,
                    faceNormalX = faceX,
                    faceNormalY = faceY,
                    faceNormalZ = faceZ,
                    blockType = b,
                    distance = dist
                )
            }

            if (maxX < maxY) {
                if (maxX < maxZ) {
                    dist = maxX
                    maxX += deltaX
                    x += stepX
                    faceX = -stepX
                    faceY = 0
                    faceZ = 0
                } else {
                    dist = maxZ
                    maxZ += deltaZ
                    z += stepZ
                    faceX = 0
                    faceY = 0
                    faceZ = -stepZ
                }
            } else {
                if (maxY < maxZ) {
                    dist = maxY
                    maxY += deltaY
                    y += stepY
                    faceX = 0
                    faceY = -stepY
                    faceZ = 0
                } else {
                    dist = maxZ
                    maxZ += deltaZ
                    z += stepZ
                    faceX = 0
                    faceY = 0
                    faceZ = -stepZ
                }
            }
        }

        return null
    }
}
