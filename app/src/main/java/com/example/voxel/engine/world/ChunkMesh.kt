package com.example.voxel.engine.world

import android.opengl.GLES20
import com.example.voxel.engine.blocks.BlockRenderType
import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.render.TextureAtlas
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_X
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_Y
import com.example.voxel.engine.world.WorldConstants.CHUNK_SIZE_Z
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class ChunkMesh(val chunk: Chunk) {

    // Opaque geometry
    private var opaqueVbo = 0
    private var opaqueIbo = 0
    var opaqueIndexCount = 0
        private set

    // Translucent/Transparent geometry (water, glass, flowers)
    private var alphaVbo = 0
    private var alphaIbo = 0
    var alphaIndexCount = 0
        private set

    // Temporary CPU builders
    private var opaqueVertexBuffer: FloatBuffer? = null
    private var opaqueIndexBuffer: ShortBuffer? = null
    private var alphaVertexBuffer: FloatBuffer? = null
    private var alphaIndexBuffer: ShortBuffer? = null

    @Volatile
    var isUploaded = false

    /**
     * Builds vertex data on background thread.
     */
    fun buildMesh(world: World) {
        val oVerts = FastFloatList(16384)
        val oIndices = FastShortList(24576)
        val aVerts = FastFloatList(4096)
        val aIndices = FastShortList(6144)

        var oIndexOffset: Short = 0
        var aIndexOffset: Short = 0

        val chunkX = chunk.chunkX
        val chunkZ = chunk.chunkZ
        val worldX = chunk.worldOriginX.toFloat()
        val worldZ = chunk.worldOriginZ.toFloat()

        for (y in 0 until CHUNK_SIZE_Y) {
            for (z in 0 until CHUNK_SIZE_Z) {
                for (x in 0 until CHUNK_SIZE_X) {
                    val block = chunk.getBlock(x, y, z)
                    if (block == BlockType.AIR) continue

                    val wx = worldX + x
                    val wy = y.toFloat()
                    val wz = worldZ + z

                    when (block.renderType) {
                        BlockRenderType.CROSS -> {
                            // Cross quads for flowers, torches
                            addCrossMesh(
                                wx, wy, wz, block,
                                aVerts, aIndices, aIndexOffset
                            )
                            aIndexOffset = (aIndexOffset + 8).toShort()
                        }
                        BlockRenderType.TRANSLUCENT, BlockRenderType.TRANSPARENT -> {
                            // Check exposed faces
                            aIndexOffset = addBlockFaces(
                                world, chunkX, chunkZ, x, y, z, wx, wy, wz, block,
                                aVerts, aIndices, aIndexOffset, isAlpha = true
                            )
                        }
                        BlockRenderType.OPAQUE -> {
                            oIndexOffset = addBlockFaces(
                                world, chunkX, chunkZ, x, y, z, wx, wy, wz, block,
                                oVerts, oIndices, oIndexOffset, isAlpha = false
                            )
                        }
                    }
                }
            }
        }

        opaqueIndexCount = oIndices.size
        alphaIndexCount = aIndices.size

        if (opaqueIndexCount > 0) {
            opaqueVertexBuffer = ByteBuffer.allocateDirect(oVerts.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(oVerts.data, 0, oVerts.size)
                    flip()
                }
            opaqueIndexBuffer = ByteBuffer.allocateDirect(oIndices.size * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer().apply {
                    put(oIndices.data, 0, oIndices.size)
                    flip()
                }
        }

        if (alphaIndexCount > 0) {
            alphaVertexBuffer = ByteBuffer.allocateDirect(aVerts.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(aVerts.data, 0, aVerts.size)
                    flip()
                }
            alphaIndexBuffer = ByteBuffer.allocateDirect(aIndices.size * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer().apply {
                    put(aIndices.data, 0, aIndices.size)
                    flip()
                }
        }

        chunk.isDirty = false
        chunk.isMeshReady = true
        isUploaded = false
    }

    private fun addBlockFaces(
        world: World,
        cx: Int, cz: Int,
        lx: Int, ly: Int, lz: Int,
        wx: Float, wy: Float, wz: Float,
        block: BlockType,
        verts: FastFloatList,
        indices: FastShortList,
        startOffset: Short,
        isAlpha: Boolean
    ): Short {
        var offset = startOffset

        fun isFaceVisible(nx: Int, ny: Int, nz: Int): Boolean {
            if (ny < 0) return false
            if (ny >= CHUNK_SIZE_Y) return true
            val neighbor = world.getBlock(wx.toInt() + nx, wy.toInt() + ny, wz.toInt() + nz)
            if (neighbor == BlockType.AIR) return true
            if (isAlpha) {
                // Don't render water-against-water interior faces
                return neighbor != block && (neighbor.renderType != BlockRenderType.OPAQUE)
            }
            return neighbor.renderType != BlockRenderType.OPAQUE
        }

        // TOP FACE (+Y)
        if (isFaceVisible(0, 1, 0)) {
            val uvs = TextureAtlas.getUVs(block.topTexX, block.topTexY)
            val ao = computeAO(world, wx.toInt(), wy.toInt() + 1, wz.toInt(), 0, 1, 0)
            val light = 1.0f

            addQuad(
                wx, wy + 1f, wz + 1f, ao[0] * light, uvs[0], uvs[3],
                wx + 1f, wy + 1f, wz + 1f, ao[1] * light, uvs[2], uvs[3],
                wx + 1f, wy + 1f, wz, ao[2] * light, uvs[2], uvs[1],
                wx, wy + 1f, wz, ao[3] * light, uvs[0], uvs[1],
                verts, indices, offset
            )
            offset = (offset + 4).toShort()
        }

        // BOTTOM FACE (-Y)
        if (isFaceVisible(0, -1, 0)) {
            val uvs = TextureAtlas.getUVs(block.bottomTexX, block.bottomTexY)
            val ao = computeAO(world, wx.toInt(), wy.toInt() - 1, wz.toInt(), 0, -1, 0)
            val light = 0.55f // ambient bottom shading

            addQuad(
                wx, wy, wz, ao[0] * light, uvs[0], uvs[3],
                wx + 1f, wy, wz, ao[1] * light, uvs[2], uvs[3],
                wx + 1f, wy, wz + 1f, ao[2] * light, uvs[2], uvs[1],
                wx, wy, wz + 1f, ao[3] * light, uvs[0], uvs[1],
                verts, indices, offset
            )
            offset = (offset + 4).toShort()
        }

        // NORTH FACE (-Z)
        if (isFaceVisible(0, 0, -1)) {
            val uvs = TextureAtlas.getUVs(block.sideTexX, block.sideTexY)
            val ao = computeAO(world, wx.toInt(), wy.toInt(), wz.toInt() - 1, 0, 0, -1)
            val light = 0.85f

            addQuad(
                wx + 1f, wy, wz, ao[0] * light, uvs[2], uvs[3],
                wx, wy, wz, ao[1] * light, uvs[0], uvs[3],
                wx, wy + 1f, wz, ao[2] * light, uvs[0], uvs[1],
                wx + 1f, wy + 1f, wz, ao[3] * light, uvs[2], uvs[1],
                verts, indices, offset
            )
            offset = (offset + 4).toShort()
        }

        // SOUTH FACE (+Z)
        if (isFaceVisible(0, 0, 1)) {
            val uvs = TextureAtlas.getUVs(block.sideTexX, block.sideTexY)
            val ao = computeAO(world, wx.toInt(), wy.toInt(), wz.toInt() + 1, 0, 0, 1)
            val light = 0.85f

            addQuad(
                wx, wy, wz + 1f, ao[0] * light, uvs[0], uvs[3],
                wx + 1f, wy, wz + 1f, ao[1] * light, uvs[2], uvs[3],
                wx + 1f, wy + 1f, wz + 1f, ao[2] * light, uvs[2], uvs[1],
                wx, wy + 1f, wz + 1f, ao[3] * light, uvs[0], uvs[1],
                verts, indices, offset
            )
            offset = (offset + 4).toShort()
        }

        // WEST FACE (-X)
        if (isFaceVisible(-1, 0, 0)) {
            val uvs = TextureAtlas.getUVs(block.sideTexX, block.sideTexY)
            val ao = computeAO(world, wx.toInt() - 1, wy.toInt(), wz.toInt(), -1, 0, 0)
            val light = 0.72f

            addQuad(
                wx, wy, wz, ao[0] * light, uvs[0], uvs[3],
                wx, wy, wz + 1f, ao[1] * light, uvs[2], uvs[3],
                wx, wy + 1f, wz + 1f, ao[2] * light, uvs[2], uvs[1],
                wx, wy + 1f, wz, ao[3] * light, uvs[0], uvs[1],
                verts, indices, offset
            )
            offset = (offset + 4).toShort()
        }

        // EAST FACE (+X)
        if (isFaceVisible(1, 0, 0)) {
            val uvs = TextureAtlas.getUVs(block.sideTexX, block.sideTexY)
            val ao = computeAO(world, wx.toInt() + 1, wy.toInt(), wz.toInt(), 1, 0, 0)
            val light = 0.72f

            addQuad(
                wx + 1f, wy, wz + 1f, ao[0] * light, uvs[0], uvs[3],
                wx + 1f, wy, wz, ao[1] * light, uvs[2], uvs[3],
                wx + 1f, wy + 1f, wz, ao[2] * light, uvs[2], uvs[1],
                wx + 1f, wy + 1f, wz + 1f, ao[3] * light, uvs[0], uvs[1],
                verts, indices, offset
            )
            offset = (offset + 4).toShort()
        }

        return offset
    }

    private fun addCrossMesh(
        wx: Float, wy: Float, wz: Float,
        block: BlockType,
        verts: FastFloatList,
        indices: FastShortList,
        offset: Short
    ) {
        val uvs = TextureAtlas.getUVs(block.sideTexX, block.sideTexY)
        val light = 1.0f

        // Diagonal 1: (0,0) to (1,1)
        addQuad(
            wx, wy, wz, light, uvs[0], uvs[3],
            wx + 1f, wy, wz + 1f, light, uvs[2], uvs[3],
            wx + 1f, wy + 1f, wz + 1f, light, uvs[2], uvs[1],
            wx, wy + 1f, wz, light, uvs[0], uvs[1],
            verts, indices, offset
        )

        // Diagonal 2: (0,1) to (1,0)
        addQuad(
            wx, wy, wz + 1f, light, uvs[0], uvs[3],
            wx + 1f, wy, wz, light, uvs[2], uvs[3],
            wx + 1f, wy + 1f, wz, light, uvs[2], uvs[1],
            wx, wy + 1f, wz + 1f, light, uvs[0], uvs[1],
            verts, indices, (offset + 4).toShort()
        )
    }

    private inline fun addQuad(
        x1: Float, y1: Float, z1: Float, l1: Float, u1: Float, v1: Float,
        x2: Float, y2: Float, z2: Float, l2: Float, u2: Float, v2: Float,
        x3: Float, y3: Float, z3: Float, l3: Float, u3: Float, v3: Float,
        x4: Float, y4: Float, z4: Float, l4: Float, u4: Float, v4: Float,
        verts: FastFloatList,
        indices: FastShortList,
        offset: Short
    ) {
        // Vertex format: x, y, z, r, g, b, u, v (8 floats)
        verts.add(x1, y1, z1, l1, l1, l1, u1, v1)
        verts.add(x2, y2, z2, l2, l2, l2, u2, v2)
        verts.add(x3, y3, z3, l3, l3, l3, u3, v3)
        verts.add(x4, y4, z4, l4, l4, l4, u4, v4)

        // Two triangles: (0, 1, 2) and (2, 3, 0)
        indices.add(
            offset,
            (offset + 1).toShort(),
            (offset + 2).toShort(),
            (offset + 2).toShort(),
            (offset + 3).toShort(),
            offset
        )
    }

    /**
     * Compute 4-corner Ambient Occlusion for soft block crevice shadows.
     */
    private fun computeAO(
        world: World,
        bx: Int, by: Int, bz: Int,
        nx: Int, ny: Int, nz: Int
    ): FloatArray {
        fun isOccluding(dx: Int, dy: Int, dz: Int): Int {
            val b = world.getBlock(bx + dx, by + dy, bz + dz)
            return if (b.isSolid && b.renderType == BlockRenderType.OPAQUE) 1 else 0
        }

        fun ao(s1: Int, s2: Int, c: Int): Float {
            val count = if (s1 == 1 && s2 == 1) 3 else s1 + s2 + c
            return when (count) {
                0 -> 1.0f
                1 -> 0.82f
                2 -> 0.65f
                else -> 0.48f
            }
        }

        return when {
            ny != 0 -> { // Y face
                val sN = isOccluding(0, 0, -1)
                val sS = isOccluding(0, 0, 1)
                val sW = isOccluding(-1, 0, 0)
                val sE = isOccluding(1, 0, 0)
                val cNW = isOccluding(-1, 0, -1)
                val cNE = isOccluding(1, 0, -1)
                val cSW = isOccluding(-1, 0, 1)
                val cSE = isOccluding(1, 0, 1)

                floatArrayOf(
                    ao(sW, sS, cSW),
                    ao(sE, sS, cSE),
                    ao(sE, sN, cNE),
                    ao(sW, sN, cNW)
                )
            }
            nz != 0 -> { // Z face
                val sD = isOccluding(0, -1, 0)
                val sU = isOccluding(0, 1, 0)
                val sW = isOccluding(-1, 0, 0)
                val sE = isOccluding(1, 0, 0)
                val cDW = isOccluding(-1, -1, 0)
                val cDE = isOccluding(1, -1, 0)
                val cUW = isOccluding(-1, 1, 0)
                val cUE = isOccluding(1, 1, 0)

                floatArrayOf(
                    ao(sD, sW, cDW),
                    ao(sD, sE, cDE),
                    ao(sU, sE, cUE),
                    ao(sU, sW, cUW)
                )
            }
            else -> { // X face
                val sD = isOccluding(0, -1, 0)
                val sU = isOccluding(0, 1, 0)
                val sN = isOccluding(0, 0, -1)
                val sS = isOccluding(0, 0, 1)
                val cDN = isOccluding(0, -1, -1)
                val cDS = isOccluding(0, -1, 1)
                val cUN = isOccluding(0, 1, -1)
                val cUS = isOccluding(0, 1, 1)

                floatArrayOf(
                    ao(sD, sN, cDN),
                    ao(sD, sS, cDS),
                    ao(sU, sS, cUS),
                    ao(sU, sN, cUN)
                )
            }
        }
    }

    /**
     * Uploads the VBO and IBO to the GPU on the GL thread.
     */
    fun uploadToGpu() {
        if (isUploaded) return

        // Opaque VBO / IBO
        opaqueVertexBuffer?.let { vb ->
            opaqueIndexBuffer?.let { ib ->
                if (opaqueVbo == 0) {
                    val buffers = IntArray(2)
                    GLES20.glGenBuffers(2, buffers, 0)
                    opaqueVbo = buffers[0]
                    opaqueIbo = buffers[1]
                }
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, opaqueVbo)
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vb.capacity() * 4, vb, GLES20.GL_STATIC_DRAW)

                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, opaqueIbo)
                GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, ib.capacity() * 2, ib, GLES20.GL_STATIC_DRAW)
            }
        }

        // Alpha VBO / IBO
        alphaVertexBuffer?.let { vb ->
            alphaIndexBuffer?.let { ib ->
                if (alphaVbo == 0) {
                    val buffers = IntArray(2)
                    GLES20.glGenBuffers(2, buffers, 0)
                    alphaVbo = buffers[0]
                    alphaIbo = buffers[1]
                }
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, alphaVbo)
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vb.capacity() * 4, vb, GLES20.GL_STATIC_DRAW)

                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, alphaIbo)
                GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, ib.capacity() * 2, ib, GLES20.GL_STATIC_DRAW)
            }
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        // Clear CPU buffers to free memory
        opaqueVertexBuffer = null
        opaqueIndexBuffer = null
        alphaVertexBuffer = null
        alphaIndexBuffer = null

        isUploaded = true
    }

    fun renderOpaque(posHandle: Int, colHandle: Int, uvHandle: Int) {
        if (opaqueIndexCount == 0 || opaqueVbo == 0) return
        val stride = 8 * 4 // 8 floats = 32 bytes

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, opaqueVbo)
        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, stride, 0)

        GLES20.glEnableVertexAttribArray(colHandle)
        GLES20.glVertexAttribPointer(colHandle, 3, GLES20.GL_FLOAT, false, stride, 3 * 4)

        GLES20.glEnableVertexAttribArray(uvHandle)
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, stride, 6 * 4)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, opaqueIbo)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, opaqueIndexCount, GLES20.GL_UNSIGNED_SHORT, 0)
    }

    fun renderAlpha(posHandle: Int, colHandle: Int, uvHandle: Int) {
        if (alphaIndexCount == 0 || alphaVbo == 0) return
        val stride = 8 * 4

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, alphaVbo)
        GLES20.glEnableVertexAttribArray(posHandle)
        GLES20.glVertexAttribPointer(posHandle, 3, GLES20.GL_FLOAT, false, stride, 0)

        GLES20.glEnableVertexAttribArray(colHandle)
        GLES20.glVertexAttribPointer(colHandle, 3, GLES20.GL_FLOAT, false, stride, 3 * 4)

        GLES20.glEnableVertexAttribArray(uvHandle)
        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, stride, 6 * 4)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, alphaIbo)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, alphaIndexCount, GLES20.GL_UNSIGNED_SHORT, 0)
    }

    fun release() {
        val buffers = intArrayOf(opaqueVbo, opaqueIbo, alphaVbo, alphaIbo)
        GLES20.glDeleteBuffers(4, buffers, 0)
        opaqueVbo = 0
        opaqueIbo = 0
        alphaVbo = 0
        alphaIbo = 0
        isUploaded = false
    }
}

/**
 * Lightweight primitive array builders to avoid boxing overhead.
 */
class FastFloatList(initialCapacity: Int = 4096) {
    var data = FloatArray(initialCapacity)
    var size = 0

    private fun ensureCapacity(minCap: Int) {
        if (minCap > data.size) {
            val newCap = Math.max(data.size * 2, minCap)
            data = data.copyOf(newCap)
        }
    }

    fun add(v1: Float, v2: Float, v3: Float, v4: Float, v5: Float, v6: Float, v7: Float, v8: Float) {
        ensureCapacity(size + 8)
        data[size++] = v1
        data[size++] = v2
        data[size++] = v3
        data[size++] = v4
        data[size++] = v5
        data[size++] = v6
        data[size++] = v7
        data[size++] = v8
    }
}

class FastShortList(initialCapacity: Int = 4096) {
    var data = ShortArray(initialCapacity)
    var size = 0

    private fun ensureCapacity(minCap: Int) {
        if (minCap > data.size) {
            val newCap = Math.max(data.size * 2, minCap)
            data = data.copyOf(newCap)
        }
    }

    fun add(i1: Short, i2: Short, i3: Short, i4: Short, i5: Short, i6: Short) {
        ensureCapacity(size + 6)
        data[size++] = i1
        data[size++] = i2
        data[size++] = i3
        data[size++] = i4
        data[size++] = i5
        data[size++] = i6
    }
}
