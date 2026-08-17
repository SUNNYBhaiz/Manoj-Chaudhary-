package com.example.voxel.engine.world

import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_X
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_Y
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_Z

class Chunk(val chunkX: Int, val chunkZ: Int) {
    val worldOriginX = chunkX * CHUNK_SIZE_X
    val worldOriginZ = chunkZ * CHUNK_SIZE_Z

    val blocks = ByteArray(CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z)

    @Volatile
    var isDirty = true

    @Volatile
    var isGenerated = false

    @Volatile
    var isMeshReady = false

    private inline fun index(x: Int, y: Int, z: Int): Int {
        return (y * CHUNK_SIZE_Z + z) * CHUNK_SIZE_X + x
    }

    fun getBlock(x: Int, y: Int, z: Int): BlockType {
        if (x !in 0 until CHUNK_SIZE_X || y !in 0 until CHUNK_SIZE_Y || z !in 0 until CHUNK_SIZE_Z) {
            return BlockType.AIR
        }
        val id = blocks[index(x, y, z)]
        return BlockType.fromId(id)
    }

    fun getBlockId(x: Int, y: Int, z: Int): Byte {
        if (x !in 0 until CHUNK_SIZE_X || y !in 0 until CHUNK_SIZE_Y || z !in 0 until CHUNK_SIZE_Z) {
            return 0
        }
        return blocks[index(x, y, z)]
    }

    fun setBlock(x: Int, y: Int, z: Int, blockType: BlockType) {
        if (x in 0 until CHUNK_SIZE_X && y in 0 until CHUNK_SIZE_Y && z in 0 until CHUNK_SIZE_Z) {
            val idx = index(x, y, z)
            if (blocks[idx] != blockType.id) {
                blocks[idx] = blockType.id
                isDirty = true
            }
        }
    }

    fun setBlockId(x: Int, y: Int, z: Int, id: Byte) {
        if (x in 0 until CHUNK_SIZE_X && y in 0 until CHUNK_SIZE_Y && z in 0 until CHUNK_SIZE_Z) {
            val idx = index(x, y, z)
            if (blocks[idx] != id) {
                blocks[idx] = id
                isDirty = true
            }
        }
    }
}
