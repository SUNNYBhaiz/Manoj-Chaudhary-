package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekContainerHigh
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.engine.physics.GameMode
import com.example.voxel.engine.world.WorldType
import java.util.Random

@Composable
fun CreateWorldDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, seed: Long, worldType: WorldType, gameMode: GameMode) -> Unit
) {
    var name by remember { mutableStateOf("New Realm") }
    var seedText by remember { mutableStateOf(Random().nextInt(999999).toString()) }
    var selectedWorldType by remember { mutableStateOf(WorldType.PLAINS_AND_HILLS) }
    var selectedGameMode by remember { mutableStateOf(GameMode.CREATIVE) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SleekSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create New World",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                // World Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("World Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_world_name"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    )
                )

                // Seed
                OutlinedTextField(
                    value = seedText,
                    onValueChange = { seedText = it },
                    label = { Text("World Seed (Number)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_world_seed"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder
                    )
                )

                // Game Mode Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "GAME MODE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameModeOption(
                            title = "Creative",
                            subtitle = "Unlimited blocks & flight",
                            isSelected = selectedGameMode == GameMode.CREATIVE,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedGameMode = GameMode.CREATIVE }
                        )
                        GameModeOption(
                            title = "Survival",
                            subtitle = "Hearts & block mining",
                            isSelected = selectedGameMode == GameMode.SURVIVAL,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedGameMode = GameMode.SURVIVAL }
                        )
                    }
                }

                // World Type Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "WORLD TERRAIN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TerrainTypeChip(
                            label = "Plains",
                            isSelected = selectedWorldType == WorldType.PLAINS_AND_HILLS,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedWorldType = WorldType.PLAINS_AND_HILLS }
                        )
                        TerrainTypeChip(
                            label = "Mountains",
                            isSelected = selectedWorldType == WorldType.EXTREME_MOUNTAINS,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedWorldType = WorldType.EXTREME_MOUNTAINS }
                        )
                        TerrainTypeChip(
                            label = "Desert",
                            isSelected = selectedWorldType == WorldType.DESERT_OASIS,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedWorldType = WorldType.DESERT_OASIS }
                        )
                        TerrainTypeChip(
                            label = "Flat",
                            isSelected = selectedWorldType == WorldType.FLAT_CREATIVE,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedWorldType = WorldType.FLAT_CREATIVE }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SleekTextSecondary)
                    }
                    Button(
                        onClick = {
                            val seed = seedText.toLongOrNull() ?: Random().nextLong()
                            onCreate(name, seed, selectedWorldType, selectedGameMode)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("btn_confirm_create_world")
                    ) {
                        Text("Create & Play", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GameModeOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SleekContainerHigh else SleekContainer.copy(alpha = 0.5f))
            .border(
                1.5.dp,
                if (isSelected) SleekPrimary else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SleekTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SleekTextSecondary
            )
        }
    }
}

@Composable
fun TerrainTypeChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SleekPrimary else SleekContainer)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else SleekTextPrimary
        )
    }
}
