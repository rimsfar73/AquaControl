package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.iu.components.AquaTarjetaAccion
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.flushing.FechasCalendarioFlushing
import com.example.aquacontrol.viewmodel.flushing.FlushingUiState
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel

// Preparamos únicamente los datos necesarios para las tarjetas.
// Cada registro conserva una ruta de detalle según su procedencia.
private data class FilaHistorialFlushing(
    val clave: String,
    val fechaHora: String,
    val temperaturaAntes: Double?,
    val temperaturaDespues: Double?,
    val fotoDisponible: Boolean,
    val rutaDetalle: String
)

@Composable
fun FlushingScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: FlushingViewModel,
    puedeRegistrar: Boolean = false
) {
    val estado by viewModel.uiState.collectAsState()
    val backStackEntry = navController.currentBackStackEntry

    var mesVisible by rememberSaveable(lineaId) {
        mutableStateOf(
            FechasCalendarioFlushing.inicioMes(
                FechasCalendarioFlushing.hoy()
            )
        )
    }

    var diaSeleccionado by rememberSaveable(lineaId) {
        mutableStateOf<Long?>(FechasCalendarioFlushing.hoy())
    }

    // Elegimos una sola procedencia: remoto o local antiguo.
    // Nunca juntamos registros cuyos identificadores puedan coincidir.
    val filas = remember(estado) {
        val resultado = estado as? FlushingUiState.Success

        if (resultado == null) {
            emptyList()
        } else if (resultado.registrosRemotos.isNotEmpty()) {
            resultado.registrosRemotos.map { registro ->
                FilaHistorialFlushing(
                    clave = "remoto_${registro.id}",
                    fechaHora = fechaParaCalendario(registro.fechaHora),
                    temperaturaAntes = registro.temperaturaAntes,
                    temperaturaDespues = registro.temperaturaDespues,
                    fotoDisponible = !registro.fotoRuta.isNullOrBlank(),

                    // Usaremos un detalle remoto separado para no buscar
                    // accidentalmente este ID en los registros locales antiguos.
                    rutaDetalle = "flushingRemotoDetalle/" +
                            "${registro.lineaId}/${registro.id}"
                )
            }
        } else {
            resultado.eventos.map { evento ->
                FilaHistorialFlushing(
                    clave = "local_${evento.id}",
                    fechaHora = evento.fechaHora,
                    temperaturaAntes = evento.temperaturaAntes,
                    temperaturaDespues = evento.temperaturaDespues,
                    fotoDisponible = !evento.fotoRuta.isNullOrBlank(),
                    rutaDetalle = "${Routes.DETALLE_FLUSHING}/" +
                            "${evento.lineaId}/${evento.id}"
                )
            }
        }
    }

    val filasPorDia = remember(filas) {
        filas.groupBy {
            FechasCalendarioFlushing.diaDelRegistro(it.fechaHora)
        }
    }

    val diasConEventos = remember(filasPorDia) {
        filasPorDia.keys.filterNotNull().toSet()
    }

    val filasDelDia = filasPorDia[diaSeleccionado].orEmpty()
    val filasSinFecha = filasPorDia[null].orEmpty()
    val desdeCopiaLocal =
        (estado as? FlushingUiState.Success)?.desdeCopiaLocal == true

    // Actualizamos al entrar o regresar a esta pantalla.
    DisposableEffect(backStackEntry, lineaId, viewModel) {
        val lifecycle = backStackEntry?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarFlushing(lineaId)
            }
        }

        if (lifecycle != null) {
            lifecycle.addObserver(observer)
        } else {
            viewModel.cargarFlushing(lineaId)
        }

        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AquaEncabezado(titulo = "Historial de flushing")
            }

            // Avisamos que los datos pueden haber cambiado en el servidor.
            if (desdeCopiaLocal) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Mostrando copia local",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "No se pudo conectar con el servidor. " +
                                        "Estos registros pueden estar desactualizados.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            OutlinedButton(
                                onClick = {
                                    viewModel.cargarFlushing(lineaId)
                                }
                            ) {
                                Text("Actualizar")
                            }
                        }
                    }
                }
            }

            if (puedeRegistrar) {
                item {
                    Button(
                        onClick = {
                            navController.navigate(
                                "${Routes.REGISTRAR_FLUSHING}/$lineaId"
                            ) {
                                launchSingleTop = true
                            }
                        },
                        enabled = lineaId > 0 && !desdeCopiaLocal,
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 1.dp,
                            disabledElevation = 0.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    ) {
                        Text(
                            text = "Registrar flushing",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            when (val actual = estado) {
                FlushingUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is FlushingUiState.Error -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = actual.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            OutlinedButton(
                                onClick = {
                                    viewModel.cargarFlushing(lineaId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                FlushingUiState.Empty,
                is FlushingUiState.Success -> {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            CalendarioFlushing(
                                modifier = Modifier.padding(12.dp),
                                mesVisible = mesVisible,
                                diaSeleccionado = diaSeleccionado,
                                diasConEventos = diasConEventos,
                                onCambiarMes = {
                                    mesVisible = it
                                    diaSeleccionado = null
                                },
                                onSeleccionarDia = {
                                    diaSeleccionado = it
                                }
                            )
                        }
                    }

                    item {
                        val dia = diaSeleccionado

                        if (dia == null) {
                            Text("Selecciona un día del calendario.")
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Flushing del " +
                                            FechasCalendarioFlushing.tituloDia(dia),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = when {
                                        actual is FlushingUiState.Empty ->
                                            "Esta línea todavía no tiene registros de flushing."
                                        filasDelDia.isEmpty() ->
                                            "No hay registros para este día."
                                        filasDelDia.size == 1 ->
                                            "1 registro"
                                        else ->
                                            "${filasDelDia.size} registros"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    if (diaSeleccionado != null) {
                        items(
                            items = filasDelDia,
                            key = { it.clave }
                        ) { fila ->
                            TarjetaHistorialFlushing(fila) {
                                navController.navigate(fila.rutaDetalle) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }

                    // Mantenemos accesibles los registros aunque su fecha
                    // no pueda colocarse correctamente en el calendario.
                    if (filasSinFecha.isNotEmpty()) {
                        item {
                            Text(
                                text = "Registros con fecha no reconocida",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(
                            items = filasSinFecha,
                            key = { it.clave }
                        ) { fila ->
                            TarjetaHistorialFlushing(fila) {
                                navController.navigate(fila.rutaDetalle) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Adaptamos la fecha ISO del backend al formato del calendario.
// No cambiamos la zona horaria ni modificamos la fecha almacenada.
private fun fechaParaCalendario(fechaHora: String): String {
    val texto = fechaHora.trim()
    val patron = Regex(
        """^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})(?::(\d{2})(?:\.\d+)?)?$"""
    )
    val coincidencia = patron.matchEntire(texto) ?: return texto

    val fecha = coincidencia.groupValues[1]
    val hora = coincidencia.groupValues[2]
    val segundos = coincidencia.groupValues[3].ifEmpty { "00" }

    return "$fecha $hora:$segundos"
}

@Composable
private fun TarjetaHistorialFlushing(
    fila: FilaHistorialFlushing,
    onVerDetalle: () -> Unit
) {
    // Mostramos explícitamente si un registro antiguo no tiene temperatura.
    val antes = fila.temperaturaAntes?.let { "$it °C" } ?: "Sin dato"
    val despues = fila.temperaturaDespues?.let { "$it °C" } ?: "Sin dato"

    val resumen = buildString {
        append("Antes: $antes · Después: $despues")
        if (fila.fotoDisponible) {
            append("\nFoto disponible en el dispositivo")
        }
    }

    AquaTarjetaAccion(
        titulo = fila.fechaHora,
        descripcion = resumen,
        icono = Icons.Outlined.Waves,
        onClick = onVerDetalle
    )
}