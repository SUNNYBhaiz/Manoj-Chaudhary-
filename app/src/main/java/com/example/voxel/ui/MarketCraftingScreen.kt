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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storefront
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
import com.example.ui.theme.SleekAccentGreen
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekContainer
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.voxel.model.CraftingManager

@Composable
fun MarketCraftingScreen() {
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
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = SleekOnPrimaryContainer
                )
            }
            Column {
                Text(
                    text = "Crafting Recipes",
                    style = MaterialTheme.typography.titleMedium,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Blueprint encyclopedia & formulas",
                    style = MaterialTheme.typography.bodySmall,
                    color = SleekTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Info Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SleekContainer)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SleekOnPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Craft these items inside the in-game 2x2 or 3x3 workbench grids anytime during gameplay!",
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AVAILABLE BLUEPRINTS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SleekTextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(CraftingManager.RECIPES) { recipe ->
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SleekAccentGreen.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = SleekAccentGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = recipe.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SleekTextPrimary
                                )
                                Text(
                                    text = "Yields ${recipe.result.count}x ${recipe.result.blockType.displayName}",
                                    fontSize = 12.sp,
                                    color = SleekTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SleekContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Craftable",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekOnPrimaryContainer
                            )
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
