package com.example.aquacontrol.iu.simulacion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.ui.theme.Navy
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
import com.example.aquacontrol.viewmodel.simulacion.SimulacionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun SimulacionScreen(
    navController: NavController,
    viewModel: SimulacionViewModel
) {
    val estado by viewModel.uiState.collectAsState()
    val soloUna by viewModel.soloLineaSeleccionada.collectAsState()
    val lineasActivas by viewModel.lineasEnSimulacion.collectAsState()
    val controlesManuales by viewModel.escenariosManuales.collectAsState()

    LaunchedEffect(viewModel) { viewModel.actualizarLineas() }

    var menuLineasAbierto by remember(estado.lineas) {
        mutableStateOf(false)
    }

    var menuEscenariosAbierto by remember {
        mutableStateOf(false)
    }

    val lineaSeleccionada = estado.lineas.firstOrNull {
        it.id == estado.lineaSeleccionadaId
    }
    val controlManualActual = estado.lineaSeleccionadaId?.let {
        controlesManuales[it]
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AquaEncabezado(
                titulo = "Simulación",
                subtitulo = "Temperaturas de demostración"
            )

            Text(
                text = "Las mediciones simuladas se guardan en Room, no en PostgreSQL. " +
                        "No representan lecturas reales de sensores.",
                style = MaterialTheme.typography.bodyLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Navy.copy(alpha = 0.08f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Navy
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (estado.activa) {
                            "Simulación activa"
                        } else {
                            "Simulación detenida"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Mediciones guardadas en esta sesión: " +
                                estado.medicionesGeneradas
                    )
                    Text(
                        text = if (estado.activa) {
                            "Líneas simuladas: ${lineasActivas.size}. " +
                                    "Controles manuales activos: ${controlesManuales.size}."
                        } else {
                            "Sin líneas simulándose actualmente."
                        }
                    )

                    val ultimaActualizacion = estado.ultimaActualizacion

                    if (ultimaActualizacion != null) {
                        Text(
                            text = "Último guardado: ${
                                formatearFechaSimulacion(ultimaActualizacion)
                            }"
                        )
                    } else {
                        Text(
                            text = "Esta sesión todavía no tiene mediciones guardadas."
                        )
                    }
                }
            }

            Text(
                text = "Cada inicio reinicia el contador y conserva el historial. " +
                        "La simulación se detiene al cerrar la app.",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Navy.copy(alpha = 0.08f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Navy
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Alcance de la simulación", style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (soloUna) "Modo: solo línea seleccionada" else "Modo: todas las líneas"
                    )
                    OutlinedButton(
                        onClick = { viewModel.establecerSoloLineaSeleccionada(false) },
                        enabled = !estado.activa && soloUna,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("Simular todas las líneas")
                    }
                    OutlinedButton(
                        onClick = { viewModel.establecerSoloLineaSeleccionada(true) },
                        enabled = !estado.activa && !soloUna,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("Simular solo la línea elegida")
                    }
                    Text(
                        text = "Configura el alcance antes de iniciar. En modo individual, " +
                                "selecciona la línea en el formulario inferior.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            estado.error?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = viewModel::iniciar,
                enabled = !estado.activa,
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(4.dp, 1.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                Text("Iniciar simulación")
            }

            OutlinedButton(
                onClick = viewModel::detener,
                enabled = estado.activa,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                Text("Detener simulación")
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Navy.copy(alpha = 0.08f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = Navy
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Aplicar escenario a una línea",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "Seleccionar las opciones no cambia la " +
                                "simulación hasta pulsar Aplicar escenario."
                    )

                    Text(
                        text = "Línea",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                menuLineasAbierto = true
                            },
                            enabled = estado.lineas.isNotEmpty() &&
                                    !(estado.activa && soloUna),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) {
                            Text(
                                text = if (lineaSeleccionada == null) {
                                    "Seleccionar línea"
                                } else {
                                    "${lineaSeleccionada.nombreGranja} · " +
                                            "${lineaSeleccionada.nombreGalpon} · " +
                                            lineaSeleccionada.nombreLinea
                                }
                            )
                        }

                        if (menuLineasAbierto) {
                            SelectorSimulacion(
                                titulo = "Seleccionar línea",
                                opciones = estado.lineas.map { linea ->
                                    linea.id.toString() to
                                            "${linea.nombreGranja} · ${linea.nombreGalpon} · ${linea.nombreLinea}"
                                },
                                onCerrar = { menuLineasAbierto = false },
                                onSeleccionar = { id ->
                                    menuLineasAbierto = false
                                    viewModel.seleccionarLinea(id.toInt())
                                }
                            )
                        }
                    }

                    Text(
                        text = "Escenario",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                menuEscenariosAbierto = true
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) {
                            Text(
                                text = nombreEscenarioSimulacion(
                                    estado.escenarioSeleccionado
                                )
                            )
                        }

                        if (menuEscenariosAbierto) {
                            SelectorSimulacion(
                                titulo = "Seleccionar escenario",
                                opciones = EscenarioSimulacion.entries.map {
                                    it.name to nombreEscenarioSimulacion(it)
                                },
                                onCerrar = { menuEscenariosAbierto = false },
                                onSeleccionar = { nombre ->
                                    menuEscenariosAbierto = false
                                    viewModel.seleccionarEscenario(
                                        EscenarioSimulacion.valueOf(nombre)
                                    )
                                }
                            )
                        }
                    }

                    Text(
                        text = "El escenario manual se mantiene sin límite de tiempo " +
                                "hasta liberarlo o detener la simulación. El cambio es gradual: " +
                                "durante la transición aún puede haber alertas de otro rango.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (!estado.activa) {
                        Text(
                            text = "Inicia la simulación para aplicar un escenario."
                        )
                    } else if (lineaSeleccionada == null) {
                        Text(
                            text = "Selecciona la línea que deseas modificar."
                        )
                    }

                    Button(
                        onClick = viewModel::aplicarEscenario,
                        enabled = estado.puedeAplicarEscenario &&
                                lineaSeleccionada != null &&
                                lineaSeleccionada.id in lineasActivas,
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(4.dp, 1.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) {
                        Text("Aplicar escenario")
                    }

                    if (controlManualActual != null) {
                        Text(
                            text = "Control manual activo: " +
                                    nombreEscenarioSimulacion(controlManualActual.escenario) +
                                    " · Objetivo: ${controlManualActual.objetivo} °C " +
                                    "· Duración: hasta liberar o detener.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(
                            onClick = viewModel::liberarEscenarioSeleccionado,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        ) {
                            Text("Liberar escenario (volver a automático)")
                        }
                    } else if (estado.activa && lineaSeleccionada != null &&
                        lineaSeleccionada.id in lineasActivas
                    ) {
                        Text(
                            text = "Esta línea sigue escenarios automáticos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    estado.mensajeEscenario?.let { mensaje ->
                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    navController.navigate(Routes.ALERTAS) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
            ) {
                Text("Ver alertas")
            }

        }
    }
}

private fun nombreEscenarioSimulacion(
    escenario: EscenarioSimulacion
): String {
    return when (escenario) {
        EscenarioSimulacion.ESTABILIDAD -> "Estabilidad"
        EscenarioSimulacion.CALENTAMIENTO -> "Calentamiento"
        EscenarioSimulacion.ENFRIAMIENTO -> "Enfriamiento"
        EscenarioSimulacion.CRITICO_CALOR -> "Crítico por calor"
        EscenarioSimulacion.CRITICO_FRIO -> "Crítico por frío"
        EscenarioSimulacion.RECUPERACION -> "Recuperación"
    }
}

private fun formatearFechaSimulacion(fechaHora: Long): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    )

    formato.timeZone = TimeZone.getTimeZone("America/Santiago")
    return formato.format(Date(fechaHora))
}
@Composable
private fun SelectorSimulacion(
    titulo: String,
    opciones: List<Pair<String, String>>,
    onCerrar: () -> Unit,
    onSeleccionar: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(titulo, color = Navy)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(opciones, key = { it.first }) { (id, nombre) ->
                    OutlinedButton(
                        onClick = { onSeleccionar(id) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) {
                        Text(nombre, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    )
}
