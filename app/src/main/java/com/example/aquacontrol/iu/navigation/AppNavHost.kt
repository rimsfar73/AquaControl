package com.example.aquacontrol.iu.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aquacontrol.data.AppDatabase
import com.example.aquacontrol.data.flushing.local.FlushingLocalDataSource
import com.example.aquacontrol.iu.alertas.AlertasScreen
import com.example.aquacontrol.iu.components.BottomBar
import com.example.aquacontrol.iu.detalle.DetalleLineaScreen
import com.example.aquacontrol.iu.flushing.FlushingDetalleScreen
import com.example.aquacontrol.iu.flushing.FlushingHomeScreen
import com.example.aquacontrol.iu.flushing.FlushingScreen
import com.example.aquacontrol.iu.flushing.RegistrarFlushingScreen
import com.example.aquacontrol.iu.galpones.GalponScreen
import com.example.aquacontrol.iu.granjas.GranjaScreen
import com.example.aquacontrol.iu.lineas.LineaScreen
import com.example.aquacontrol.iu.roles.OperarioScreen
import com.example.aquacontrol.iu.roles.RolSelectionScreen
import com.example.aquacontrol.iu.roles.SupervisorScreen
import com.example.aquacontrol.repository.flushing.FlushingRepositoryImpl
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModel
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val perfilViewModel: PerfilViewModel = viewModel()
    val appContext = LocalContext.current.applicationContext

    // El formulario y el historial utilizan la misma base de datos.
    val flushingRepository = remember(appContext) {
        val database = AppDatabase.getInstance(appContext)

        val localDataSource = FlushingLocalDataSource(
            database.flushingDao()
        )

        FlushingRepositoryImpl(localDataSource)
    }

    val historialFactory = remember(flushingRepository) {
        viewModelFactory {
            initializer {
                FlushingViewModel(flushingRepository)
            }
        }
    }

    val registroFactory = remember(flushingRepository) {
        viewModelFactory {
            initializer {
                RegistrarFlushingViewModel(flushingRepository)
            }
        }
    }

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

            // Historial real de flushing por línea
            composable(
                route = Routes.FLUSHING_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                if (lineaId <= 0) {
                    Text(
                        "No se pudo identificar la línea. Vuelve y selecciónala."
                    )
                } else {
                    val historialViewModel: FlushingViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = historialFactory
                    )

                    FlushingScreen(
                        navController = navController,
                        lineaId = lineaId,
                        viewModel = historialViewModel
                    )
                }
            }

            // Formulario de registro para una línea seleccionada
            composable(
                route = "${Routes.REGISTRAR_FLUSHING}/{lineaId}",
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                if (lineaId <= 0) {
                    Text(
                        "No se pudo identificar la línea. Vuelve y selecciónala."
                    )
                } else {
                    val registroViewModel: RegistrarFlushingViewModel =
                        viewModel(
                            viewModelStoreOwner = backStack,
                            factory = registroFactory
                        )

                    RegistrarFlushingScreen(
                        navController = navController,
                        lineaId = lineaId,
                        viewModel = registroViewModel
                    )
                }
            }

            // Alertas
            composable(Routes.ALERTAS) {
                AlertasScreen(navController)
            }

            // Selección de granja, galpón y línea para registrar
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