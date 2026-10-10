package com.example.aquacontrol.iu.simulacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.viewmodel.simulacion.SimulacionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SimulacionScreen(
    navController: NavController,
    viewModel: SimulacionViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Simulación de temperaturas",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Genera mediciones de demostración para las líneas " +
                        "de agua. No corresponden a lecturas de sensores reales.",
                style = MaterialTheme.typography.bodyLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                text = "Se genera una lectura por línea aproximadamente " +
                        "cada cinco segundos. Las temperaturas recorren " +
                        "escenarios normales, de advertencia y críticos.",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Cada inicio comienza una nueva secuencia y reinicia " +
                        "el contador. Las mediciones anteriores permanecen " +
                        "guardadas con origen SIMULADA.",
                style = MaterialTheme.typography.bodyMedium
            )

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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar simulación")
            }

            OutlinedButton(
                onClick = viewModel::detener,
                enabled = estado.activa,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Detener simulación")
            }

            OutlinedButton(
                onClick = {
                    navController.navigate(Routes.ALERTAS) {
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver alertas")
            }

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

private fun formatearFechaSimulacion(fechaHora: Long): String {
    val formato = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    )

    return formato.format(Date(fechaHora))
}