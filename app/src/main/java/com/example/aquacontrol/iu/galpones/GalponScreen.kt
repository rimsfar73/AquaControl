package com.example.aquacontrol.iu.galpones

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
import com.example.aquacontrol.viewmodel.galpones.GalponUiState
import com.example.aquacontrol.viewmodel.galpones.GalponViewModel

@Composable
fun GalponScreen(
    navController: NavController,
    granjaId: Int,
    viewModel: GalponViewModel = viewModel()
) {
    LaunchedEffect(granjaId) {
        viewModel.cargarGalpones(granjaId)
    }

    val estado by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Galpones")
        Spacer(modifier = Modifier.height(12.dp))

        when (val actual = estado) {
            GalponUiState.Loading -> {
                CircularProgressIndicator()
            }

            is GalponUiState.Success -> {
                actual.galpones.forEach { galpon ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                navController.navigate("lineas/${galpon.id}")

                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Galpón: ${galpon.nombre}")
                            Text("ID: ${galpon.id}")
                        }
                    }
                }
            }

            GalponUiState.Empty -> {
                Text("Esta granja no tiene galpones registrados.")
            }

            is GalponUiState.Error -> {
                Text(actual.mensaje)

                Button(
                    onClick = {
                        viewModel.cargarGalpones(granjaId)
                    }
                ) {
                    Text("Reintentar")
                }
            }
        }
    }
}
