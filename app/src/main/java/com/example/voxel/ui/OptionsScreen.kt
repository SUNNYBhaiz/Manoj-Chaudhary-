package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.viewmodel.VoxelViewModel

@Composable
fun OptionsScreen(viewModel: VoxelViewModel) {
    val settings by viewModel.settings.collectAsState()

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
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = SleekOnPrimaryContainer
                )
            }
            Column {
                Text(
                    text = "Game Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Graphics, Audio & Simulation Preferences",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Graphics Section
            item {
                Text(
                    text = "GRAPHICS & DISPLAY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            item {
                SettingsSliderCard(
                    icon = Icons.Default.Visibility,
                    title = "Render Distance",
                    subtitle = "${settings.renderDistance} Chunks (${settings.renderDistance * 16} blocks radius)",
                    value = settings.renderDistance.toFloat(),
                    valueRange = 2f..8f,
                    steps = 5,
                    onValueChange = {
                        viewModel.updateSettings(settings.copy(renderDistance = it.toInt()))
                    }
                )
            }

            item {
                SettingsSliderCard(
                    icon = Icons.Default.Palette,
                    title = "Field of View (FOV)",
                    subtitle = "${settings.fov.toInt()}°",
                    value = settings.fov,
                    valueRange = 60f..100f,
                    steps = 7,
                    onValueChange = {
                        viewModel.updateSettings(settings.copy(fov = it))
                    }
                )
            }

            item {
                SettingsToggleCard(
                    title = "Dynamic Day/Night Cycle",
                    subtitle = "Celestial sun/moon rotation and distance fog blending",
                    checked = settings.isDayNightCycle,
                    onCheckedChange = {
                        viewModel.updateSettings(settings.copy(isDayNightCycle = it))
                    }
                )
            }

            // Audio Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "AUDIO & SOUND FX",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextSecondary,
                    letterSpacing = 1.sp
                )
            }

            item {
                SettingsSliderCard(
                    icon = Icons.Default.VolumeUp,
                    title = "Sound FX Volume",
                    subtitle = "${(settings.soundVolume * 100).toInt()}%",
                    value = settings.soundVolume,
                    valueRange = 0f..1f,
                    steps = 9,
                    onValueChange = {
                        viewModel.updateSettings(settings.copy(soundVolume = it))
                    }
                )
            }

            item {
                SettingsSliderCard(
                    icon = Icons.Default.MusicNote,
                    title = "Ambient Procedural Music",
                    subtitle = "${(settings.musicVolume * 100).toInt()}%",
                    value = settings.musicVolume,
                    valueRange = 0f..1f,
                    steps = 9,
                    onValueChange = {
                        viewModel.updateSettings(settings.copy(musicVolume = it))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SettingsSliderCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SleekTextPrimary
                    )
                }

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SleekPrimary
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = SleekPrimary,
                    activeTrackColor = SleekPrimary,
                    inactiveTrackColor = SleekContainer
                )
            )
        }
    }
}

@Composable
fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SleekSurface)
            .border(1.dp, SleekBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SleekTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SleekTextSecondary
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SleekPrimary,
                    uncheckedThumbColor = SleekTextSecondary,
                    uncheckedTrackColor = SleekSurfaceVariant
                )
            )
        }
    }
}
