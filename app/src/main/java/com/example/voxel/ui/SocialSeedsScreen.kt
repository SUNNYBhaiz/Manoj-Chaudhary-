package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekAccentBlue
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.engine.physics.GameMode
import com.example.voxel.engine.world.WorldType
import com.example.voxel.viewmodel.VoxelViewModel

data class SeedPreset(
    val title: String,
    val description: String,
    val seed: Long,
    val worldType: WorldType,
    val tag: String
)

@Composable
fun SocialSeedsScreen(viewModel: VoxelViewModel) {
    val presets = listOf(
        SeedPreset(
            title = "Emerald Peaks & Alpine Valleys",
            description = "Towering alpine mountain ranges with lush pine trees, deep stone ravines and coal veins.",
            seed = 987654321L,
            worldType = WorldType.EXTREME_MOUNTAINS,
            tag = "Mountainous"
        ),
        SeedPreset(
            title = "Golden Desert & Sand Dunes",
            description = "Expansive sun-baked dunes with cacti, sandstone cliffs, and flat desert expanses.",
            seed = 554433221L,
            worldType = WorldType.DESERT_OASIS,
            tag = "Desert"
        ),
        SeedPreset(
            title = "Twin Lakes & Flower Fields",
            description = "Gentle rolling plains filled with red roses, yellow dandelions, birch groves, and calm ponds.",
            seed = 133742069L,
            worldType = WorldType.PLAINS_AND_HILLS,
            tag = "Scenic"
        ),
        SeedPreset(
            title = "Architect Flatland Arena",
            description = "A pristine flat bedrock & grass plane perfect for mega structures and TNT physics.",
            seed = 424242L,
            worldType = WorldType.FLAT_CREATIVE,
            tag = "Creative"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = SleekOnPrimaryContainer
                )
            }
            Column {
                Text(
                    text = "Seed Discoveries",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Featured procedural world seeds",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "FEATURED SEED REALMS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SleekTextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(presets) { preset ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(SleekSurface)
                        .border(1.dp, SleekBorder.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SleekTextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SleekAccentBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = preset.tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SleekAccentBlue
                                )
                            }
                        }

                        Text(
                            text = preset.description,
                            fontSize = 12.sp,
                            color = SleekTextSecondary,
                            lineHeight = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Seed: ${preset.seed}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SleekTextSecondary
                            )

                            Button(
                                onClick = {
                                    viewModel.createWorld(
                                        name = preset.title,
                                        seed = preset.seed,
                                        worldType = preset.worldType,
                                        gameMode = GameMode.CREATIVE
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Generate",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
