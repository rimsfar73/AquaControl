package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.viewmodel.flushing.FlushingUiState
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel

@Composable fun FlushingScreen(
navController: NavController,
lineaId: Int,
viewModel: FlushingViewModel = viewModel()
) {
    LaunchedEffect(lineaId) {
        viewModel.cargarFlushing(lineaId)
    }

    val estado by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Eventos de Flushing")
        Spacer(modifier = Modifier.height(12.dp))

        when (val actual = estado) {
            FlushingUiState.Loading -> {
                CircularProgressIndicator()
            }

            is FlushingUiState.Success -> {
                actual.eventos.forEach { evento ->
                    Text("Fecha: ${evento.fechaHora}")
                    Text("Antes: ${evento.temperaturaAntes}°C")
                    Text("Después: ${evento.temperaturaDespues}°C")
                    Text("Duración: ${evento.duracionSegundos} segundos")
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            FlushingUiState.Empty -> {
                Text("Esta línea no tiene eventos de flushing registrados.")
            }

            is FlushingUiState.Error -> {
                Text(actual.mensaje)

                Button(
                    onClick = {
                        viewModel.cargarFlushing(lineaId)
                    }
                ) {
                    Text("Reintentar")
                }
            }
        }
    }
}
