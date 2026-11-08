package com.athimue.pleinmalin

import androidx.compose.runtime.Composable
import com.athimue.pleinmalin.presentation.navigation.AppNavigation
import com.athimue.pleinmalin.presentation.theme.PleinMalinTheme

/** Racine partagée de l'UI, appelée par MainActivity (Android) et MainViewController (iOS). */
@Composable
fun App() {
    PleinMalinTheme {
        AppNavigation()
    }
}
