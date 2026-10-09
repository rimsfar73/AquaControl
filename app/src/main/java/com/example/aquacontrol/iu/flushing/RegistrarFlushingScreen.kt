package com.example.aquacontrol.iu.flushing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModel

@Composable
fun RegistrarFlushingScreen(
    navController: NavController,
    lineaId: Int,
    viewModel: RegistrarFlushingViewModel
) {
    val estado by viewModel.uiState.collectAsState()

    LaunchedEffect(lineaId) {
        if (estado.lineaId != lineaId) {
            viewModel.seleccionarLinea(lineaId)
        }
    }

    val registrado = estado.eventoGuardado != null
    val editable = !estado.guardando && !registrado

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registrar flushing",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Completa los datos del procedimiento realizado. " +
                        "Todos los campos son obligatorios.",
                style = MaterialTheme.typography.bodyMedium
            )

            estado.errorLinea?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            CampoFlushing(
                valor = estado.observacion,
                alCambiar = viewModel::actualizarObservacion,
                etiqueta = "Observación",
                error = estado.errorObservacion,
                habilitado = editable,
                unaLinea = false
            )

            CampoFlushing(
                valor = estado.temperaturaAntes,
                alCambiar = viewModel::actualizarTemperaturaAntes,
                etiqueta = "Temperatura antes (°C)",
                error = estado.errorTemperaturaAntes,
                habilitado = editable,
                teclado = KeyboardType.Decimal
            )

            CampoFlushing(
                valor = estado.temperaturaDespues,
                alCambiar = viewModel::actualizarTemperaturaDespues,
                etiqueta = "Temperatura después (°C)",
                error = estado.errorTemperaturaDespues,
                habilitado = editable,
                teclado = KeyboardType.Decimal
            )

            CampoFlushing(
                valor = estado.duracionSegundos,
                alCambiar = viewModel::actualizarDuracion,
                etiqueta = "Duración en segundos",
                error = estado.errorDuracion,
                habilitado = editable,
                teclado = KeyboardType.Number
            )

            Button(
                onClick = { viewModel.registrarFlushing() },
                enabled = editable,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(Modifier.width(8.dp))
                    Text("Guardando…")
                } else {
                    Text(
                        if (registrado) "Registro guardado"
                        else "Guardar flushing"
                    )
                }
            }

            estado.errorGuardado?.let { mensaje ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error de guardado"
                        )

                        Text(
                            text = mensaje,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (registrado) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Registro guardado"
                        )

                        Text(
                            text = "Flushing guardado correctamente.",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
private fun CampoFlushing(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String?,
    habilitado: Boolean,
    teclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        enabled = habilitado,
        isError = error != null,
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        keyboardOptions = KeyboardOptions(
            keyboardType = teclado
        ),
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        trailingIcon = {
            if (error != null) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Campo con error"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}