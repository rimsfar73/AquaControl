package com.example.aquacontrol.iu.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.aquacontrol.iu.components.BottomBar
import com.example.aquacontrol.iu.roles.RolSelectionScreen
import com.example.aquacontrol.iu.roles.OperarioScreen
import com.example.aquacontrol.iu.roles.SupervisorScreen
import com.example.aquacontrol.iu.granjas.GranjaScreen
import com.example.aquacontrol.iu.galpones.GalponScreen
import com.example.aquacontrol.iu.lineas.LineaScreen
import com.example.aquacontrol.iu.detalle.DetalleLineaScreen
import com.example.aquacontrol.iu.flushing.FlushingScreen
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel

@Composable
fun AppNavHost() {

    val navController: NavHostController = rememberNavController()
    val perfilViewModel: PerfilViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Routes.ROLE_SELECTION,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable(Routes.ROLE_SELECTION) {
                RolSelectionScreen(navController, perfilViewModel)
            }

            composable(Routes.OPERARIO_HOME) {
                OperarioScreen(navController, perfilViewModel)
            }

            composable(Routes.SUPERVISOR_HOME) {
                SupervisorScreen(navController, perfilViewModel)
            }

            composable(Routes.GRANJAS) {
                GranjaScreen(navController)
            }

            composable("galpones/{granjaId}") { backStack ->
                val granjaId = backStack.arguments?.getString("granjaId")?.toIntOrNull() ?: 0
                GalponScreen(navController, granjaId)
            }

            composable("lineas/{galponId}") { backStack ->
                val galponId = backStack.arguments?.getString("galponId")?.toIntOrNull() ?: 0
                LineaScreen(navController, galponId)
            }

            composable("${Routes.DETALLE_LINEA}/{lineaId}") { backStack ->
                val lineaId = backStack.arguments?.getString("lineaId")?.toIntOrNull() ?: 0
                DetalleLineaScreen(navController, lineaId)
            }

            composable("flushing/{lineaId}") { backStack ->
                val lineaId = backStack.arguments?.getString("lineaId")?.toIntOrNull() ?: 0
                FlushingScreen(navController, lineaId)
            }
        }
    }
}
