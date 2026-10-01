package com.example.aquacontrol.iu.detalle

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

    val historial = viewModel.historial.collectAsState().value

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Detalle de Línea $lineaId")
        Spacer(modifier = Modifier.height(12.dp))

        historial.forEach { registro ->
            Text("Hora: ${registro.fechaHora}  |  Temp: ${registro.temperatura}°C")
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
