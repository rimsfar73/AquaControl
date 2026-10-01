package com.example.aquacontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.aquacontrol.ui.theme.AquaControlTheme
import com.example.aquacontrol.iu.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AquaControlTheme {
                AppNavHost()   // Navegación completa de AquaControl 3.0
            }
        }
    }
}
