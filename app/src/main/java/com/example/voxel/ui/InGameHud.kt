package com.example.voxel.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekAccentAmber
import com.example.ui.theme.SleekAccentGreen
import com.example.ui.theme.SleekAccentRed
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekContainerHigh
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.engine.blocks.BlockType
import com.example.voxel.engine.physics.GameMode
import com.example.voxel.viewmodel.VoxelViewModel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun InGameHud(
    viewModel: VoxelViewModel,
    onLookDrag: (deltaX: Float, deltaY: Float) -> Unit,
    onMoveChange: (moveX: Float, moveZ: Float) -> Unit,
    onJumpState: (isHeld: Boolean) -> Unit,
    onSneakState: (isHeld: Boolean) -> Unit,
    onPlaceBlock: () -> Unit,
    onMineStart: () -> Unit,
    onMineEnd: () -> Unit
) {
    val player = viewModel.activePlayer
    val renderer = viewModel.activeRenderer
    val hotbar by viewModel.hotbar.collectAsState()
    val selectedSlot by viewModel.selectedHotbarIndex.collectAsState()
    val isFlying = player?.isFlying == true
    val isSurvival = player?.gameMode == GameMode.SURVIVAL

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Right 70% Touch Area for Camera Look Rotation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onLookDrag(dragAmount.x, dragAmount.y)
                    }
                }
        )

        // 2. Crosshair in screen center
        Canvas(
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
        ) {
            val stroke = 2.dp.toPx()
            val len = 8.dp.toPx()
            val cx = size.width / 2
            val cy = size.height / 2

            // Horizontal line
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(cx - len, cy),
                end = Offset(cx + len, cy),
                strokeWidth = stroke
            )
            // Vertical line
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(cx, cy - len),
                end = Offset(cx, cy + len),
                strokeWidth = stroke
            )
        }

        // 3. Top Header Bar (Sleek Glass Pill)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekSurface.copy(alpha = 0.88f))
                    .border(1.dp, SleekBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Time indicator icon
                    val isDay = (renderer?.timeOfDay ?: 0.25f) in 0.15f..0.65f
                    Icon(
                        imageVector = if (isDay) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                        contentDescription = null,
                        tint = if (isDay) SleekAccentAmber else SleekPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    Column {
                        val posX = player?.posX?.roundToInt() ?: 0
                        val posY = player?.posY?.roundToInt() ?: 0
                        val posZ = player?.posZ?.roundToInt() ?: 0
                        Text(
                            text = "XYZ: $posX, $posY, $posZ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                        Text(
                            text = "${renderer?.currentFps ?: 60} FPS • ${renderer?.renderedChunkCount ?: 0} Chunks",
                            fontSize = 10.sp,
                            color = SleekTextSecondary
                        )
                    }
                }

                // Action icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Inventory / Backpack Button
                    IconButton(
                        onClick = { viewModel.toggleInventory() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekContainer)
                            .testTag("btn_open_inventory")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Inventory",
                            tint = SleekOnPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Pause Menu Button
                    IconButton(
                        onClick = { viewModel.togglePauseMenu() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SleekContainer)
                            .testTag("btn_pause_game")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Pause",
                            tint = SleekOnPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 4. Survival Hearts Bar (if in survival mode)
        if (isSurvival) {
            val health = player?.health ?: 20
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 76.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SleekSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 10) {
                        val isFull = health >= (i + 1) * 2
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (isFull) SleekAccentRed else SleekBorder,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 5. Virtual Movement Joystick (Bottom-Left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 86.dp)
        ) {
            VirtualJoystick(onMove = onMoveChange)
        }

        // 6. Action Control Buttons (Bottom-Right)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 86.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Mine / Break Block Button
                    HoldActionButton(
                        icon = Icons.Default.Remove,
                        label = "Mine",
                        containerColor = SleekAccentRed.copy(alpha = 0.85f),
                        onDown = onMineStart,
                        onUp = onMineEnd,
                        testTag = "btn_mine_block"
                    )

                    // Place Block Button
                    TapActionButton(
                        icon = Icons.Default.Add,
                        label = "Place",
                        containerColor = SleekAccentGreen.copy(alpha = 0.9f),
                        onClick = onPlaceBlock,
                        testTag = "btn_place_block"
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Flight Toggle (Creative)
                    if (player?.gameMode == GameMode.CREATIVE) {
                        TapActionButton(
                            icon = Icons.Default.Flight,
                            label = if (isFlying) "Land" else "Fly",
                            containerColor = if (isFlying) SleekPrimary else SleekContainer,
                            contentColor = if (isFlying) Color.White else SleekOnPrimaryContainer,
                            onClick = { player.toggleFlight() },
                            testTag = "btn_toggle_flight"
                        )
                    }

                    // Sneak / Descend Button
                    HoldActionButton(
                        icon = Icons.Default.KeyboardArrowDown,
                        label = "Sneak",
                        containerColor = SleekSurface.copy(alpha = 0.85f),
                        contentColor = SleekTextPrimary,
                        onDown = { onSneakState(true) },
                        onUp = { onSneakState(false) },
                        testTag = "btn_sneak"
                    )

                    // Jump / Ascend Button
                    HoldActionButton(
                        icon = Icons.Default.KeyboardArrowUp,
                        label = "Jump",
                        containerColor = SleekPrimary.copy(alpha = 0.9f),
                        contentColor = Color.White,
                        onDown = { onJumpState(true) },
                        onUp = { onJumpState(false) },
                        testTag = "btn_jump"
                    )
                }
            }
        }

        // 7. Sleek Hotbar Bar (Bottom Center)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SleekSurface.copy(alpha = 0.92f))
                    .border(1.dp, SleekBorder.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                hotbar.forEachIndexed { index, block ->
                    val isSelected = index == selectedSlot
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) SleekContainerHigh else Color.Transparent)
                            .border(
                                2.dp,
                                if (isSelected) SleekPrimary else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.selectHotbarSlot(index) }
                            .testTag("hotbar_slot_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        BlockColorThumb(block, size = 26.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun VirtualJoystick(onMove: (x: Float, z: Float) -> Unit) {
    val maxRadius = 55f
    var knobOffsetX by remember { mutableFloatStateOf(0f) }
    var knobOffsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(SleekSurface.copy(alpha = 0.75f))
            .border(1.5.dp, SleekBorder.copy(alpha = 0.6f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        knobOffsetX = 0f
                        knobOffsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffsetX = 0f
                        knobOffsetY = 0f
                        onMove(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = knobOffsetX + dragAmount.x
                        val newY = knobOffsetY + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)
                        if (dist > maxRadius) {
                            knobOffsetX = (newX / dist) * maxRadius
                            knobOffsetY = (newY / dist) * maxRadius
                        } else {
                            knobOffsetX = newX
                            knobOffsetY = newY
                        }

                        val normX = knobOffsetX / maxRadius
                        val normZ = -knobOffsetY / maxRadius // Forward is -Y on screen
                        onMove(normX, normZ)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Center Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffsetX.roundToInt(), knobOffsetY.roundToInt()) }
                .size(46.dp)
                .clip(CircleShape)
                .background(SleekPrimary.copy(alpha = 0.85f))
                .border(2.dp, Color.White, CircleShape)
        )
    }
}

@Composable
fun TapActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color = Color.White,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, SleekBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun HoldActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color = Color.White,
    onDown: () -> Unit,
    onUp: () -> Unit,
    testTag: String
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPressed) containerColor.copy(alpha = 1.0f) else containerColor)
            .border(
                1.5.dp,
                if (isPressed) Color.White else SleekBorder.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onDown()
                        tryAwaitRelease()
                        isPressed = false
                        onUp()
                    }
                )
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
