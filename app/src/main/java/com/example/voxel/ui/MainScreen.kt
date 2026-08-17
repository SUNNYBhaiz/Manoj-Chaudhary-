package com.example.voxel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.viewmodel.AppTab
import com.example.voxel.viewmodel.VoxelViewModel

@Composable
fun MainScreen(viewModel: VoxelViewModel) {
    val isPlaying by viewModel.isPlayingGame.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    if (isPlaying) {
        GameScreen(viewModel = viewModel)
    } else {
        Scaffold(
            bottomBar = {
                SleekBottomNavigation(
                    selectedTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            },
            containerColor = SleekBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.LOBBY -> LobbyScreen(
                        viewModel = viewModel,
                        onCreateWorldClick = { showCreateDialog = true }
                    )
                    AppTab.MARKET -> MarketCraftingScreen()
                    AppTab.SOCIAL -> SocialSeedsScreen(viewModel = viewModel)
                    AppTab.OPTIONS -> OptionsScreen(viewModel = viewModel)
                }

                if (showCreateDialog) {
                    CreateWorldDialog(
                        onDismiss = { showCreateDialog = false },
                        onCreate = { name, seed, worldType, gameMode ->
                            showCreateDialog = false
                            viewModel.createWorld(name, seed, worldType, gameMode)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SleekBottomNavigation(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekSurfaceVariant)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f), thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SleekNavItem(
                icon = Icons.Default.GridView,
                label = "Lobby",
                isSelected = selectedTab == AppTab.LOBBY,
                onClick = { onTabSelected(AppTab.LOBBY) },
                testTag = "nav_tab_lobby"
            )
            SleekNavItem(
                icon = Icons.Default.Storefront,
                label = "Market",
                isSelected = selectedTab == AppTab.MARKET,
                onClick = { onTabSelected(AppTab.MARKET) },
                testTag = "nav_tab_market"
            )
            SleekNavItem(
                icon = Icons.Default.Groups,
                label = "Social",
                isSelected = selectedTab == AppTab.SOCIAL,
                onClick = { onTabSelected(AppTab.SOCIAL) },
                testTag = "nav_tab_social"
            )
            SleekNavItem(
                icon = Icons.Default.Settings,
                label = "Options",
                isSelected = selectedTab == AppTab.OPTIONS,
                onClick = { onTabSelected(AppTab.OPTIONS) },
                testTag = "nav_tab_options"
            )
        }
    }
}

@Composable
fun SleekNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) SleekContainer else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) SleekTextPrimary else SleekTextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) SleekTextPrimary else SleekTextSecondary
        )
    }
}
