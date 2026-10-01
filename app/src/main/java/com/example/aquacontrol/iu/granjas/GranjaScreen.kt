package com.example.aquacontrol.iu.granjas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aquacontrol.viewmodel.granjas.GranjaUiState
import com.example.aquacontrol.viewmodel.granjas.GranjaViewModel

@Composable
fun GranjaScreen(
    navController: NavController,
    viewModel: GranjaViewModel = viewModel()
) {
    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Granjas")
        Spacer(modifier = Modifier.height(8.dp))

        when (val actual = estado) {
            GranjaUiState.Loading -> {
                CircularProgressIndicator()
            }

            is GranjaUiState.Success -> {
                actual.granjas.forEach { granja ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate("galpones/${granja.id}")

                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(granja.nombre)
                        }
                    }
                }
            }

            GranjaUiState.Empty -> {
                Text("No hay granjas disponibles.")
            }

            is GranjaUiState.Error -> {
                Text(actual.mensaje)

                Button(
                    onClick = { viewModel.cargarGranjas() }
                ) {
                    Text("Reintentar")
                }
            }
        }
    }
}