package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.viewmodel.VoxelViewModel

@Composable
fun PauseMenuDialog(
    viewModel: VoxelViewModel,
    onResume: () -> Unit,
    onSaveAndExit: () -> Unit
) {
    val worldName = viewModel.activeWorldEntity.value?.name ?: "Voxel Realm"

    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SleekSurface,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Game Paused",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                Text(
                    text = worldName,
                    fontSize = 13.sp,
                    color = SleekTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Resume
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_resume_game"),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text("Resume Game", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // Respawn
                OutlinedButton(
                    onClick = {
                        viewModel.activePlayer?.respawn()
                        onResume()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = SleekPrimary)
                        Text("Respawn at Origin", color = SleekPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Save and Quit
                Button(
                    onClick = onSaveAndExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_save_exit"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekContainer,
                        contentColor = SleekTextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Text("Save & Exit to Lobby", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
