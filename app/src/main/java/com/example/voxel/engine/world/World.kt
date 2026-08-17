package com.example.voxel.engine.world

import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_X
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_Y
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_Z
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor

data class ActiveTnt(
    val x: Float,
    val y: Float,
    val z: Float,
    var fuseSeconds: Float = 3.0f,
    var isWhiteFlash: Boolean = false
)

class World(
    val seed: Long = 1337L,
    val worldType: WorldType = WorldType.PLAINS_AND_HILLS
) {
    val generator = WorldGenerator(seed, worldType)
    private val worldScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Chunk maps keyed by chunkKey(chunkX, chunkZ)
    val chunks = ConcurrentHashMap<Long, Chunk>()
    val chunkMeshes = ConcurrentHashMap<Long, ChunkMesh>()

    // User modified blocks: key = blockKey(x,y,z), value = blockId
    val modifiedBlocks = ConcurrentHashMap<Long, Byte>()

    // Active TNT entities
    val activeTntList = mutableListOf<ActiveTnt>()

    // TNT explosion callback
    var onExplosionListener: ((x: Float, y: Float, z: Float, radius: Float) -> Unit)? = null
    var onBlockBreakListener: ((x: Int, y: Int, z: Int, block: BlockType) -> Unit)? = null
    var onBlockPlaceListener: ((x: Int, y: Int, z: Int, block: BlockType) -> Unit)? = null

    companion object {
        fun chunkKey(cx: Int, cz: Int): Long {
            return (cx.toLong() and 0xFFFFFFFFL) or ((cz.toLong() and 0xFFFFFFFFL) shl 32)
        }

        fun blockKey(x: Int, y: Int, z: Int): Long {
            return (x.toLong() and 0x3FFFFFFL) or
                    ((z.toLong() and 0x3FFFFFFL) shl 26) or
                    ((y.toLong() and 0xFFFL) shl 52)
        }
    }

    fun getChunk(chunkX: Int, chunkZ: Int): Chunk? = chunks[chunkKey(chunkX, chunkZ)]

    fun getOrCreateChunk(chunkX: Int, chunkZ: Int): Chunk {
        val key = chunkKey(chunkX, chunkZ)
        return chunks.computeIfAbsent(key) {
            val chunk = Chunk(chunkX, chunkZ)
            generator.generateChunk(chunk)
            // Apply modifications
            applyModificationsToChunk(chunk)
            chunk
        }
    }

    private fun applyModificationsToChunk(chunk: Chunk) {
        val cx = chunk.worldOriginX
        val cz = chunk.worldOriginZ
        for (ly in 0 until CHUNK_SIZE_Y) {
            for (lz in 0 until CHUNK_SIZE_Z) {
                for (lx in 0 until CHUNK_SIZE_X) {
                    val wx = cx + lx
                    val wz = cz + lz
                    val bKey = blockKey(wx, ly, wz)
                    val mod = modifiedBlocks[bKey]
                    if (mod != null) {
                        chunk.setBlockId(lx, ly, lz, mod)
                    }
                }
            }
        }
    }

    fun getBlock(x: Int, y: Int, z: Int): BlockType {
        if (y !in 0 until CHUNK_SIZE_Y) return BlockType.AIR
        val cx = floor(x.toFloat() / CHUNK_SIZE_X).toInt()
        val cz = floor(z.toFloat() / CHUNK_SIZE_Z).toInt()
        val chunk = chunks[chunkKey(cx, cz)] ?: return BlockType.AIR

        var lx = x - chunk.worldOriginX
        var lz = z - chunk.worldOriginZ
        if (lx < 0) lx += CHUNK_SIZE_X
        if (lz < 0) lz += CHUNK_SIZE_Z

        return chunk.getBlock(lx, y, lz)
    }

    fun setBlock(x: Int, y: Int, z: Int, blockType: BlockType) {
        if (y !in 0 until CHUNK_SIZE_Y) return
        val cx = floor(x.toFloat() / CHUNK_SIZE_X).toInt()
        val cz = floor(z.toFloat() / CHUNK_SIZE_Z).toInt()
        val chunk = getOrCreateChunk(cx, cz)

        var lx = x - chunk.worldOriginX
        var lz = z - chunk.worldOriginZ
        if (lx < 0) lx += CHUNK_SIZE_X
        if (lz < 0) lz += CHUNK_SIZE_Z

        val oldBlock = chunk.getBlock(lx, y, lz)
        if (oldBlock == blockType) return

        chunk.setBlock(lx, y, lz, blockType)
        modifiedBlocks[blockKey(x, y, z)] = blockType.id

        if (blockType == BlockType.AIR) {
            onBlockBreakListener?.invoke(x, y, z, oldBlock)
        } else {
            onBlockPlaceListener?.invoke(x, y, z, blockType)
        }

        // Flag neighbor chunks dirty if on border
        if (lx == 0) getChunk(cx - 1, cz)?.isDirty = true
        if (lx == CHUNK_SIZE_X - 1) getChunk(cx + 1, cz)?.isDirty = true
        if (lz == 0) getChunk(cx, cz - 1)?.isDirty = true
        if (lz == CHUNK_SIZE_Z - 1) getChunk(cx, cz + 1)?.isDirty = true

        rebuildMeshAsync(chunk)
    }

    fun igniteTnt(x: Int, y: Int, z: Int) {
        setBlock(x, y, z, BlockType.AIR)
        synchronized(activeTntList) {
            activeTntList.add(ActiveTnt(x + 0.5f, y + 0.5f, z + 0.5f))
        }
    }

    fun updateTnt(dt: Float) {
        val exploding = mutableListOf<ActiveTnt>()
        synchronized(activeTntList) {
            val iter = activeTntList.iterator()
            while (iter.hasNext()) {
                val tnt = iter.next()
                tnt.fuseSeconds -= dt
                tnt.isWhiteFlash = ((tnt.fuseSeconds * 8).toInt() % 2 == 0)
                if (tnt.fuseSeconds <= 0f) {
                    exploding.add(tnt)
                    iter.remove()
                }
            }
        }

        for (tnt in exploding) {
            explode(tnt.x, tnt.y, tnt.z, radius = 3.5f)
        }
    }

    fun explode(originX: Float, originY: Float, originZ: Float, radius: Float = 3.5f) {
        val minX = floor(originX - radius).toInt()
        val maxX = floor(originX + radius).toInt()
        val minY = floor(originY - radius).toInt().coerceIn(1, CHUNK_SIZE_Y - 1)
        val maxY = floor(originY + radius).toInt().coerceIn(1, CHUNK_SIZE_Y - 1)
        val minZ = floor(originZ - radius).toInt()
        val maxZ = floor(originZ + radius).toInt()

        val r2 = radius * radius
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    val dx = x + 0.5f - originX
                    val dy = y + 0.5f - originY
                    val dz = z + 0.5f - originZ
                    if (dx * dx + dy * dy + dz * dz <= r2) {
                        val b = getBlock(x, y, z)
                        if (b != BlockType.AIR && b != BlockType.BEDROCK && b != BlockType.OBSIDIAN) {
                            if (b == BlockType.TNT) {
                                igniteTnt(x, y, z)
                            } else {
                                setBlock(x, y, z, BlockType.AIR)
                            }
                        }
                    }
                }
            }
        }

        onExplosionListener?.invoke(originX, originY, originZ, radius)
    }

    fun updateLoadedChunks(playerX: Float, playerZ: Float, renderDistance: Int) {
        val playerChunkX = floor(playerX / CHUNK_SIZE_X).toInt()
        val playerChunkZ = floor(playerZ / CHUNK_SIZE_Z).toInt()

        // Load chunks within distance
        for (dz in -renderDistance..renderDistance) {
            for (dx in -renderDistance..renderDistance) {
                if (dx * dx + dz * dz <= renderDistance * renderDistance) {
                    val cx = playerChunkX + dx
                    val cz = playerChunkZ + dz
                    val key = chunkKey(cx, cz)
                    if (!chunks.containsKey(key)) {
                        worldScope.launch {
                            val chunk = getOrCreateChunk(cx, cz)
                            rebuildMeshAsync(chunk)
                        }
                    }
                }
            }
        }

        // Unload distant chunks to save RAM
        val maxDistanceSq = (renderDistance + 2) * (renderDistance + 2)
        val keysToRemove = mutableListOf<Long>()
        for ((key, chunk) in chunks) {
            val dx = chunk.chunkX - playerChunkX
            val dz = chunk.chunkZ - playerChunkZ
            if (dx * dx + dz * dz > maxDistanceSq) {
                keysToRemove.add(key)
            }
        }
        for (key in keysToRemove) {
            chunks.remove(key)
            chunkMeshes.remove(key)?.release()
        }
    }

    fun rebuildMeshAsync(chunk: Chunk) {
        worldScope.launch {
            val key = chunkKey(chunk.chunkX, chunk.chunkZ)
            val mesh = chunkMeshes.computeIfAbsent(key) { ChunkMesh(chunk) }
            mesh.buildMesh(this@World)
        }
    }
}
