package com.athimue.pleinmalin

import androidx.compose.ui.window.ComposeUIViewController
import com.athimue.pleinmalin.di.initKoin
import platform.UIKit.UIViewController

/** Appelée depuis Swift : MainViewControllerKt.MainViewController() */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }

/** Appelée une seule fois au lancement, depuis iOSApp.init : MainViewControllerKt.setupKoin() */
fun setupKoin() = initKoin()
