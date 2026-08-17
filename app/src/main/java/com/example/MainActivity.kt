package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.theme.MyApplicationTheme
import com.example.voxel.ui.MainScreen
import com.example.voxel.viewmodel.VoxelViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VoxelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Keep screen on while playing the 3D voxel world
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveCurrentWorldState()
    }
}
