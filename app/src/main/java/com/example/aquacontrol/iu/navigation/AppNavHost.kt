
package com.example.aquacontrol.iu.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aquacontrol.BuildConfig
import com.example.aquacontrol.repository.data.FlushingConsultaRepository
import com.example.aquacontrol.repository.data.FlushingRemoteRepository
import com.example.aquacontrol.repository.data.FotoFlushingRepository
import com.example.aquacontrol.iu.flushing.DetalleFlushingRemotoScreen
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingRemotoViewModel
import com.example.aquacontrol.data.AppDatabase
import com.example.aquacontrol.data.flushing.FotosFlushing
import com.example.aquacontrol.data.flushing.local.FlushingLocalDataSource
import com.example.aquacontrol.data.temperatura.local.TemperaturaLocalDataSource
import com.example.aquacontrol.iu.alertas.AlertasScreen
import com.example.aquacontrol.iu.alertas.DetalleAlertaScreen
import com.example.aquacontrol.iu.components.BottomBar
import com.example.aquacontrol.iu.detalle.DetalleLineaScreen
import com.example.aquacontrol.iu.flushing.FlushingDetalleScreen
import com.example.aquacontrol.iu.flushing.FlushingHomeScreen
import com.example.aquacontrol.iu.flushing.FlushingScreen
import com.example.aquacontrol.iu.flushing.RegistrarFlushingScreen
import com.example.aquacontrol.iu.galpones.GalponScreen
import com.example.aquacontrol.iu.granjas.GranjaScreen
import com.example.aquacontrol.iu.historial.HistorialTemperaturaScreen
import com.example.aquacontrol.iu.lineas.LineaScreen
import com.example.aquacontrol.iu.roles.OperarioScreen
import com.example.aquacontrol.iu.roles.RolSelectionScreen
import com.example.aquacontrol.iu.roles.SupervisorScreen
import com.example.aquacontrol.iu.simulacion.SimulacionScreen
import com.example.aquacontrol.iu.temperatura.DetalleTemperaturaScreen
import com.example.aquacontrol.iu.temperatura.RegistrarTemperaturaScreen
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.viewmodel.alertas.AlertasUiState
import com.example.aquacontrol.notificaciones.NotificadorAlertas
import com.example.aquacontrol.repository.flushing.FlushingRepositoryImpl
import com.example.aquacontrol.repository.temperatura.TemperaturaRepositoryImpl
import com.example.aquacontrol.viewmodel.alertas.AlertasViewModel
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingViewModel
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModel
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModelFactory
import com.example.aquacontrol.viewmodel.galpones.GalponViewModel
import com.example.aquacontrol.viewmodel.historial.HistorialTemperaturaViewModel
import com.example.aquacontrol.viewmodel.lineas.LineaViewModel
import com.example.aquacontrol.viewmodel.perfil.PerfilViewModel
import com.example.aquacontrol.viewmodel.simulacion.SimulacionViewModel
import com.example.aquacontrol.viewmodel.supervisor.SupervisorViewModel
import com.example.aquacontrol.viewmodel.temperatura.GraficoTemperaturaViewModel
import com.example.aquacontrol.viewmodel.temperatura.DetalleTemperaturaViewModel
import com.example.aquacontrol.viewmodel.temperatura.RegistrarTemperaturaViewModel
import com.example.aquacontrol.auth.LoginScreen
import com.example.aquacontrol.auth.AuthSessionManager
import android.util.Log
private const val DETALLE_ALERTA = "detalleAlerta"
private const val DETALLE_ALERTA_PARAM =
    "$DETALLE_ALERTA/{lineaId}/{medicionId}"

@Composable
// Conecta pantallas, repositorios y ViewModels; aplica los permisos del rol seleccionado.
fun AppNavHost(
    lineaIdNotificacion: Int? = null,
    medicionIdNotificacion: Long? = null,
    onNotificacionAbierta: () -> Unit = {}

)

{
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val perfilViewModel: PerfilViewModel = viewModel()

    val cerrarSesion: () -> Unit = {

        // Eliminamos el JWT y los datos de la sesión.
        AuthSessionManager.limpiarSesion()

        // Evidencia técnica del cierre de sesión.
        Log.d(
            "JWT_TEST",
            "JWT después de logout: " +
                    (AuthSessionManager.obtenerToken() != null)
        )

        // Limpiamos el perfil del usuario.
        perfilViewModel.limpiarPerfil()

        // Regresamos al login.
        navController.navigate(Routes.LOGIN) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    val perfil by perfilViewModel.perfilSeleccionado.collectAsState()
    // Fuente única del permiso que se envía a las pantallas y protege las rutas.
    val puedeRegistrar = perfil == PerfilUsuario.OPERARIO
    val appContext = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    // Comparte la base Room de data/AppDatabase.kt entre todos los repositorios.
    val database = remember(appContext) {
        AppDatabase.getInstance(appContext)
    }

    val flushingRepository = remember(database) {
        FlushingRepositoryImpl(
            FlushingLocalDataSource(database.flushingDao())
        )
    }

    val notificadorAlertas = remember(appContext) {
        NotificadorAlertas(appContext).also {
            it.crearCanales()
        }
    }

    val temperaturaRepository = remember(database, notificadorAlertas) {
        TemperaturaRepositoryImpl(
            localDataSource = TemperaturaLocalDataSource(
                database.medicionTemperaturaDao()
            ),
            notificadorAlertas = notificadorAlertas
        )
    }

    // Preparamos la consulta al backend y su copia Room cuando se necesitan.
    // Conservamos aparte el repositorio de registros locales antiguos.
    val consultaFlushing = remember(database) {
        lazy {
            FlushingConsultaRepository(
                dao = database.flushingRemotoDao(),
                remoto = FlushingRemoteRepository(),
                servidor = BuildConfig.BACKEND_URL.trim().trimEnd('/')
            )
        }
    }

    // El historial consulta el servidor y puede mostrar la copia descargada.
    val historialFactory = remember(flushingRepository, consultaFlushing) {
        viewModelFactory {
            initializer {
                FlushingViewModel(
                    repository = flushingRepository,
                    consultaRemota = consultaFlushing.value
                )
            }
        }
    }

    // Consultamos la foto y conservamos su referencia en la copia remota.
    val fotosFlushing = remember(appContext, database) {
        lazy {
            FotoFlushingRepository(
                context = appContext,
                dao = database.flushingRemotoDao(),
                remoto = FlushingRemoteRepository(),
                servidor = BuildConfig.BACKEND_URL.trim().trimEnd('/')
            )
        }
    }

    // El detalle carga sus datos y su foto con estados independientes.
    val detalleFlushingRemotoFactory = remember(consultaFlushing, fotosFlushing) {
        viewModelFactory {
            initializer {
                DetalleFlushingRemotoViewModel(
                    repository = consultaFlushing.value,
                    fotosRepository = fotosFlushing.value
                )
            }
        }
    }

    // Conectamos el registro y su foto con el backend autenticado.
    // Creamos la fábrica al abrir el formulario con una URL configurada.
    val registroFactory = remember(appContext) {
        lazy {
            RegistrarFlushingViewModelFactory(
                context = appContext,
                repository = FlushingRemoteRepository()
            )
        }
    }

    // Inyecta el repositorio y la limpieza de evidencia al detalle de flushing.
    val detalleFlushingFactory = remember(flushingRepository, appContext) {
        viewModelFactory {
            initializer {
                DetalleFlushingViewModel(
                    repository = flushingRepository,
                    eliminarFoto = { nombre ->
                        // El ViewModel invoca esta limpieza solo después de eliminar el evento en Room.
                        FotosFlushing.eliminarFotoNoGuardada(appContext, nombre)
                    }
                )
            }
        }
    }

    val registroTemperaturaFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                RegistrarTemperaturaViewModel(temperaturaRepository)
            }
        }
    }

    val historialTemperaturaFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                HistorialTemperaturaViewModel(temperaturaRepository)
            }
        }
    }

    // Inyecta el repositorio al detalle que permite editar o eliminar según el rol.
    val detalleTemperaturaFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                DetalleTemperaturaViewModel(temperaturaRepository)
            }
        }
    }

    // Proporciona el repositorio al gráfico sin crear otra base de datos.
    val graficoTemperaturaFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                GraficoTemperaturaViewModel(temperaturaRepository)
            }
        }
    }

    val galponesFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                GalponViewModel(temperaturaRepository)
            }
        }
    }

    val lineasFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                LineaViewModel(temperaturaRepository)
            }
        }
    }

    val detalleFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                DetalleLineaViewModel(temperaturaRepository)
            }
        }
    }

    val alertasFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                AlertasViewModel(temperaturaRepository)
            }
        }
    }

    val alertasViewModel: AlertasViewModel = viewModel(
        factory = alertasFactory
    )
    // Observa el ViewModel; cada cambio de estado actualiza la interfaz.
    val estadoAlertas by alertasViewModel.uiState.collectAsState()
    val alertasActuales = (estadoAlertas as? AlertasUiState.Success)
        ?.alertas.orEmpty()
    val cantidadCriticas = alertasActuales.count {
        it.medicion.estado == EstadoLinea.CRITICO
    }
    val cantidadAdvertencias = alertasActuales.count {
        it.medicion.estado == EstadoLinea.ADVERTENCIA
    }

    val supervisorFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                SupervisorViewModel(temperaturaRepository)
            }
        }
    }

    val simulacionFactory = remember(temperaturaRepository) {
        viewModelFactory {
            initializer {
                SimulacionViewModel(temperaturaRepository)
            }
        }
    }

    val simulacionViewModel: SimulacionViewModel = viewModel(
        factory = simulacionFactory
    )

    // Detiene la generación de lecturas cuando el perfil deja de ser Operario.
    LaunchedEffect(puedeRegistrar, simulacionViewModel) {
        // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
        if (!puedeRegistrar) {
            simulacionViewModel.detener()
        }
    }

    // Vincula la simulación al ciclo de vida; ON_STOP la detiene al ir a segundo plano.
    DisposableEffect(lifecycleOwner, simulacionViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                simulacionViewModel.detener()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            simulacionViewModel.detener()
        }
    }

    // Reserva el área de la barra inferior y entrega su margen al contenido navegable.
    Scaffold(
        bottomBar = {
            if (currentRoute != Routes.LOGIN) {
                BottomBar(
                    navController = navController,
                    perfil = perfil,
                    cantidadCriticas = cantidadCriticas,
                    cantidadAdvertencias = cantidadAdvertencias
                )
            }
        }
    ) { innerPadding ->
        // Declara las rutas y convierte sus parámetros a los IDs usados por cada pantalla.
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen { nombre, rol ->
                    perfilViewModel.actualizarNombre(nombre)
                    perfilViewModel.seleccionarPerfil(rol)

                    val destino = if (rol == PerfilUsuario.OPERARIO) {
                        Routes.OPERARIO_HOME
                    } else {
                        Routes.SUPERVISOR_HOME
                    }

                    navController.navigate(destino) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            // Roles
            composable(Routes.ROLE_SELECTION) {
                RolSelectionScreen(navController, perfilViewModel)
            }

            composable(Routes.OPERARIO_HOME) {
                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (puedeRegistrar) {
                    OperarioScreen(navController, perfilViewModel,onCerrarSesion = cerrarSesion)
                } else {
                    Text("Selecciona el rol Operario desde Inicio para acceder.")
                }
            }

            composable(Routes.SUPERVISOR_HOME) { backStack ->
                val supervisorViewModel: SupervisorViewModel = viewModel(
                    viewModelStoreOwner = backStack,
                    factory = supervisorFactory
                )

                SupervisorScreen(
                    navController = navController,
                    perfilViewModel = perfilViewModel,
                    viewModel = supervisorViewModel,
                    onCerrarSesion = cerrarSesion
                )
            }

            // Granjas
            composable(Routes.GRANJAS) {
                GranjaScreen(navController)
            }

            // Galpones
            composable(Routes.GALPONES_PARAM) { backStack ->
                val granjaId = backStack.arguments
                    ?.getString("granjaId")
                    ?.toIntOrNull() ?: 0

                val galponesViewModel: GalponViewModel = viewModel(
                    viewModelStoreOwner = backStack,
                    factory = galponesFactory
                )

                GalponScreen(
                    navController = navController,
                    granjaId = granjaId,
                    viewModel = galponesViewModel
                )
            }

            // Líneas
            composable(
                route = Routes.LINEAS_PARAM,
                arguments = listOf(
                    navArgument("galponId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val galponId = backStack.arguments
                    ?.getInt("galponId") ?: 0

                val lineasViewModel: LineaViewModel = viewModel(
                    viewModelStoreOwner = backStack,
                    factory = lineasFactory
                )

                LineaScreen(
                    navController = navController,
                    galponId = galponId,
                    viewModel = lineasViewModel
                )
            }

            // Detalle de línea
            composable(
                route = Routes.DETALLE_LINEA_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                val detalleViewModel: DetalleLineaViewModel = viewModel(
                    viewModelStoreOwner = backStack,
                    factory = detalleFactory
                )

                // Solo Supervisor recibe el gráfico; cada detalle conserva su propio ViewModel.
                val graficoViewModel: GraficoTemperaturaViewModel? =
                    if (perfil == PerfilUsuario.SUPERVISOR) {
                        viewModel<GraficoTemperaturaViewModel>(
                            viewModelStoreOwner = backStack,
                            factory = graficoTemperaturaFactory
                        )
                    } else {
                        null
                    }

                DetalleLineaScreen(
                    navController = navController,
                    lineaId = lineaId,
                    viewModel = detalleViewModel,
                    puedeRegistrar = puedeRegistrar,
                    graficoViewModel = graficoViewModel
                )
            }

            // Historial de temperaturas con filtros y paginación
            composable(
                route = Routes.HISTORIAL_TEMPERATURAS_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                if (lineaId <= 0) {
                    Text("No se pudo identificar la línea.")
                } else {
                    val historialTemperaturaViewModel:
                            HistorialTemperaturaViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = historialTemperaturaFactory
                    )

                    HistorialTemperaturaScreen(
                        lineaId = lineaId,
                        viewModel = historialTemperaturaViewModel,
                        // Conserva el historial en la pila para regresar con sus filtros.
                        onVerDetalle = { medicionId ->
                            navController.navigate(
                                "${Routes.DETALLE_TEMPERATURA}/$lineaId/$medicionId"
                            ) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Detalle de una medición: recibe su identidad y el perfil actual.
            composable(
                route = Routes.DETALLE_TEMPERATURA_PARAM,
                arguments = listOf(
                    navArgument("lineaId") { type = NavType.IntType },
                    navArgument("medicionId") { type = NavType.LongType }
                )
            ) { backStack ->
                val lineaId = backStack.arguments?.getInt("lineaId") ?: 0
                val medicionId = backStack.arguments?.getLong("medicionId") ?: 0L

                if (lineaId <= 0 || medicionId <= 0L) {
                    Text("No se pudo identificar la medición.")
                } else {
                    // Cada entrada mantiene su propio estado y borrador de edición.
                    val detalleTemperaturaViewModel: DetalleTemperaturaViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = detalleTemperaturaFactory
                    )
                    DetalleTemperaturaScreen(
                        lineaId = lineaId,
                        medicionId = medicionId,
                        perfil = perfil,
                        viewModel = detalleTemperaturaViewModel
                    )
                }
            }

            // Registro manual de temperatura
            composable(
                route = Routes.REGISTRAR_TEMPERATURA_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (!puedeRegistrar) {
                    Text("El registro está disponible solo para el operario.")
                } else if (lineaId <= 0) {
                    Text(
                        "No se pudo identificar la línea. Vuelve y selecciónala."
                    )
                } else {
                    val registroTemperaturaViewModel:
                            RegistrarTemperaturaViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = registroTemperaturaFactory
                    )

                    RegistrarTemperaturaScreen(
                        navController = navController,
                        lineaId = lineaId,
                        viewModel = registroTemperaturaViewModel
                    )
                }
            }

            // Medición que originó una notificación
            composable(
                route = DETALLE_ALERTA_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    },
                    navArgument("medicionId") {
                        type = NavType.LongType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                val medicionId = backStack.arguments
                    ?.getLong("medicionId") ?: 0L

                if (lineaId <= 0 || medicionId <= 0L) {
                    Text("No se pudo identificar la alerta.")
                } else {
                    val detalleAlertaViewModel: DetalleLineaViewModel =
                        viewModel(
                            viewModelStoreOwner = backStack,
                            factory = detalleFactory
                        )

                    DetalleAlertaScreen(
                        navController = navController,
                        lineaId = lineaId,
                        medicionId = medicionId,
                        viewModel = detalleAlertaViewModel
                    )
                }
            }

            // Historial de flushing
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
                } else if (BuildConfig.BACKEND_URL.isBlank()) {
                    Text("Falta configurar la dirección del backend.")
                } else {
                    val historialViewModel: FlushingViewModel = viewModel(
                        viewModelStoreOwner = backStack,
                        factory = historialFactory
                    )

                    FlushingScreen(
                        navController = navController,
                        lineaId = lineaId,
                        viewModel = historialViewModel,
                        puedeRegistrar = puedeRegistrar
                    )
                }
            }

            // Registro de flushing
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

                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (!puedeRegistrar) {
                    Text("El registro está disponible solo para el operario.")
                } else if (lineaId <= 0) {
                    Text(
                        "No se pudo identificar la línea. Vuelve y selecciónala."
                    )
                } else if (BuildConfig.BACKEND_URL.isBlank()) {
                    Text("Falta configurar la dirección del backend para registrar.")
                } else {
                    val registroViewModel: RegistrarFlushingViewModel =
                        viewModel(
                            viewModelStoreOwner = backStack,
                            factory = registroFactory.value
                        )

                    RegistrarFlushingScreen(
                        navController = navController,
                        lineaId = lineaId,
                        viewModel = registroViewModel
                    )
                }
            }

            // Abrimos registros remotos sin utilizar los IDs de la tabla antigua.
            composable(
                route = "flushingRemotoDetalle/{lineaId}/{registroId}",
                arguments = listOf(
                    navArgument("lineaId") { type = NavType.LongType },
                    navArgument("registroId") { type = NavType.LongType }
                )
            ) { backStack ->
                val lineaId = backStack.arguments?.getLong("lineaId") ?: 0L
                val registroId = backStack.arguments?.getLong("registroId") ?: 0L

                if (lineaId <= 0L || registroId <= 0L) {
                    Text("No se pudo identificar el registro de flushing.")
                } else if (BuildConfig.BACKEND_URL.isBlank()) {
                    Text("Falta configurar la dirección del backend.")
                } else {
                    val detalleRemotoViewModel: DetalleFlushingRemotoViewModel =
                        viewModel(
                            viewModelStoreOwner = backStack,
                            factory = detalleFlushingRemotoFactory
                        )

                    DetalleFlushingRemotoScreen(
                        lineaId = lineaId,
                        registroId = registroId,
                        viewModel = detalleRemotoViewModel
                    )
                }
            }

            // Conservamos el detalle original para los registros locales antiguos.
            composable(
                route = Routes.DETALLE_FLUSHING_PARAM,
                arguments = listOf(
                    navArgument("lineaId") {
                        type = NavType.IntType
                    },
                    navArgument("eventoId") {
                        type = NavType.IntType
                    }
                )
            ) { backStack ->
                val lineaId = backStack.arguments
                    ?.getInt("lineaId") ?: 0

                val eventoId = backStack.arguments
                    ?.getInt("eventoId") ?: 0

                if (lineaId <= 0 || eventoId <= 0) {
                    Text("No se pudo identificar el registro de flushing.")
                } else {
                    // Estado independiente del historial, con edición y eliminación por perfil.
                    val detalleFlushingViewModel: DetalleFlushingViewModel =
                        viewModel(
                            viewModelStoreOwner = backStack,
                            factory = detalleFlushingFactory
                        )

                    FlushingDetalleScreen(
                        lineaId = lineaId,
                        eventoId = eventoId,
                        perfil = perfil,
                        viewModel = detalleFlushingViewModel
                    )
                }
            }

            // Alertas actuales
            composable(Routes.ALERTAS) {

                AlertasScreen(
                    navController = navController,
                    viewModel = alertasViewModel
                )
            }

            // Selección para registrar flushing
            composable(Routes.FLUSHING) {
                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (puedeRegistrar) {
                    FlushingHomeScreen(navController)
                } else {
                    Text("El registro está disponible solo para el operario.")
                }
            }

            // Simulación compartida
            composable(Routes.SIMULACION) {
                // Permiso del perfil: solo Operario puede acceder a acciones que registran datos.
                if (puedeRegistrar) {
                    SimulacionScreen(
                        navController = navController,
                        viewModel = simulacionViewModel
                    )
                } else {
                    Text("La simulación está disponible solo para el operario.")
                }
            }
        }
    }

    // Abre el detalle de la medición recibida desde MainActivity al tocar una notificación.
    LaunchedEffect(
        lineaIdNotificacion,
        medicionIdNotificacion
    ) {
        val lineaId = lineaIdNotificacion
        val medicionId = medicionIdNotificacion

        if (
            lineaId != null &&
            medicionId != null &&
            lineaId > 0 &&
            medicionId > 0L
        ) {
            navController.navigate(
                "$DETALLE_ALERTA/$lineaId/$medicionId"
            ) {
                launchSingleTop = true
            }

            onNotificacionAbierta()
        }
    }
}
