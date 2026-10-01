package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel

@Composable
fun FlushingScreen(navController: NavController, viewModel: FlushingViewModel = viewModel()) {

    LaunchedEffect(Unit) {
        viewModel.cargarFlushing(lineaId = 1)
    }

    val eventos = viewModel.flushingEvents.collectAsState().value

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Eventos de Flushing")

        Spacer(modifier = Modifier.height(12.dp))

        eventos.forEach { evento ->
            Text("Fecha: ${evento.fechaHora}")
            Text("Antes: ${evento.temperaturaAntes}°C")
            Text("Después: ${evento.temperaturaDespues}°C")
            Text("Duración: ${evento.duracionSegundos} segundos")
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
