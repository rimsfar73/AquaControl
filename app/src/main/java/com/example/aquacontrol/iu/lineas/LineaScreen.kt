package com.example.aquacontrol.iu.lineas

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
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.viewmodel.lineas.LineaUiState
import com.example.aquacontrol.viewmodel.lineas.LineaViewModel

@Composable
fun LineaScreen(
    navController: NavController,
    viewModel: LineaViewModel = viewModel()
) {
    // Se reemplazará por el ID recibido desde la navegación.
    val galponId = 2

    LaunchedEffect(galponId) {
        viewModel.cargarLineas(galponId)
    }

    val estado by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Líneas de bebederos")
        Spacer(modifier = Modifier.height(8.dp))

        when (val actual = estado) {
            LineaUiState.Loading -> {
                CircularProgressIndicator()
            }

            is LineaUiState.Success -> {
                actual.lineas.forEach { linea ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate(Routes.DETALLE_LINEA)
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "${linea.nombre} – " +
                                        "${linea.temperatura} °C – ${linea.estado}"
                            )
                            Text("Actualizado: ${linea.actualizado}")
                        }
                    }
                }
            }

            LineaUiState.Empty -> {
                Text("Este galpón no tiene líneas registradas.")
            }

            is LineaUiState.Error -> {
                Text(actual.mensaje)

                Button(
                    onClick = {
                        viewModel.cargarLineas(galponId)
                    }
                ) {
                    Text("Reintentar")
                }
            }
        }
    }
}