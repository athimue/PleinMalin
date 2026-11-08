package com.athimue.pleinmalin.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Vert "bon plan" pour le prix, ambre pour les accents.
private val LightColors = lightColorScheme(
    primary = Color(0xFF1B6C3A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA6F4B5),
    onPrimaryContainer = Color(0xFF00210C),
    secondary = Color(0xFF8A5100),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCBE),
    onSecondaryContainer = Color(0xFF2C1600),
    background = Color(0xFFF7FBF4),
    surface = Color(0xFFF7FBF4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BD89B),
    onPrimary = Color(0xFF003919),
    primaryContainer = Color(0xFF005227),
    onPrimaryContainer = Color(0xFFA6F4B5),
    secondary = Color(0xFFFFB870),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF693C00),
    onSecondaryContainer = Color(0xFFFFDCBE),
    background = Color(0xFF101510),
    surface = Color(0xFF101510),
)

@Composable
fun PleinMalinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
