package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekAccentAmber
import com.example.ui.theme.SleekAccentGreen
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekContainerHigh
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.data.WorldEntity
import com.example.voxel.viewmodel.VoxelViewModel

@Composable
fun LobbyScreen(
    viewModel: VoxelViewModel,
    onCreateWorldClick: () -> Unit
) {
    val worlds by viewModel.allWorlds.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val lastWorld = worlds.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Emerald Green Logo Badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekAccentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "V",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "VoxelCraft",
                            style = MaterialTheme.typography.titleMedium,
                            color = SleekTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "v1.20.4 • Stable",
                            style = MaterialTheme.typography.bodySmall,
                            color = SleekTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Profile Avatar Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SleekContainerHigh)
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = SleekOnPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero World Card
                item {
                    lastWorld?.let { world ->
                        HeroWorldCard(
                            world = world,
                            onPlay = { viewModel.launchGame(world) }
                        )
                    }
                }

                // 2. Your Worlds Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "YOUR WORLDS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${worlds.size} Worlds",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekPrimary
                        )
                    }
                }

                // 3. Worlds List
                items(worlds, key = { it.id }) { world ->
                    WorldItemCard(
                        world = world,
                        onPlay = { viewModel.launchGame(world) },
                        onDelete = { viewModel.deleteWorld(world) }
                    )
                }

                // 4. Performance Engine Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(SleekContainer)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = SleekOnPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Performance Engine",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SleekOnPrimaryContainer
                                    )
                                }

                                Switch(
                                    checked = settings.isPerformanceEngineEnabled,
                                    onCheckedChange = { viewModel.togglePerformanceEngine() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = SleekPrimary,
                                        uncheckedThumbColor = SleekTextSecondary,
                                        uncheckedTrackColor = SleekSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("performance_switch")
                                )
                            }

                            Text(
                                text = "Optimized for mobile hardware. Dynamic chunk throttling and smooth 60 FPS voxel meshing enabled.",
                                fontSize = 12.sp,
                                color = SleekTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Floating Create Button
        FloatingActionButton(
            onClick = onCreateWorldClick,
            containerColor = SleekPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("create_world_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create World")
                Text(text = "New World", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HeroWorldCard(
    world: WorldEntity,
    onPlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(28.dp))
            .border(2.dp, Color.White, RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF388E3C),
                        Color(0xFF1B5E20),
                        Color(0xFF0D3311)
                    )
                )
            )
            .clickable { onPlay() }
            .testTag("hero_world_card")
    ) {
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0x99000000), Color(0xEE000000))
                    )
                )
        )

        // World details
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = "Last Played",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${world.gameMode.lowercase().replaceFirstChar { it.uppercase() }}: ${world.name} • (${world.playerX.toInt()}, ${world.playerY.toInt()}, ${world.playerZ.toInt()})",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )
        }

        // Play Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SleekContainerHigh)
                .clickable { onPlay() }
                .testTag("hero_play_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play World",
                tint = SleekOnPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun WorldItemCard(
    world: WorldEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val badgeColor = when (world.gameMode) {
        "SURVIVAL" -> SleekAccentAmber
        else -> SleekAccentGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable { onPlay() }
            .padding(12.dp)
            .testTag("world_item_${world.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Landscape,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = world.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SleekTextPrimary
                )
                Text(
                    text = "${world.worldType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }} • Seed: ${world.seed}",
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )
                Text(
                    text = "Pos: (${world.playerX.toInt()}, ${world.playerY.toInt()}, ${world.playerZ.toInt()}) • ${world.gameMode.lowercase().replaceFirstChar { it.uppercase() }}",
                    fontSize = 11.sp,
                    color = SleekPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = SleekTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(SleekSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play World", color = SleekTextPrimary) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = SleekPrimary) },
                        onClick = {
                            menuExpanded = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete World", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
