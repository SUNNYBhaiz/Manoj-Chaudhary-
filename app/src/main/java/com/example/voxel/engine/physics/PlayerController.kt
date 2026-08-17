package com.example.voxel.engine.physics

import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.world.Raycast
import com.example.voxel.engine.world.RaycastHit
import com.example.voxel.engine.world.World
import com.example.voxel.engine.world.WorldConstants.GRAVITY
import com.example.voxel.engine.world.WorldConstants.JUMP_VELOCITY
import com.example.voxel.engine.world.WorldConstants.PLAYER_EYE_HEIGHT
import com.example.voxel.engine.world.WorldConstants.PLAYER_FLY_SPEED
import com.example.voxel.engine.world.WorldConstants.PLAYER_HEIGHT
import com.example.voxel.engine.world.WorldConstants.PLAYER_SPRINT_SPEED
import com.example.voxel.engine.world.WorldConstants.PLAYER_WALK_SPEED
import com.example.voxel.engine.world.WorldConstants.PLAYER_WIDTH
import kotlin.math.abs
import kotlin.math.sqrt

enum class GameMode {
    CREATIVE,
    SURVIVAL
}

class PlayerController(
    val camera: Camera = Camera(),
    var gameMode: GameMode = GameMode.CREATIVE
) {
    var posX = 8f
    var posY = 35f
    var posZ = 8f

    var velX = 0f
    var velY = 0f
    var velZ = 0f

    var isGrounded = false
    var isFlying = false
    var isSprinting = false

    // Survival stats
    var health = 20 // 10 hearts
    var maxHealth = 20
    var hunger = 20 // 10 drumsticks
    var maxHunger = 20
    private var fallStartY = 0f

    val boundingBox = AABB(0f, 0f, 0f, 0f, 0f, 0f)

    // Raycast target
    var currentTarget: RaycastHit? = null
        private set

    // Mining / Breaking state
    var isBreaking = false
    var breakProgress = 0f // 0.0 to 1.0
    var targetBreakingBlock: BlockType? = null
    var breakTargetX = 0
    var breakTargetY = 0
    var breakTargetZ = 0

    var onDamageTaken: ((damage: Int) -> Unit)? = null

    init {
        updateBoundingBox()
    }

    private fun updateBoundingBox() {
        boundingBox.set(posX, posY, posZ, PLAYER_WIDTH, PLAYER_HEIGHT)
        camera.posX = posX
        camera.posY = posY + PLAYER_EYE_HEIGHT
        camera.posZ = posZ
    }

    fun update(world: World, dt: Float, inputX: Float, inputZ: Float, jumpHeld: Boolean, sneakHeld: Boolean) {
        // 1. Raycast for block targeting
        currentTarget = Raycast.cast(
            world,
            camera.posX, camera.posY, camera.posZ,
            camera.forwardX, camera.forwardY, camera.forwardZ
        )

        // 2. Breaking logic
        updateBreaking(world, dt)

        // 3. Movement
        val speed = when {
            isFlying -> PLAYER_FLY_SPEED
            isSprinting -> PLAYER_SPRINT_SPEED
            else -> PLAYER_WALK_SPEED
        }

        // Direction relative to camera yaw
        val forwardX = camera.forwardX
        val forwardZ = camera.forwardZ
        val fLen = sqrt(forwardX * forwardX + forwardZ * forwardZ)
        val nxF = if (fLen > 0.001f) forwardX / fLen else 0f
        val nzF = if (fLen > 0.001f) forwardZ / fLen else 0f

        val rightX = camera.rightX
        val rightZ = camera.rightZ

        val moveDirX = (rightX * inputX + nxF * inputZ)
        val moveDirZ = (rightZ * inputX + nzF * inputZ)
        val moveLen = sqrt(moveDirX * moveDirX + moveDirZ * moveDirZ)

        if (moveLen > 0.01f) {
            velX = (moveDirX / moveLen) * speed
            velZ = (moveDirZ / moveLen) * speed
        } else {
            velX *= 0.7f
            velZ *= 0.7f
        }

        if (isFlying) {
            velY = when {
                jumpHeld -> PLAYER_FLY_SPEED
                sneakHeld -> -PLAYER_FLY_SPEED
                else -> 0f
            }
        } else {
            // Apply Gravity
            velY += GRAVITY * dt
            if (isGrounded) {
                if (jumpHeld) {
                    velY = JUMP_VELOCITY
                    isGrounded = false
                    fallStartY = posY
                }
            }
        }

        // Physics collision & movement
        val dx = velX * dt
        val dy = velY * dt
        val dz = velZ * dt

        val beforeY = boundingBox.minY
        val (mx, my, mz) = AABB.collideAndMove(world, boundingBox, dx, dy, dz, isFlying)

        posX += mx
        posY += my
        posZ += mz

        // Ground detection
        if (!isFlying) {
            if (dy < 0 && my > dy) {
                // Landed on ground
                if (!isGrounded && gameMode == GameMode.SURVIVAL) {
                    val fallDist = fallStartY - posY
                    if (fallDist > 3.5f) {
                        val dmg = (fallDist - 3.5f).toInt()
                        if (dmg > 0) {
                            health = (health - dmg).coerceAtLeast(0)
                            onDamageTaken?.invoke(dmg)
                        }
                    }
                }
                isGrounded = true
                velY = 0f
                fallStartY = posY
            } else {
                isGrounded = false
                if (velY > 0) {
                    fallStartY = posY
                }
            }
        }

        updateBoundingBox()
        camera.updateView()
    }

    private fun updateBreaking(world: World, dt: Float) {
        val hit = currentTarget
        if (isBreaking && hit != null && hit.blockType.hardness < Float.POSITIVE_INFINITY) {
            if (hit.blockX == breakTargetX && hit.blockY == breakTargetY && hit.blockZ == breakTargetZ) {
                val breakSpeed = if (gameMode == GameMode.CREATIVE) 100f else (1.0f / hit.blockType.hardness)
                breakProgress += breakSpeed * dt

                if (breakProgress >= 1.0f) {
                    // Block broken!
                    if (hit.blockType == BlockType.TNT) {
                        world.igniteTnt(hit.blockX, hit.blockY, hit.blockZ)
                    } else {
                        world.setBlock(hit.blockX, hit.blockY, hit.blockZ, BlockType.AIR)
                    }
                    breakProgress = 0f
                    isBreaking = false
                }
            } else {
                // Switched target
                breakTargetX = hit.blockX
                breakTargetY = hit.blockY
                breakTargetZ = hit.blockZ
                targetBreakingBlock = hit.blockType
                breakProgress = 0f
            }
        } else {
            breakProgress = 0f
        }
    }

    fun startBreaking() {
        val hit = currentTarget ?: return
        if (hit.blockType.hardness < Float.POSITIVE_INFINITY) {
            isBreaking = true
            breakTargetX = hit.blockX
            breakTargetY = hit.blockY
            breakTargetZ = hit.blockZ
            targetBreakingBlock = hit.blockType
            breakProgress = 0f

            // Creative instant break
            if (gameMode == GameMode.CREATIVE) {
                if (hit.blockType == BlockType.TNT) {
                    // In creative mode, let's allow breaking or igniting TNT
                }
            }
        }
    }

    fun stopBreaking() {
        isBreaking = false
        breakProgress = 0f
    }

    fun placeBlock(world: World, blockType: BlockType): Boolean {
        val hit = currentTarget ?: return false
        if (blockType == BlockType.AIR) return false

        // Adjacent position based on face normal
        val placeX = hit.blockX + hit.faceNormalX
        val placeY = hit.blockY + hit.faceNormalY
        val placeZ = hit.blockZ + hit.faceNormalZ

        if (placeY !in 0 until 64) return false

        // Check if player bounding box intersects the block if block is solid
        if (blockType.isSolid) {
            val blockBox = AABB(
                placeX.toFloat(), placeY.toFloat(), placeZ.toFloat(),
                placeX + 1f, placeY + 1f, placeZ + 1f
            )
            if (boundingBox.intersects(blockBox)) {
                return false // Cannot place inside player
            }
        }

        world.setBlock(placeX, placeY, placeZ, blockType)
        return true
    }

    fun toggleFlight() {
        if (gameMode == GameMode.CREATIVE) {
            isFlying = !isFlying
            velY = 0f
        }
    }

    fun respawn(spawnX: Float = 8f, spawnY: Float = 35f, spawnZ: Float = 8f) {
        posX = spawnX
        posY = spawnY
        posZ = spawnZ
        velX = 0f
        velY = 0f
        velZ = 0f
        health = maxHealth
        hunger = maxHunger
        isGrounded = false
        updateBoundingBox()
    }
}
