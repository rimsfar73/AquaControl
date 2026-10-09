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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.viewmodel.alertas.AlertaLinea
import com.example.aquacontrol.viewmodel.alertas.AlertasUiState
import com.example.aquacontrol.viewmodel.alertas.AlertasViewModel

@Composable
fun AlertasScreen(
    navController: NavController,
    viewModel: AlertasViewModel = viewModel()
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
                    text = "Líneas con temperaturas fuera del rango normal.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Text(
                    text = "Datos de demostración. Las fechas corresponden a las mediciones de ejemplo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        it.linea.estado == EstadoLinea.CRITICO
                    }

                    val advertencias = actual.alertas.count {
                        it.linea.estado == EstadoLinea.ADVERTENCIA
                    }

                    item {
                        Text(
                            text = "Críticas: $criticas · Advertencias: $advertencias",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(
                        items = actual.alertas,
                        key = { it.linea.id }
                    ) { alerta ->
                        TarjetaAlerta(
                            alerta = alerta,
                            onVerLinea = {
                                navController.navigate(
                                    "${Routes.DETALLE_LINEA}/${alerta.linea.id}"
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }

                AlertasUiState.Empty -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No se encontraron líneas en advertencia o estado crítico en los datos consultados.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                is AlertasUiState.Error -> {
                    item {
                        Text(
                            text = actual.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = viewModel::cargarAlertas,
                    enabled = estado !is AlertasUiState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (estado is AlertasUiState.Error) {
                            "Reintentar"
                        } else {
                            "Actualizar alertas"
                        }
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
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
    val esCritica = alerta.linea.estado == EstadoLinea.CRITICO

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

    val titulo = when (alerta.linea.estado) {
        EstadoLinea.CRITICO -> "ALERTA CRÍTICA"
        EstadoLinea.ADVERTENCIA -> "ADVERTENCIA"
        EstadoLinea.NORMAL -> "NORMAL"
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
                text = alerta.linea.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Temperatura: ${alerta.linea.temperatura} °C",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Última medición: ${alerta.linea.actualizado}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = if (esCritica) {
                    "Revisa esta línea con prioridad y verifica la medición."
                } else {
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