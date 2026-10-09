package com.example.aquacontrol.iu.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aquacontrol.iu.alertas.AlertasScreen
import com.example.aquacontrol.iu.components.BottomBar
import com.example.aquacontrol.iu.detalle.DetalleLineaScreen
import com.example.aquacontrol.iu.flushing.FlushingDetalleScreen
import com.example.aquacontrol.iu.flushing.FlushingHomeScreen
import com.example.aquacontrol.iu.flushing.FlushingScreen
import com.example.aquacontrol.iu.galpones.GalponScreen
import com.example.aquacontrol.iu.granjas.GranjaScreen
import com.example.aquacontrol.iu.lineas.LineaScreen
import com.example.aquacontrol.iu.roles.OperarioScreen
import com.example.aquacontrol.iu.roles.RolSelectionScreen
import com.example.aquacontrol.iu.roles.SupervisorScreen
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val perfilViewModel: PerfilViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.ROLE_SELECTION,
            modifier = Modifier.padding(innerPadding)
        ) {

            // Roles
            composable(Routes.ROLE_SELECTION) {
                RolSelectionScreen(navController, perfilViewModel)
            }

            composable(Routes.OPERARIO_HOME) {
                OperarioScreen(navController, perfilViewModel)
            }

            composable(Routes.SUPERVISOR_HOME) {
                SupervisorScreen(navController, perfilViewModel)
            }

            // Granjas
            composable(Routes.GRANJAS) {
                GranjaScreen(navController)
            }

            // Galpones de la granja seleccionada
            composable(Routes.GALPONES_PARAM) { backStack ->
                val granjaId = backStack.arguments
                    ?.getString("granjaId")
                    ?.toIntOrNull() ?: 0

                GalponScreen(navController, granjaId)
            }

            // Líneas del galpón seleccionado
            composable(Routes.LINEAS_PARAM) { backStack ->
                val galponId = backStack.arguments
                    ?.getString("galponId")
                    ?.toIntOrNull() ?: 0

                LineaScreen(navController, galponId)
            }

            // Detalle de la línea seleccionada
            composable(Routes.DETALLE_LINEA_PARAM) { backStack ->
                val lineaId = backStack.arguments
                    ?.getString("lineaId")
                    ?.toIntOrNull() ?: 0

                DetalleLineaScreen(navController, lineaId)
            }

            // Historial de flushing por línea
            composable(Routes.FLUSHING_PARAM) { backStack ->
                val lineaId = backStack.arguments
                    ?.getString("lineaId")
                    ?.toIntOrNull() ?: 0

                FlushingScreen(navController, lineaId)
            }

            // Alertas
            composable(Routes.ALERTAS) {
                AlertasScreen(navController)
            }

            // Flushing general
            composable(Routes.FLUSHING) {
                FlushingHomeScreen(navController)
            }

            // Detalle de flushing
            composable("flushingDetalle/{lineaId}") { backStack ->
                val lineaId = backStack.arguments
                    ?.getString("lineaId")
                    ?.toIntOrNull() ?: 0

                FlushingDetalleScreen(navController, lineaId)
            }
        }
    }
}