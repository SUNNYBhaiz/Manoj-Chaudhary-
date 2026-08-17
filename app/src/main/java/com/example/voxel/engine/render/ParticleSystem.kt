package com.example.voxel.engine.render

import android.opengl.GLES20
import android.opengl.Matrix
import com.example.voxel.engine.blocks.BlockType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Random

data class VoxelParticle(
    var x: Float,
    var y: Float,
    var z: Float,
    var vx: Float,
    var vy: Float,
    var vz: Float,
    var r: Float,
    var g: Float,
    var b: Float,
    var size: Float,
    var life: Float,
    var maxLife: Float
)

class ParticleSystem {

    private val particles = mutableListOf<VoxelParticle>()
    private val rng = Random()

    private var program = 0
    private var uMVPMatrixHandle = 0
    private var aPositionHandle = 0
    private var aColorHandle = 0

    private val maxParticles = 250
    private var vertexBuffer: FloatBuffer? = null

    fun init() {
        val vs = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            attribute vec4 a_Color;
            varying vec4 v_Color;
            void main() {
                v_Color = a_Color;
                gl_Position = u_MVPMatrix * a_Position;
            }
        """.trimIndent()

        val fs = """
            precision mediump float;
            varying vec4 v_Color;
            void main() {
                gl_FragColor = v_Color;
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
        aPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        aColorHandle = GLES20.glGetAttribLocation(program, "a_Color")

        // Allocate buffer for rendering cubes/quads (max 250 particles * 6 vertices * 7 floats)
        vertexBuffer = ByteBuffer.allocateDirect(maxParticles * 6 * 7 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }

    fun spawnBlockBreakParticles(bx: Int, by: Int, bz: Int, block: BlockType) {
        val count = 12
        val (baseR, baseG, baseB) = getBlockColor(block)

        synchronized(particles) {
            for (i in 0 until count) {
                if (particles.size >= maxParticles) particles.removeAt(0)

                val px = bx + 0.2f + rng.nextFloat() * 0.6f
                val py = by + 0.2f + rng.nextFloat() * 0.6f
                val pz = bz + 0.2f + rng.nextFloat() * 0.6f

                val vx = (rng.nextFloat() - 0.5f) * 3.5f
                val vy = rng.nextFloat() * 3.5f + 1.0f
                val vz = (rng.nextFloat() - 0.5f) * 3.5f

                val varC = (rng.nextFloat() - 0.5f) * 0.15f
                val life = 0.5f + rng.nextFloat() * 0.4f

                particles.add(
                    VoxelParticle(
                        x = px, y = py, z = pz,
                        vx = vx, vy = vy, vz = vz,
                        r = (baseR + varC).coerceIn(0f, 1f),
                        g = (baseG + varC).coerceIn(0f, 1f),
                        b = (baseB + varC).coerceIn(0f, 1f),
                        size = 0.12f,
                        life = life,
                        maxLife = life
                    )
                )
            }
        }
    }

    fun spawnExplosionParticles(ex: Float, ey: Float, ez: Float) {
        synchronized(particles) {
            for (i in 0 until 40) {
                if (particles.size >= maxParticles) particles.removeAt(0)

                val speed = rng.nextFloat() * 8.0f + 2.0f
                val theta = rng.nextFloat() * Math.PI.toFloat() * 2f
                val phi = (rng.nextFloat() - 0.5f) * Math.PI.toFloat()

                val vx = speed * Math.cos(phi.toDouble()).toFloat() * Math.cos(theta.toDouble()).toFloat()
                val vy = speed * Math.sin(phi.toDouble()).toFloat() + 2f
                val vz = speed * Math.cos(phi.toDouble()).toFloat() * Math.sin(theta.toDouble()).toFloat()

                val isSmoke = rng.nextBoolean()
                val (r, g, b) = if (isSmoke) {
                    Triple(0.8f, 0.8f, 0.8f) // Smoke gray
                } else {
                    Triple(1.0f, 0.6f + rng.nextFloat() * 0.4f, 0.1f) // Fire orange/yellow
                }

                val life = 0.6f + rng.nextFloat() * 0.5f
                particles.add(
                    VoxelParticle(
                        x = ex, y = ey, z = ez,
                        vx = vx, vy = vy, vz = vz,
                        r = r, g = g, b = b,
                        size = 0.22f,
                        life = life,
                        maxLife = life
                    )
                )
            }
        }
    }

    private fun getBlockColor(block: BlockType): Triple<Float, Float, Float> {
        return when (block) {
            BlockType.GRASS -> Triple(0.35f, 0.7f, 0.2f)
            BlockType.DIRT -> Triple(0.52f, 0.37f, 0.26f)
            BlockType.STONE -> Triple(0.5f, 0.5f, 0.5f)
            BlockType.COBBLESTONE -> Triple(0.45f, 0.45f, 0.45f)
            BlockType.WOOD_OAK, BlockType.WOOD_PLANKS -> Triple(0.65f, 0.5f, 0.3f)
            BlockType.WOOD_BIRCH -> Triple(0.9f, 0.9f, 0.88f)
            BlockType.LEAVES -> Triple(0.2f, 0.55f, 0.15f)
            BlockType.SAND -> Triple(0.85f, 0.8f, 0.55f)
            BlockType.GLASS -> Triple(0.8f, 0.9f, 1.0f)
            BlockType.WATER -> Triple(0.2f, 0.45f, 0.85f)
            BlockType.COAL_ORE -> Triple(0.15f, 0.15f, 0.15f)
            BlockType.IRON_ORE -> Triple(0.8f, 0.65f, 0.5f)
            BlockType.GOLD_ORE -> Triple(1.0f, 0.85f, 0.1f)
            BlockType.DIAMOND_ORE -> Triple(0.3f, 0.9f, 0.95f)
            BlockType.BRICK -> Triple(0.7f, 0.3f, 0.2f)
            BlockType.TNT -> Triple(0.9f, 0.2f, 0.1f)
            BlockType.SNOW -> Triple(0.95f, 0.97f, 1.0f)
            BlockType.CACTUS -> Triple(0.2f, 0.6f, 0.2f)
            BlockType.GLOWSTONE -> Triple(1.0f, 0.85f, 0.4f)
            BlockType.OBSIDIAN -> Triple(0.1f, 0.05f, 0.15f)
            BlockType.FLOWER_RED -> Triple(0.9f, 0.15f, 0.15f)
            BlockType.FLOWER_YELLOW -> Triple(1.0f, 0.9f, 0.1f)
            else -> Triple(0.5f, 0.5f, 0.5f)
        }
    }

    fun update(dt: Float) {
        synchronized(particles) {
            val iter = particles.iterator()
            while (iter.hasNext()) {
                val p = iter.next()
                p.life -= dt
                if (p.life <= 0f) {
                    iter.remove()
                    continue
                }
                // Gravity & velocity
                p.vy -= 18.0f * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.z += p.vz * dt
            }
        }
    }

    fun render(viewMatrix: FloatArray, projectionMatrix: FloatArray) {
        val buf = vertexBuffer ?: return
        buf.clear()

        var count = 0
        synchronized(particles) {
            for (p in particles) {
                if (count >= maxParticles) break
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                val s = p.size * 0.5f

                // Billboard facing quad (or cube proxy)
                val x1 = p.x - s
                val x2 = p.x + s
                val y1 = p.y - s
                val y2 = p.y + s
                val z = p.z

                // 2 triangles: 6 vertices (x, y, z, r, g, b, a)
                fun addV(x: Float, y: Float) {
                    buf.put(x); buf.put(y); buf.put(z)
                    buf.put(p.r); buf.put(p.g); buf.put(p.b); buf.put(alpha)
                }

                addV(x1, y1)
                addV(x2, y1)
                addV(x2, y2)
                addV(x1, y1)
                addV(x2, y2)
                addV(x1, y2)
                count++
            }
        }

        if (count == 0) return
        buf.flip()

        GLES20.glUseProgram(program)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)

        val stride = 7 * 4
        buf.position(0)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, stride, buf)

        buf.position(3)
        GLES20.glEnableVertexAttribArray(aColorHandle)
        GLES20.glVertexAttribPointer(aColorHandle, 4, GLES20.GL_FLOAT, false, stride, buf)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count * 6)
        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
