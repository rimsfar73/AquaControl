package com.example.aquacontrol.iu.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier

import com.example.aquacontrol.iu.components.BottomBar
import com.example.aquacontrol.iu.roles.RolSelectionScreen
import com.example.aquacontrol.iu.roles.OperarioScreen
import com.example.aquacontrol.iu.roles.SupervisorScreen
import com.example.aquacontrol.iu.granjas.GranjaScreen
import com.example.aquacontrol.iu.galpones.GalponScreen
import com.example.aquacontrol.iu.lineas.LineaScreen
import com.example.aquacontrol.iu.detalle.DetalleLineaScreen
import com.example.aquacontrol.iu.flushing.FlushingScreen   // ← NUEVO

@Composable
fun AppNavHost() {

    val navController: NavHostController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.ROLE_SELECTION,
            modifier = Modifier.padding(innerPadding)
        ) {

            // --- Nivel 0: Selección de Rol ---
            composable(Routes.ROLE_SELECTION) {
                RolSelectionScreen(navController)
            }

            // --- Nivel 1: Operario ---
            composable(Routes.OPERARIO_HOME) {
                OperarioScreen(navController)
            }

            // --- Nivel 2: Supervisor ---
            composable(Routes.SUPERVISOR_HOME) {
                SupervisorScreen(navController)
            }

            // --- Nivel 3: Granjas ---
            composable(Routes.GRANJAS) {
                GranjaScreen(navController)
            }

            // --- Nivel 4: Galpones ---
            composable(Routes.GALPONES) {
                GalponScreen(navController)
            }

            // --- Nivel 5: Líneas ---
            composable(Routes.LINEAS) {
                LineaScreen(navController)
            }

            // --- Nivel 6: Detalle de Línea ---
            composable("${Routes.DETALLE_LINEA}/{lineaId}") { backStack ->
                val lineaId = backStack.arguments?.getString("lineaId")?.toIntOrNull() ?: 0
                DetalleLineaScreen(navController, lineaId)
            }

            // --- Nivel 7: Flushing ---
            composable(Routes.FLUSHING) {
                FlushingScreen(navController)
            }
        }
    }
}
