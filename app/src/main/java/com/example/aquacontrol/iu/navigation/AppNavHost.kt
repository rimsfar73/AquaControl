package com.example.aquacontrol.iu.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.aquacontrol.iu.granjas.GranjaScreen
import com.example.aquacontrol.iu.galpones.GalponScreen
import com.example.aquacontrol.iu.lineas.LineaScreen
import com.example.aquacontrol.iu.detalle.DetalleLineaScreen

@Composable
fun AppNavHost(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Routes.GRANJAS
    ) {

        // Nivel 1: Granjas
        composable(Routes.GRANJAS) {
            GranjaScreen(navController)
        }

        // Nivel 2: Galpones
        composable(Routes.GALPONES) { backStack ->
            val granjaId = backStack.arguments?.getString("granjaId")?.toIntOrNull() ?: 0
            GalponScreen(navController, granjaId)
        }

        // Nivel 3: Líneas
        composable(Routes.LINEAS) { backStack ->
            val galponId = backStack.arguments?.getString("galponId")?.toIntOrNull() ?: 0
            LineaScreen(navController, galponId)
        }

        // Nivel 4: Detalle de Línea
        composable(Routes.DETALLE) { backStack ->
            val galponId = backStack.arguments?.getString("galponId")?.toIntOrNull() ?: 0
            val lineaId = backStack.arguments?.getString("lineaId")?.toIntOrNull() ?: 0
            DetalleLineaScreen(galponId, lineaId)
        }
    }
}
