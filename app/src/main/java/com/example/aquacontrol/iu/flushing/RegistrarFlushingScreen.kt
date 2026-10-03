package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModel

@Composable
fun RegistrarFlushingScreen(
    navController: NavController,
    viewModel: RegistrarFlushingViewModel = viewModel()
) {
    var observacion by remember { mutableStateOf("") }
    var registrado by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {

        Text("Registrar Flushing", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = observacion,
            onValueChange = { observacion = it },
            label = { Text("Observación") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.registrarFlushing(observacion)
                registrado = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        Spacer(Modifier.height(16.dp))

        if (registrado) {
            Text("Flushing registrado correctamente", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }
}
