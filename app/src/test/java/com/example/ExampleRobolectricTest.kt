package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.voxel.data.AppDatabase
import com.example.voxel.data.InventorySerializer
import com.example.voxel.data.WorldEntity
import com.example.voxel.data.WorldRepository
import com.example.voxel.engine.blocks.BlockType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WorldRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = WorldRepository(db.worldDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VoxelCraft 3D", appName)
    }

    @Test
    fun `save and load player coordinates and inventory in Room`() = runBlocking {
        val hotbar = listOf(
            BlockType.DIAMOND_ORE,
            BlockType.GOLD_ORE,
            BlockType.IRON_ORE,
            BlockType.COAL_ORE,
            BlockType.GLOWSTONE,
            BlockType.OBSIDIAN,
            BlockType.GLASS,
            BlockType.TORCH,
            BlockType.TNT
        )
        val serializedInventory = InventorySerializer.serializeHotbar(hotbar)

        val entity = WorldEntity(
            name = "Test Survival Realm",
            seed = 424242L,
            worldType = "PLAINS_AND_HILLS",
            gameMode = "SURVIVAL",
            playerX = 12.5f,
            playerY = 48.0f,
            playerZ = -34.2f,
            playerYaw = 90.0f,
            playerPitch = -15.0f,
            playerHealth = 18,
            isFlying = false,
            selectedHotbarIndex = 3,
            inventoryData = serializedInventory,
            lastPlayedTimestamp = System.currentTimeMillis()
        )

        val insertedId = repository.insertWorld(entity)
        val loaded = repository.getWorldById(insertedId)

        assertNotNull(loaded)
        assertEquals("Test Survival Realm", loaded?.name)
        assertEquals(12.5f, loaded?.playerX ?: 0f, 0.01f)
        assertEquals(48.0f, loaded?.playerY ?: 0f, 0.01f)
        assertEquals(-34.2f, loaded?.playerZ ?: 0f, 0.01f)
        assertEquals(18, loaded?.playerHealth)
        assertEquals(3, loaded?.selectedHotbarIndex)

        val restoredHotbar = InventorySerializer.deserializeHotbar(loaded?.inventoryData ?: "")
        assertEquals(9, restoredHotbar.size)
        assertEquals(BlockType.DIAMOND_ORE, restoredHotbar[0])
        assertEquals(BlockType.COAL_ORE, restoredHotbar[3])
    }
}

