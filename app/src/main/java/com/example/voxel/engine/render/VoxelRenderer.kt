package com.example.voxel.engine.render

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.physics.PlayerController
import com.example.voxel.engine.world.World
import com.example.voxel.engine.world.WorldConstants.DAY_LENGTH_SECONDS
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.floor

class VoxelRenderer(
    val world: World,
    val player: PlayerController
) : GLSurfaceView.Renderer {

    val voxelShader = VoxelShader()
    val skyRenderer = SkyRenderer()
    val outlineRenderer = BlockOutlineRenderer()
    val particleSystem = ParticleSystem()

    var renderDistanceChunks = 4 // Default 4 chunks (64x64 blocks radius)
    var fovDegrees = 75f
    var timeOfDay = 0.25f // 0.25 = Noon (Clear sunny day)
    var isDayNightCycleActive = true

    var currentFps = 60
        private set
    var renderedChunkCount = 0
        private set

    private var lastFrameTime = System.nanoTime()
    private var fpsCounter = 0
    private var fpsTimer = 0f
    private var screenWidth = 1920
    private var screenHeight = 1080

    // Virtual inputs from UI
    var inputMoveX = 0f
    var inputMoveZ = 0f
    var inputJumpHeld = false
    var inputSneakHeld = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glCullFace(GLES20.GL_BACK)

        TextureAtlas.initTexture()
        voxelShader.init()
        skyRenderer.init()
        outlineRenderer.init()
        particleSystem.init()

        // Hook explosion and break particles
        world.onExplosionListener = { ex, ey, ez, _ ->
            particleSystem.spawnExplosionParticles(ex, ey, ez)
        }
        world.onBlockBreakListener = { bx, by, bz, b ->
            particleSystem.spawnBlockBreakParticles(bx, by, bz, b)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        GLES20.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.toFloat().coerceAtLeast(1f)
        player.camera.updateProjection(aspect, fovDegrees)
    }

    fun updateFov(newFov: Float) {
        fovDegrees = newFov
        val aspect = screenWidth.toFloat() / screenHeight.toFloat().coerceAtLeast(1f)
        player.camera.updateProjection(aspect, fovDegrees)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = ((now - lastFrameTime) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
        lastFrameTime = now

        // FPS Calculation
        fpsCounter++
        fpsTimer += dt
        if (fpsTimer >= 1.0f) {
            currentFps = fpsCounter
            fpsCounter = 0
            fpsTimer = 0f
        }

        // 1. Update Game Physics and Entities
        if (isDayNightCycleActive) {
            timeOfDay = (timeOfDay + dt / DAY_LENGTH_SECONDS) % 1.0f
        }
        skyRenderer.updateTime(timeOfDay)

        player.update(world, dt, inputMoveX, inputMoveZ, inputJumpHeld, inputSneakHeld)
        world.updateTnt(dt)
        particleSystem.update(dt)
        world.updateLoadedChunks(player.posX, player.posZ, renderDistanceChunks)

        // 2. Clear Screen with Dynamic Sky Color
        val sky = skyRenderer.skyColor
        GLES20.glClearColor(sky[0], sky[1], sky[2], 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        val viewMatrix = player.camera.viewMatrix
        val projMatrix = player.camera.projectionMatrix

        // 3. Render Sky Dome, Sun, Moon & Clouds
        skyRenderer.render(viewMatrix, projMatrix, player.posX, player.posY, player.posZ, timeOfDay)

        // 4. Render Voxel World Chunks
        GLES20.glUseProgram(voxelShader.programId)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureAtlas.textureId)
        GLES20.glUniform1i(voxelShader.uTextureHandle, 0)

        // Fog setup
        val fog = skyRenderer.fogColor
        GLES20.glUniform4f(voxelShader.uFogColorHandle, fog[0], fog[1], fog[2], fog[3])
        val fogDistEnd = (renderDistanceChunks * 16.0f) * 0.95f
        val fogDistStart = fogDistEnd * 0.55f
        GLES20.glUniform2f(voxelShader.uFogDistHandle, fogDistStart, fogDistEnd)

        // Sun light intensity
        val sun = skyRenderer.sunLight
        GLES20.glUniform3f(voxelShader.uSunLightHandle, sun[0], sun[1], sun[2])

        // Calculate View-Projection Matrix
        val vpMatrix = FloatArray(16)
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)
        GLES20.glUniformMatrix4fv(voxelShader.uMVPMatrixHandle, 1, false, vpMatrix, 0)

        // Upload dirty meshes to GPU
        for ((_, mesh) in world.chunkMeshes) {
            if (!mesh.isUploaded && mesh.chunk.isMeshReady) {
                mesh.uploadToGpu()
            }
        }

        // Pass 1: Opaque Geometries
        var chunksDrawn = 0
        val playerChunkX = floor(player.posX / 16f).toInt()
        val playerChunkZ = floor(player.posZ / 16f).toInt()
        val maxDistSq = renderDistanceChunks * renderDistanceChunks

        for ((_, mesh) in world.chunkMeshes) {
            val dx = mesh.chunk.chunkX - playerChunkX
            val dz = mesh.chunk.chunkZ - playerChunkZ
            if (dx * dx + dz * dz <= maxDistSq) {
                mesh.renderOpaque(
                    voxelShader.aPositionHandle,
                    voxelShader.aColorHandle,
                    voxelShader.aTexCoordHandle
                )
                chunksDrawn++
            }
        }
        renderedChunkCount = chunksDrawn

        // Pass 2: Alpha Translucent (Water, Glass, Flowers)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glDisable(GLES20.GL_CULL_FACE) // Render both sides of transparent leaves/flowers/water

        for ((_, mesh) in world.chunkMeshes) {
            val dx = mesh.chunk.chunkX - playerChunkX
            val dz = mesh.chunk.chunkZ - playerChunkZ
            if (dx * dx + dz * dz <= maxDistSq) {
                mesh.renderAlpha(
                    voxelShader.aPositionHandle,
                    voxelShader.aColorHandle,
                    voxelShader.aTexCoordHandle
                )
            }
        }
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        GLES20.glDisable(GLES20.GL_BLEND)

        // 5. Render Targeted Block Wireframe Box
        val hit = player.currentTarget
        if (hit != null) {
            outlineRenderer.render(viewMatrix, projMatrix, hit.blockX, hit.blockY, hit.blockZ)
        }

        // 6. Render 3D Breaking & Explosion Particles
        particleSystem.render(viewMatrix, projMatrix)
    }
}
