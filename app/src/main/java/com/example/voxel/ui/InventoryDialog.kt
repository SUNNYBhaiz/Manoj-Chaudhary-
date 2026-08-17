package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SleekAccentGreen
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekContainerHigh
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.viewmodel.VoxelViewModel

@Composable
fun InventoryDialog(
    viewModel: VoxelViewModel,
    onDismiss: () -> Unit
) {
    val hotbar by viewModel.hotbar.collectAsState()
    val selectedIndex by viewModel.selectedHotbarIndex.collectAsState()
    val craftingGrid by viewModel.craftingGrid.collectAsState()
    val craftingResult by viewModel.craftingResult.collectAsState()

    val allCreatableBlocks = listOf(
        BlockType.GRASS, BlockType.DIRT, BlockType.STONE, BlockType.COBBLESTONE,
        BlockType.WOOD_OAK, BlockType.WOOD_BIRCH, BlockType.WOOD_PLANKS, BlockType.LEAVES,
        BlockType.SAND, BlockType.GLASS, BlockType.BRICK, BlockType.COAL_ORE,
        BlockType.IRON_ORE, BlockType.GOLD_ORE, BlockType.DIAMOND_ORE, BlockType.TNT,
        BlockType.TORCH, BlockType.SNOW, BlockType.CACTUS, BlockType.GLOWSTONE,
        BlockType.OBSIDIAN, BlockType.BOOKSHELF, BlockType.CRAFTING_TABLE, BlockType.FURNACE,
        BlockType.FLOWER_RED, BlockType.FLOWER_YELLOW
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SleekSurface,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Inventory & Crafting",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "Select blocks for hotbar or combine resources",
                            fontSize = 12.sp,
                            color = SleekTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SleekOnPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 2x2 Crafting Bench Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SleekContainer)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // 2x2 Input Grid
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CraftingSlot(craftingGrid[0], onClick = { viewModel.setCraftingSlot(0, null) })
                                CraftingSlot(craftingGrid[1], onClick = { viewModel.setCraftingSlot(1, null) })
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CraftingSlot(craftingGrid[2], onClick = { viewModel.setCraftingSlot(2, null) })
                                CraftingSlot(craftingGrid[3], onClick = { viewModel.setCraftingSlot(3, null) })
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Yields",
                            tint = SleekOnPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )

                        // Output slot
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (craftingResult != null) SleekAccentGreen.copy(alpha = 0.2f) else SleekSurface)
                                .border(
                                    2.dp,
                                    if (craftingResult != null) SleekAccentGreen else SleekBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(enabled = craftingResult != null) {
                                    viewModel.takeCraftingResult()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (craftingResult != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    BlockColorThumb(craftingResult!!.blockType, size = 26.dp)
                                    Text(
                                        text = "x${craftingResult!!.count}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTextPrimary
                                    )
                                }
                            } else {
                                Text("Empty", fontSize = 10.sp, color = SleekTextSecondary)
                            }
                        }
                    }
                }

                Text(
                    text = "BLOCK PALETTE (TAP TO ASSIGN TO SELECTED SLOT)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextSecondary,
                    letterSpacing = 0.8.sp
                )

                // Block selection grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 58.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allCreatableBlocks) { block ->
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SleekSurface)
                                .border(1.dp, SleekBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .clickable {
                                    // Put in hotbar slot
                                    viewModel.setHotbarSlot(selectedIndex, block)
                                    // Also put in first empty crafting slot
                                    val emptySlot = craftingGrid.indexOfFirst { it == null }
                                    if (emptySlot != -1) {
                                        viewModel.setCraftingSlot(emptySlot, block)
                                    }
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                BlockColorThumb(block, size = 26.dp)
                                Text(
                                    text = block.displayName,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SleekTextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Active Hotbar Preview
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "HOTBAR SLOTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        hotbar.forEachIndexed { index, block ->
                            val isSelected = index == selectedIndex
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SleekContainerHigh else SleekSurface)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) SleekPrimary else SleekBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.selectHotbarSlot(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                BlockColorThumb(block, size = 20.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CraftingSlot(block: BlockType?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (block != null) {
            BlockColorThumb(block, size = 24.dp)
        } else {
            Text("+", fontSize = 16.sp, color = SleekTextSecondary)
        }
    }
}

@Composable
fun BlockColorThumb(block: BlockType, size: androidx.compose.ui.unit.Dp = 24.dp) {
    val color = when (block) {
        BlockType.GRASS -> Color(0xFF4CAF50)
        BlockType.DIRT -> Color(0xFF795548)
        BlockType.STONE -> Color(0xFF9E9E9E)
        BlockType.COBBLESTONE -> Color(0xFF757575)
        BlockType.WOOD_OAK, BlockType.WOOD_PLANKS -> Color(0xFF8D6E63)
        BlockType.WOOD_BIRCH -> Color(0xFFE0E0E0)
        BlockType.LEAVES -> Color(0xFF2E7D32)
        BlockType.SAND -> Color(0xFFFFD54F)
        BlockType.GLASS -> Color(0xFF81D4FA)
        BlockType.WATER -> Color(0xFF29B6F6)
        BlockType.COAL_ORE -> Color(0xFF424242)
        BlockType.IRON_ORE -> Color(0xFFBCAAA4)
        BlockType.GOLD_ORE -> Color(0xFFFFC107)
        BlockType.DIAMOND_ORE -> Color(0xFF00E5FF)
        BlockType.BRICK -> Color(0xFFD32F2F)
        BlockType.TNT -> Color(0xFFF44336)
        BlockType.SNOW -> Color(0xFFECEFF1)
        BlockType.CACTUS -> Color(0xFF388E3C)
        BlockType.GLOWSTONE -> Color(0xFFFFEE58)
        BlockType.OBSIDIAN -> Color(0xFF311B92)
        BlockType.TORCH -> Color(0xFFFF9800)
        BlockType.FLOWER_RED -> Color(0xFFE91E63)
        BlockType.FLOWER_YELLOW -> Color(0xFFFFEB3B)
        else -> Color(0xFF9E9E9E)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
    )
}
