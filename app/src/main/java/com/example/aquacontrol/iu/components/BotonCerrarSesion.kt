
package com.example.aquacontrol.iu.components

import android.util.Log
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
import com.example.aquacontrol.auth.AuthSessionManager

@Composable
fun BotonCerrarSesion(
    onCerrarSesion: () -> Unit
) {
    var mostrarDialogo by rememberSaveable {
        mutableStateOf(false)
    }

    OutlinedButton(
        onClick = {
            mostrarDialogo = true

            Log.d(
                "JWT_TEST",
                "Solicitud de cierre de sesión"
            )
        }
    ) {
        Text("Cerrar sesión")
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogo = false

                Log.d(
                    "JWT_TEST",
                    "Diálogo de logout cerrado sin confirmar"
                )
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

                        Log.d(
                            "JWT_TEST",
                            "Logout confirmado"
                        )

                        // Delegamos el cierre real de sesión.
                        onCerrarSesion()

                        // Comprobamos si el callback eliminó el JWT.
                        Log.d(
                            "JWT_TEST",
                            "JWT después de logout: " +
                                    (AuthSessionManager.obtenerToken() != null)
                        )
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

                        Log.d(
                            "JWT_TEST",
                            "Logout cancelado. JWT conservado: " +
                                    (AuthSessionManager.obtenerToken() != null)
                        )
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
