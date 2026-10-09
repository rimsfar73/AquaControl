package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.aquacontrol.iu.navigation.Routes
import com.example.aquacontrol.viewmodel.flushing.FlushingUiState
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel

@Composable
fun FlushingScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: FlushingViewModel
) {
    val estado by viewModel.uiState.collectAsState()
    val backStackEntry = navController.currentBackStackEntry

    DisposableEffect(backStackEntry, lineaId, viewModel) {
        val lifecycle = backStackEntry?.lifecycle

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.cargarFlushing(lineaId)
            }
        }

        if (lifecycle != null) {
            lifecycle.addObserver(observer)
        } else {
            viewModel.cargarFlushing(lineaId)
        }

        onDispose {
            lifecycle?.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Historial de flushin",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Text(
                    text = "Procedimientos registrados para la línea seleccionada.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Button(
                    onClick = {
                        navController.navigate(
                            "${Routes.REGISTRAR_FLUSHING}/$lineaId"
                        ) {
                            launchSingleTop = true
                        }
                    },
                    enabled = lineaId > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar flushin")
                }
            }

            when (val actual = estado) {
                FlushingUiState.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is FlushingUiState.Success -> {
                    items(
                        items = actual.eventos,
                        key = { evento -> evento.id }
                    ) { evento ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Fecha: ${evento.fechaHora}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Temperatura antes: ${evento.temperaturaAntes} °C"
                                )

                                Text(
                                    text = "Temperatura después: ${evento.temperaturaDespues} °C"
                                )

                                Text(
                                    text = "Duración: ${evento.duracionSegundos} segundos"
                                )

                                Text(
                                    text = "Observación: ${
                                        evento.observacion.ifBlank {
                                            "Sin observación"
                                        }
                                    }"
                                )
                            }
                        }
                    }
                }

                FlushingUiState.Empty -> {
                    item {
                        Text(
                            text = "Esta línea todavía no tiene registros de flushin."
                        )
                    }
                }

                is FlushingUiState.Error -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = actual.mensaje,
                                color = MaterialTheme.colorScheme.error
                            )

                            OutlinedButton(
                                onClick = {
                                    viewModel.cargarFlushing(lineaId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }

            item {
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
}