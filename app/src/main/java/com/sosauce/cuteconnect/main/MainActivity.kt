package com.sosauce.cuteconnect.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.sosauce.cuteconnect.ui.navigation.Nav
import com.sosauce.cuteconnect.ui.screens.setup.SetupScreen
import com.sosauce.cuteconnect.ui.theme.CuteConnectTheme
import com.sosauce.cuteconnect.utils.hasBothRoles

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            CuteConnectTheme {

                WindowCompat
                .getInsetsController(window, window.decorView)
                .apply {
                    isAppearanceLightStatusBars = !isSystemInDarkTheme()
                    isAppearanceLightNavigationBars = !isSystemInDarkTheme()
                }

                var hasBothRoles by remember { mutableStateOf(hasBothRoles()) }
                if (hasBothRoles) {
                    Nav(intent = intent)
                } else {
                    SetupScreen { hasBothRoles = hasBothRoles() }
                }
            }
        }
    }

}

