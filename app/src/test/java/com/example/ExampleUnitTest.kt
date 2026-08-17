package com.example

import com.example.voxel.data.InventorySerializer
import com.example.voxel.engine.blocks.BlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHotbarSerializationAndDeserialization() {
        val originalHotbar = listOf(
            BlockType.GRASS,
            BlockType.DIRT,
            BlockType.STONE,
            BlockType.WOOD_PLANKS,
            BlockType.GLASS,
            BlockType.TORCH,
            BlockType.TNT,
            BlockType.BEDROCK,
            BlockType.WATER
        )

        val serialized = InventorySerializer.serializeHotbar(originalHotbar)
        val deserialized = InventorySerializer.deserializeHotbar(serialized)

        assertEquals(9, deserialized.size)
        assertEquals(originalHotbar, deserialized)
    }

    @Test
    fun testCraftingGridSerializationAndDeserialization() {
        val originalGrid = listOf(
            BlockType.WOOD_PLANKS,
            null,
            BlockType.WOOD_PLANKS,
            null
        )

        val serialized = InventorySerializer.serializeCraftingGrid(originalGrid)
        val deserialized = InventorySerializer.deserializeCraftingGrid(serialized)

        assertEquals(4, deserialized.size)
        assertEquals(BlockType.WOOD_PLANKS, deserialized[0])
        assertNull(deserialized[1])
        assertEquals(BlockType.WOOD_PLANKS, deserialized[2])
        assertNull(deserialized[3])
    }
}

