package com.example.voxel.engine.world

/**
 * Global constants for world dimensions, physics, and chunk partitioning.
 */
object WorldConstants {
    const val CHUNK_SIZE_X = 16
    const val CHUNK_SIZE_Z = 16
    const val CHUNK_SIZE_Y = 64 // 64 vertical height for ultra-smooth low-end device performance

    const val WATER_LEVEL = 20
    const val BEDROCK_LEVEL = 0

    const val REACH_DISTANCE = 5.5f
    const val GRAVITY = -22.0f
    const val JUMP_VELOCITY = 7.8f
    const val PLAYER_WALK_SPEED = 4.3f
    const val PLAYER_SPRINT_SPEED = 6.8f
    const val PLAYER_FLY_SPEED = 10.0f
    const val PLAYER_EYE_HEIGHT = 1.62f
    const val PLAYER_HEIGHT = 1.8f
    const val PLAYER_WIDTH = 0.6f

    // Day/Night cycle
    const val DAY_LENGTH_SECONDS = 300f // 5 minutes for full cycle
}
