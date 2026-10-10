package com.example.aquacontrol.iu.alertas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.alertas.AlertaLinea
import com.example.aquacontrol.viewmodel.alertas.AlertasUiState
import com.example.aquacontrol.viewmodel.alertas.AlertasViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertasScreen(
    navController: NavController,
    viewModel: AlertasViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Alertas de temperatura",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Text(
                    text = "Estado según la última medición guardada de cada línea.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            when (val actual = estado) {
                AlertasUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is AlertasUiState.Success -> {
                    val criticas = actual.alertas.count {
                        it.medicion.estado == EstadoLinea.CRITICO
                    }

                    val advertencias = actual.alertas.count {
                        it.medicion.estado == EstadoLinea.ADVERTENCIA
                    }

                    val lineasConMediciones =
                        actual.totalLineas - actual.lineasSinMediciones

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Críticas: $criticas · Advertencias: $advertencias",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Líneas con mediciones: " +
                                            "$lineasConMediciones de ${actual.totalLineas}"
                                )

                                Text(
                                    text = "Sin mediciones: ${actual.lineasSinMediciones}"
                                )
                            }
                        }
                    }

                    if (actual.lineasSinMediciones > 0) {
                        item {
                            Text(
                                text = "Las líneas sin mediciones no tienen un estado térmico evaluado.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (actual.alertas.isEmpty()) {
                        item {
                            val mensaje = when {
                                actual.totalLineas == 0 ->
                                    "No hay líneas registradas."

                                lineasConMediciones == 0 ->
                                    "Todavía no hay mediciones para evaluar alertas."

                                else ->
                                    "No hay advertencias ni alertas críticas " +
                                            "en las últimas mediciones disponibles."
                            }

                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    items(
                        items = actual.alertas,
                        key = { it.medicion.lineaId }
                    ) { alerta ->
                        TarjetaAlerta(
                            alerta = alerta,
                            onVerLinea = {
                                navController.navigate(
                                    "${Routes.DETALLE_LINEA}/${alerta.medicion.lineaId}"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                is AlertasUiState.Error -> {
                    item {
                        Text(
                            text = actual.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    item {
                        Button(
                            onClick = viewModel::cargarAlertas,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "La lista se actualiza al guardar nuevas mediciones. " +
                            "Las lecturas simuladas se identifican en cada tarjeta.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
private fun TarjetaAlerta(
    alerta: AlertaLinea,
    onVerLinea: () -> Unit
) {
    val medicion = alerta.medicion
    val esCritica = medicion.estado == EstadoLinea.CRITICO

    val colorFondo = if (esCritica) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        Color(0xFFFFF3CD)
    }

    val colorContenido = if (esCritica) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        Color(0xFF4D3800)
    }

    val titulo = when (medicion.estado) {
        EstadoLinea.CRITICO -> "ALERTA CRÍTICA"
        EstadoLinea.ADVERTENCIA -> "ADVERTENCIA"
        EstadoLinea.NORMAL -> "NORMAL"
    }

    val origen = when (medicion.origen) {
        OrigenMedicion.MANUAL -> "Manual"
        OrigenMedicion.SIMULADA -> "Simulada"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorFondo,
            contentColor = colorContenido
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null
            )

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "${alerta.nombreGranja} · ${alerta.nombreGalpon}",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = alerta.nombreLinea,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Temperatura: ${medicion.temperatura} °C",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Origen: $origen"
            )

            Text(
                text = "Última medición: ${
                    formatearFechaAlerta(medicion.fechaHora)
                }",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = when {
                    medicion.origen == OrigenMedicion.SIMULADA ->
                        "Alerta generada por una simulación; no corresponde a un sensor real."

                    esCritica ->
                        "Revisa esta línea con prioridad y verifica la medición."

                    else ->
                        "Revisa la temperatura y las condiciones de esta línea."
                }
            )

            Button(
                onClick = onVerLinea,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver línea")
            }
        }
    }
}

private fun formatearFechaAlerta(fechaHora: Long): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    )

    return formato.format(Date(fechaHora))
}