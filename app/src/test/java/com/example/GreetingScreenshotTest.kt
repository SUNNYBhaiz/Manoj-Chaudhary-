package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.example.voxel.data.WorldEntity
import com.example.voxel.ui.WorldItemCard
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun world_item_card_screenshot() {
        val sampleWorld = WorldEntity(
            id = 1,
            name = "Survival Realm",
            seed = 987654L,
            worldType = "PLAINS_AND_HILLS",
            gameMode = "SURVIVAL",
            playerX = 14f,
            playerY = 42f,
            playerZ = 8f
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                WorldItemCard(
                    world = sampleWorld,
                    onPlay = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/world_item.png")
    }
}

