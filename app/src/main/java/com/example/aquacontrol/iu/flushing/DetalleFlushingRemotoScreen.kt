package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingRemotoUiState
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingRemotoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetalleFlushingRemotoScreen(
    lineaId: Long,
    registroId: Long,
    viewModel: DetalleFlushingRemotoViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    // Consultamos exactamente el registro indicado por la navegación.
    LaunchedEffect(lineaId, registroId, viewModel) {
        viewModel.cargar(lineaId, registroId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AquaEncabezado(titulo = "Detalle de flushing")
            }

            when (val actual = estado) {
                DetalleFlushingRemotoUiState.Cargando -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                DetalleFlushingRemotoUiState.NoEncontrado -> {
                    item {
                        Text(
                            text = "Este registro ya no está disponible en el servidor.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                is DetalleFlushingRemotoUiState.Error -> {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = actual.mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            OutlinedButton(
                                onClick = {
                                    viewModel.cargar(lineaId, registroId)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                is DetalleFlushingRemotoUiState.Disponible -> {
                    val registro = actual.registro

                    // Avisamos cuando no pudimos comprobar los datos actuales.
                    if (actual.desdeCopiaLocal) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Mostrando copia local",
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "No se pudo conectar con el servidor. " +
                                                "Los datos pueden haber cambiado.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "Descargado: " +
                                                fechaDescargaFlushing(registro.descargadoEn),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Conservamos la fecha del backend sin
                                // convertirla a otra zona horaria.
                                CampoDetalleFlushingRemoto(
                                    titulo = "Fecha y hora",
                                    valor = registro.fechaHora.replace('T', ' ')
                                )

                                CampoDetalleFlushingRemoto(
                                    titulo = "Temperatura antes",
                                    valor = registro.temperaturaAntes
                                        ?.let { "$it °C" }
                                        ?: "Sin dato"
                                )

                                CampoDetalleFlushingRemoto(
                                    titulo = "Temperatura después",
                                    valor = registro.temperaturaDespues
                                        ?.let { "$it °C" }
                                        ?: "Sin dato"
                                )

                                CampoDetalleFlushingRemoto(
                                    titulo = "Duración",
                                    valor = "${registro.duracionSegundos} segundos"
                                )

                                CampoDetalleFlushingRemoto(
                                    titulo = "Observación",
                                    valor = registro.observacion
                                        ?.takeIf { it.isNotBlank() }
                                        ?: "Sin observación"
                                )
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                viewModel.cargar(lineaId, registroId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Actualizar")
                        }
                    }
                }
            }
        }
    }
}

// Reutilizamos la misma presentación para cada dato del detalle.
@Composable
private fun CampoDetalleFlushingRemoto(
    titulo: String,
    valor: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// La descarga sí representa un instante: mostramos su hora local.
private fun fechaDescargaFlushing(instante: Long): String {
    return SimpleDateFormat(
        "dd/MM/yyyy HH:mm",
        Locale.getDefault()
    ).format(Date(instante))
}