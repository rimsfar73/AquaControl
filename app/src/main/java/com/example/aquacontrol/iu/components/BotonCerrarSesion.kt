
package com.example.aquacontrol.iu.components


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color


@Composable
fun BotonCerrarSesion(
    onCerrarSesion: () -> Unit
) {
    var mostrarDialogo by rememberSaveable {
        mutableStateOf(false)
    }

    OutlinedButton(
        onClick = { mostrarDialogo = true }
    ) {
        Text("Cerrar sesión")
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false
            },
            title = {
                Text("Cerrar sesión")
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas cerrar sesión?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                        onCerrarSesion()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF15803D)
                    )
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        mostrarDialogo = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB91C1C)
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

