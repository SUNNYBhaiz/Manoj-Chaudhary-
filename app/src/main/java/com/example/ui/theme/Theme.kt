package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SleekDarkPrimary,
    onPrimary = SleekDarkOnPrimary,
    primaryContainer = SleekPrimary,
    onPrimaryContainer = SleekPrimaryContainer,
    secondary = SleekSecondary,
    secondaryContainer = SleekDarkSurfaceVariant,
    onSecondaryContainer = SleekDarkPrimary,
    background = SleekDarkBackground,
    surface = SleekDarkSurface,
    surfaceVariant = SleekDarkSurfaceVariant,
    onBackground = SleekBackground,
    onSurface = SleekBackground,
    outline = SleekTextTertiary
)

private val LightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    onPrimary = SleekOnPrimary,
    primaryContainer = SleekPrimaryContainer,
    onPrimaryContainer = SleekOnPrimaryContainer,
    secondary = SleekSecondary,
    secondaryContainer = SleekSecondaryContainer,
    onSecondaryContainer = SleekOnSecondaryContainer,
    background = SleekBackground,
    surface = SleekSurface,
    surfaceVariant = SleekSurfaceVariant,
    onBackground = SleekTextPrimary,
    onSurface = SleekTextPrimary,
    outline = SleekBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to clean sleek light theme matching the design
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
