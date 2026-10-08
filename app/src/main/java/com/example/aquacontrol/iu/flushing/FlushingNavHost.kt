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

        // Pantalla de detalle de flushing (requiere lineaId)
        composable("flushingDetalle/{lineaId}") { backStack ->
            val lineaId = backStack.arguments?.getString("lineaId")?.toIntOrNull() ?: 0
            FlushingDetalleScreen(navController, lineaId)
        }
    }
}
