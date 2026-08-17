package com.example.voxel.ui

import android.opengl.GLSurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.voxel.viewmodel.VoxelViewModel

@Composable
fun GameScreen(viewModel: VoxelViewModel) {
    val context = LocalContext.current
    val renderer = viewModel.activeRenderer
    val player = viewModel.activePlayer
    val world = viewModel.activeWorld
    val isInventoryOpen by viewModel.isInventoryOpen.collectAsState()
    val isPauseMenuOpen by viewModel.isPauseMenuOpen.collectAsState()
    val hotbar by viewModel.hotbar.collectAsState()
    val selectedSlot by viewModel.selectedHotbarIndex.collectAsState()

    BackHandler {
        viewModel.togglePauseMenu()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. OpenGL 3D Surface View
        if (renderer != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    GLSurfaceView(context).apply {
                        setEGLContextClientVersion(2)
                        setRenderer(renderer)
                        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                    }
                }
            )
        }

        // 2. Compose In-Game HUD Overlay
        InGameHud(
            viewModel = viewModel,
            onLookDrag = { dx, dy ->
                val sensitivity = 0.22f
                player?.camera?.rotate(dx * sensitivity, -dy * sensitivity)
            },
            onMoveChange = { mx, mz ->
                renderer?.inputMoveX = mx
                renderer?.inputMoveZ = mz
            },
            onJumpState = { isHeld ->
                renderer?.inputJumpHeld = isHeld
                if (isHeld) {
                    viewModel.soundEngine.playJump()
                }
            },
            onSneakState = { isHeld ->
                renderer?.inputSneakHeld = isHeld
            },
            onPlaceBlock = {
                if (world != null && player != null) {
                    val block = hotbar[selectedSlot]
                    player.placeBlock(world, block)
                }
            },
            onMineStart = {
                player?.startBreaking()
            },
            onMineEnd = {
                player?.stopBreaking()
            }
        )

        // 3. Inventory & Crafting Dialog
        if (isInventoryOpen) {
            InventoryDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.toggleInventory() }
            )
        }

        // 4. Pause Menu Dialog
        if (isPauseMenuOpen) {
            PauseMenuDialog(
                viewModel = viewModel,
                onResume = { viewModel.togglePauseMenu() },
                onSaveAndExit = { viewModel.saveAndExitGame() }
            )
        }
    }
}
