package com.example.aquacontrol.iu.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Detalle de Línea $lineaId")
        Spacer(modifier = Modifier.height(12.dp))

        when (val actual = estado) {
            DetalleLineaUiState.Loading -> {
                CircularProgressIndicator()
            }

            is DetalleLineaUiState.Success -> {
                actual.historial.forEach { registro ->
                    Text(
                        "Hora: ${registro.fechaHora} | " +
                                "Temp: ${registro.temperatura}°C"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            DetalleLineaUiState.Empty -> {
                Text("Esta línea no tiene temperaturas registradas.")
            }

            is DetalleLineaUiState.Error -> {
                Text(actual.mensaje)

                Button(
                    onClick = {
                        viewModel.cargarHistorial(lineaId)
                    }
                ) {
                    Text("Reintentar")
                }
            }
        }
    }
}