package com.example.aquacontrol.iu.flushing

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aquacontrol.iu.navigation.Routes

@Composable
fun FlushingNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.FLUSHING
    ) {

        // Pantalla principal de Flushing
        composable(Routes.FLUSHING) {
            FlushingHomeScreen(navController)
        }

        // Pantalla de detalle flushing
        composable("flushingDetalle") {
            FlushingDetalleScreen(navController)
        }

        // Registrar flushing
        composable(Routes.REGISTRAR_FLUSHING) {
            RegistrarFlushingScreen(navController)
        }
    }
}

