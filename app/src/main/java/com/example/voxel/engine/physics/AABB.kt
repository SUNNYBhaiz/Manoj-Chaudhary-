package com.example.voxel.engine.physics

import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.world.World
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class AABB(
    var minX: Float,
    var minY: Float,
    var minZ: Float,
    var maxX: Float,
    var maxY: Float,
    var maxZ: Float
) {
    fun intersects(other: AABB): Boolean {
        return (minX < other.maxX && maxX > other.minX &&
                minY < other.maxY && maxY > other.minY &&
                minZ < other.maxZ && maxZ > other.minZ)
    }

    fun set(x: Float, y: Float, z: Float, width: Float, height: Float) {
        val halfW = width * 0.5f
        minX = x - halfW
        minY = y
        minZ = z - halfW
        maxX = x + halfW
        maxY = y + height
        maxZ = z + halfW
    }

    companion object {
        fun collideAndMove(
            world: World,
            box: AABB,
            dx: Float, dy: Float, dz: Float,
            isFlying: Boolean
        ): Triple<Float, Float, Float> {
            if (isFlying) {
                // Free flight, no block collisions
                return Triple(dx, dy, dz)
            }

            var mx = dx
            var my = dy
            var mz = dz

            val queryMinX = floor(min(box.minX, box.minX + mx) - 1).toInt()
            val queryMaxX = floor(max(box.maxX, box.maxX + mx) + 1).toInt()
            val queryMinY = floor(min(box.minY, box.minY + my) - 1).toInt().coerceAtLeast(0)
            val queryMaxY = floor(max(box.maxY, box.maxY + my) + 1).toInt().coerceAtMost(63)
            val queryMinZ = floor(min(box.minZ, box.minZ + mz) - 1).toInt()
            val queryMaxZ = floor(max(box.maxZ, box.maxZ + mz) + 1).toInt()

            val blockBoxes = mutableListOf<AABB>()
            for (y in queryMinY..queryMaxY) {
                for (z in queryMinZ..queryMaxZ) {
                    for (x in queryMinX..queryMaxX) {
                        val b = world.getBlock(x, y, z)
                        if (b.isSolid && b != BlockType.AIR && b != BlockType.WATER) {
                            blockBoxes.add(
                                AABB(
                                    x.toFloat(), y.toFloat(), z.toFloat(),
                                    x + 1f, y + 1f, z + 1f
                                )
                            )
                        }
                    }
                }
            }

            // Resolve Y axis first (gravity / floor / ceiling)
            for (bb in blockBoxes) {
                if (box.minX < bb.maxX && box.maxX > bb.minX && box.minZ < bb.maxZ && box.maxZ > bb.minZ) {
                    if (my > 0 && box.maxY <= bb.minY && box.maxY + my > bb.minY) {
                        my = bb.minY - box.maxY
                    } else if (my < 0 && box.minY >= bb.maxY && box.minY + my < bb.maxY) {
                        my = bb.maxY - box.minY
                    }
                }
            }
            box.minY += my
            box.maxY += my

            // Resolve X axis
            for (bb in blockBoxes) {
                if (box.minY < bb.maxY && box.maxY > bb.minY && box.minZ < bb.maxZ && box.maxZ > bb.minZ) {
                    if (mx > 0 && box.maxX <= bb.minX && box.maxX + mx > bb.minX) {
                        mx = bb.minX - box.maxX
                    } else if (mx < 0 && box.minX >= bb.maxX && box.minX + mx < bb.maxX) {
                        mx = bb.maxX - box.minX
                    }
                }
            }
            box.minX += mx
            box.maxX += mx

            // Resolve Z axis
            for (bb in blockBoxes) {
                if (box.minX < bb.maxX && box.maxX > bb.minX && box.minY < bb.maxY && box.maxY > bb.minY) {
                    if (mz > 0 && box.maxZ <= bb.minZ && box.maxZ + mz > bb.minZ) {
                        mz = bb.minZ - box.maxZ
                    } else if (mz < 0 && box.minZ >= bb.maxZ && box.minZ + mz < bb.maxZ) {
                        mz = bb.maxZ - box.minZ
                    }
                }
            }

            return Triple(mx, my, mz)
        }
    }
}
