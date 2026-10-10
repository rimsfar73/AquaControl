package com.example.aquacontrol.iu.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.components.IndicadorEstado
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaUiState
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetalleLineaScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: DetalleLineaViewModel
) {
    LaunchedEffect(lineaId, viewModel) {
        viewModel.cargarHistorial(lineaId)
    }

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
            when (val actual = estado) {
                DetalleLineaUiState.Loading -> {
                    item {
                        Text(
                            text = "Detalle de línea",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    item {
                        CircularProgressIndicator()
                    }
                }

                is DetalleLineaUiState.Success -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Detalle de ${actual.linea.nombre}",
                                style = MaterialTheme.typography.headlineSmall
                            )

                            Text(
                                text = "${actual.nombreGranja} · ${actual.nombreGalpon}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Última medición",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                val medicion = actual.linea.ultimaMedicion

                                if (medicion == null) {
                                    IndicadorEstado(estado = null)

                                    Text(
                                        text = "Todavía no se ha guardado una " +
                                                "temperatura para esta línea.",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                } else {
                                    Text(
                                        text = "${medicion.temperatura} °C",
                                        style = MaterialTheme.typography.headlineMedium
                                    )

                                    IndicadorEstado(
                                        estado = medicion.estado
                                    )

                                    Text(
                                        text = "Origen: ${
                                            nombreOrigenDetalle(medicion.origen)
                                        }",
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = "Fecha y hora: ${
                                            formatearFechaDetalle(medicion.fechaHora)
                                        }",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                navController.navigate(
                                    "${Routes.REGISTRAR_TEMPERATURA}/${actual.linea.id}"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "Registrar temperatura",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                navController.navigate(
                                    "${Routes.FLUSHING}/${actual.linea.id}"
                                ) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver historial de flushing")
                        }
                    }

                    item {
                        Text(
                            text = "Historial de temperaturas",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    item {
                        Text(
                            text = "Mediciones guardadas: ${actual.historial.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (actual.historial.isEmpty()) {
                        item {
                            Text(
                                text = "Esta línea todavía no tiene " +
                                        "mediciones históricas registradas."
                            )
                        }
                    } else {
                        items(
                            items = actual.historial,
                            key = { it.id }
                        ) { registro ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${registro.temperatura} °C",
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    IndicadorEstado(
                                        estado = registro.estado
                                    )

                                    Text(
                                        text = "Origen: ${
                                            nombreOrigenDetalle(registro.origen)
                                        }",
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    Text(
                                        text = "Fecha y hora: ${
                                            formatearFechaDetalle(registro.fechaHora)
                                        }",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                is DetalleLineaUiState.Error -> {
                    item {
                        Text(
                            text = "Detalle de línea",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    item {
                        Text(
                            text = actual.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                viewModel.cargarHistorial(lineaId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
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

private fun nombreOrigenDetalle(origen: OrigenMedicion): String {
    return when (origen) {
        OrigenMedicion.MANUAL -> "Manual"
        OrigenMedicion.SIMULADA -> "Simulada"
    }
}

private fun formatearFechaDetalle(fechaHora: Long): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    )

    return formato.format(Date(fechaHora))
}