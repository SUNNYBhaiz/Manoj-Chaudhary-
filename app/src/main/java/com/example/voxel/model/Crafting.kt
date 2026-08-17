package com.example.voxel.model

import com.example.voxel.engine.blocks.BlockType

data class ItemStack(
    val blockType: BlockType,
    var count: Int = 1
) {
    fun copyStack(): ItemStack = ItemStack(blockType, count)
}

data class CraftingRecipe(
    val result: ItemStack,
    val pattern: List<List<BlockType?>>, // 2x2 or 3x3 grid
    val name: String
)

object CraftingManager {
    val RECIPES = listOf(
        // Wood Log -> 4 Planks
        CraftingRecipe(
            result = ItemStack(BlockType.WOOD_PLANKS, 4),
            pattern = listOf(
                listOf(BlockType.WOOD_OAK)
            ),
            name = "Oak Planks (x4)"
        ),
        CraftingRecipe(
            result = ItemStack(BlockType.WOOD_PLANKS, 4),
            pattern = listOf(
                listOf(BlockType.WOOD_BIRCH)
            ),
            name = "Birch Planks (x4)"
        ),
        // 4 Planks -> Crafting Table
        CraftingRecipe(
            result = ItemStack(BlockType.CRAFTING_TABLE, 1),
            pattern = listOf(
                listOf(BlockType.WOOD_PLANKS, BlockType.WOOD_PLANKS),
                listOf(BlockType.WOOD_PLANKS, BlockType.WOOD_PLANKS)
            ),
            name = "Crafting Table"
        ),
        // 1 Coal + 1 Plank (or stick) -> 4 Torches
        CraftingRecipe(
            result = ItemStack(BlockType.TORCH, 4),
            pattern = listOf(
                listOf(BlockType.COAL_ORE),
                listOf(BlockType.WOOD_PLANKS)
            ),
            name = "Torches (x4)"
        ),
        // 4 Sand + 1 Dirt/Coal -> TNT
        CraftingRecipe(
            result = ItemStack(BlockType.TNT, 1),
            pattern = listOf(
                listOf(BlockType.SAND, BlockType.DIRT),
                listOf(BlockType.DIRT, BlockType.SAND)
            ),
            name = "TNT Explosive"
        ),
        // 4 Bricks / Clay -> Brick block
        CraftingRecipe(
            result = ItemStack(BlockType.BRICK, 1),
            pattern = listOf(
                listOf(BlockType.STONE, BlockType.DIRT),
                listOf(BlockType.DIRT, BlockType.STONE)
            ),
            name = "Bricks"
        ),
        // 6 Planks + 3 Books -> Bookshelf
        CraftingRecipe(
            result = ItemStack(BlockType.BOOKSHELF, 1),
            pattern = listOf(
                listOf(BlockType.WOOD_PLANKS, BlockType.WOOD_PLANKS),
                listOf(BlockType.WOOD_PLANKS, BlockType.WOOD_PLANKS)
            ),
            name = "Bookshelf"
        ),
        // 8 Cobblestone -> Furnace
        CraftingRecipe(
            result = ItemStack(BlockType.FURNACE, 1),
            pattern = listOf(
                listOf(BlockType.COBBLESTONE, BlockType.COBBLESTONE),
                listOf(BlockType.COBBLESTONE, BlockType.COBBLESTONE)
            ),
            name = "Furnace"
        )
    )

    fun findRecipe(grid: List<List<BlockType?>>): ItemStack? {
        for (recipe in RECIPES) {
            if (matches(grid, recipe.pattern)) {
                return recipe.result.copyStack()
            }
        }
        return null
    }

    private fun matches(grid: List<List<BlockType?>>, pattern: List<List<BlockType?>>): Boolean {
        // Find bounding box of non-null items in grid
        var minR = 99; var maxR = -1; var minC = 99; var maxC = -1
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                if (grid[r][c] != null && grid[r][c] != BlockType.AIR) {
                    if (r < minR) minR = r
                    if (r > maxR) maxR = r
                    if (c < minC) minC = c
                    if (c > maxC) maxC = c
                }
            }
        }
        if (maxR == -1) return false // empty grid

        val gridH = maxR - minR + 1
        val gridW = maxC - minC + 1
        val patH = pattern.size
        val patW = pattern[0].size

        if (gridH != patH || gridW != patW) return false

        for (r in 0 until patH) {
            for (c in 0 until patW) {
                val gridBlock = grid[minR + r][minC + c]
                val patBlock = pattern[r][c]
                if (gridBlock != patBlock) return false
            }
        }
        return true
    }
}
