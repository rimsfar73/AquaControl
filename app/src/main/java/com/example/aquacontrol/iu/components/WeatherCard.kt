
package com.example.aquacontrol.iu.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquacontrol.viewmodel.weather.WeatherUiState
import com.example.aquacontrol.viewmodel.weather.WeatherViewModel

@Composable
fun WeatherCard(
    viewModel: WeatherViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    AquaPanelResumen(
        etiqueta = "CONDICIONES METEOROLÓGICAS"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (val actual = estado) {

                WeatherUiState.Loading -> {
                    CircularProgressIndicator()
                    Text("Consultando condiciones exteriores...")
                }

                is WeatherUiState.Success -> {
                    val clima = actual.clima

                    Text(
                        text = "${clima.temperaturaExterior} °C",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Humedad relativa: ${clima.humedadRelativa}%"
                    )

                    Text(
                        text = "Fecha y hora: ${clima.fechaHora.replace('T', ' ')}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Fuente: ${clima.fuente}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    OutlinedButton(
                        onClick = viewModel::cargarClima,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Actualizar")
                    }
                }

                is WeatherUiState.Error -> {
                    Text(
                        text = actual.mensaje,
                        color = MaterialTheme.colorScheme.error
                    )

                    OutlinedButton(
                        onClick = viewModel::cargarClima,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}
