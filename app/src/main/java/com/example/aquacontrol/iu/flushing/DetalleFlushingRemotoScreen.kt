package com.example.aquacontrol.iu.flushing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import com.example.aquacontrol.viewmodel.flushing.EdicionFlushingUiState
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.iu.components.AquaEncabezado
import com.example.aquacontrol.viewmodel.flushing.FotoDetalleFlushingUiState
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingRemotoUiState
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingRemotoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun DetalleFlushingRemotoScreen(
    lineaId: Long,
    registroId: Long,
    viewModel: DetalleFlushingRemotoViewModel,
    puedeEditar: Boolean = false,
    puedeEliminar: Boolean = false,
    alVolver: () -> Unit = {}
) {
    val estado by viewModel.uiState.collectAsState()
    val disponible = estado as? DetalleFlushingRemotoUiState.Disponible
    val edicion = disponible?.edicion

    var confirmarEliminacion by rememberSaveable(lineaId, registroId) {
        mutableStateOf(false)
    }

    BackHandler(
        enabled = edicion?.guardando == true || disponible?.eliminando == true
    ) { }

    if (
        confirmarEliminacion && puedeEliminar && disponible != null &&
        disponible.edicion == null && !disponible.desdeCopiaLocal
    ) {
        AlertDialog(
            onDismissRequest = { confirmarEliminacion = false },
            title = { Text("Eliminar flushing") },
            text = {
                Text("Se eliminará este registro y su foto del servidor. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmarEliminacion = false
                        viewModel.eliminarRegistro()
                    },
                    enabled = !disponible.eliminando
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (puedeEditar && edicion != null) {
        DialogoEdicionFlushing(edicion = edicion, viewModel = viewModel)
    }

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

                is DetalleFlushingRemotoUiState.Eliminado -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(actual.mensaje)
                            OutlinedButton(
                                onClick = alVolver,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Volver al historial")
                            }
                        }
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

                    actual.mensajeEdicion?.let { mensaje ->
                        item {
                            Text(
                                text = mensaje,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    if (puedeEditar) {
                        item {
                            OutlinedButton(
                                onClick = { viewModel.iniciarEdicion() },
                                enabled = !actual.desdeCopiaLocal && actual.edicion == null && !actual.eliminando,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Editar flushing")
                            }
                            if (actual.desdeCopiaLocal) {
                                Text("Actualiza con conexión para poder editar este registro.")
                            }
                        }
                    }

                    if (puedeEliminar) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { confirmarEliminacion = true },
                                    enabled = !actual.desdeCopiaLocal &&
                                            actual.edicion == null && !actual.eliminando,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (actual.eliminando) "Eliminando…" else "Eliminar flushing",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                if (actual.eliminando) {
                                    CircularProgressIndicator()
                                }
                                if (actual.desdeCopiaLocal) {
                                    Text("Actualiza con conexión antes de eliminar este registro.")
                                }
                                actual.errorEliminacion?.let { mensaje ->
                                    Text(text = mensaje, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    item {
                        FotoDelDetalleRemoto(
                            estado = actual.foto,
                            alReintentar = { viewModel.reintentarFoto() },
                            habilitado = !actual.eliminando
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                viewModel.cargar(lineaId, registroId)
                            },
                            enabled = actual.edicion == null && !actual.eliminando,
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
    ).apply { timeZone = TimeZone.getTimeZone("America/Santiago") }
        .format(Date(instante))
}

// Mostramos la evidencia sin reemplazar los datos cuando falla su consulta.
@Composable
private fun FotoDelDetalleRemoto(
    estado: FotoDetalleFlushingUiState,
    alReintentar: () -> Unit,
    habilitado: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Foto de evidencia",
            style = MaterialTheme.typography.titleMedium
        )

        when (estado) {
            FotoDetalleFlushingUiState.Cargando -> {
                CircularProgressIndicator()
                Text("Cargando foto…")
            }

            is FotoDetalleFlushingUiState.Disponible -> {
                FotoFlushing(fotoRuta = estado.nombreArchivo)

                if (estado.desdeCopiaLocal) {
                    Text(
                        text = "Mostrando la foto descargada anteriormente. " +
                                "No pudimos comprobar si cambió en el servidor.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = alReintentar,
                        enabled = habilitado,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Actualizar foto")
                    }
                }
            }

            FotoDetalleFlushingUiState.SinFoto -> {
                Text("No hay una foto disponible para este registro en el servidor.")
                OutlinedButton(
                    onClick = alReintentar,
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Consultar nuevamente")
                }
            }

            FotoDetalleFlushingUiState.SinCopiaLocal -> {
                Text(
                    "No pudimos consultar la foto y no hay una copia descargada. " +
                            "Conéctate al backend para intentar obtenerla."
                )
                OutlinedButton(
                    onClick = alReintentar,
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reintentar foto")
                }
            }

            is FotoDetalleFlushingUiState.Error -> {
                Text(
                    text = estado.mensaje,
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedButton(
                    onClick = alReintentar,
                    enabled = habilitado,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reintentar foto")
                }
            }
        }
    }
}

// Editamos solo los campos permitidos; conservamos fecha, línea y foto.
@Composable
private fun DialogoEdicionFlushing(
    edicion: EdicionFlushingUiState,
    viewModel: DetalleFlushingRemotoViewModel
) {
    AlertDialog(
        onDismissRequest = {
            if (!edicion.guardando) viewModel.cancelarEdicion()
        },
        title = { Text("Editar flushing") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampoEdicionFlushing(
                    etiqueta = "Observación",
                    valor = edicion.observacion,
                    alCambiar = { viewModel.editarObservacion(it) },
                    habilitado = !edicion.guardando,
                    variasLineas = true
                )
                CampoEdicionFlushing(
                    etiqueta = "Duración en segundos",
                    valor = edicion.duracion,
                    alCambiar = { viewModel.editarDuracion(it) },
                    habilitado = !edicion.guardando,
                    teclado = KeyboardType.Number
                )
                CampoEdicionFlushing(
                    etiqueta = "Temperatura antes (°C)",
                    valor = edicion.temperaturaAntes,
                    alCambiar = { viewModel.editarTemperaturaAntes(it) },
                    habilitado = !edicion.guardando,
                    teclado = KeyboardType.Decimal
                )
                CampoEdicionFlushing(
                    etiqueta = "Temperatura después (°C)",
                    valor = edicion.temperaturaDespues,
                    alCambiar = { viewModel.editarTemperaturaDespues(it) },
                    habilitado = !edicion.guardando,
                    teclado = KeyboardType.Decimal
                )
                Text("Una temperatura vacía quedará como sin dato.")
                edicion.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                if (edicion.guardando) {
                    CircularProgressIndicator()
                    Text("Guardando cambios en el servidor…")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.guardarEdicion() },
                enabled = !edicion.guardando
            ) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { viewModel.cancelarEdicion() },
                enabled = !edicion.guardando
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CampoEdicionFlushing(
    etiqueta: String,
    valor: String,
    alCambiar: (String) -> Unit,
    habilitado: Boolean,
    teclado: KeyboardType = KeyboardType.Text,
    variasLineas: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        enabled = habilitado,
        singleLine = !variasLineas,
        minLines = if (variasLineas) 2 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}
