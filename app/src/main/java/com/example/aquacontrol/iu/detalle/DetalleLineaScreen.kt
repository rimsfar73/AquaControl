package com.example.aquacontrol.iu.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaUiState
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel

@Composable
fun DetalleLineaScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: DetalleLineaViewModel = viewModel()
) {
    LaunchedEffect(lineaId) {
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
                                    text = "Última temperatura registrada",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "${actual.linea.temperatura} °C",
                                    style = MaterialTheme.typography.headlineMedium
                                )

                                val nombreEstado = when (actual.linea.estado) {
                                    EstadoLinea.NORMAL -> "Normal"
                                    EstadoLinea.ADVERTENCIA -> "Advertencia"
                                    EstadoLinea.CRITICO -> "Crítico"
                                }

                                Text(
                                    text = "Estado: $nombreEstado"
                                )

                                Text(
                                    text = "Actualizado: ${actual.linea.actualizado}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
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
                            Text("Ver historial de flushin")
                        }
                    }

                    item {
                        Text(
                            text = "Historial de temperaturas",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    if (actual.historial.isEmpty()) {
                        item {
                            Text(
                                text = "Esta línea no tiene mediciones históricas registradas."
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
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "Fecha y hora: ${registro.fechaHora}",
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