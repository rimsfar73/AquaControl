
package com.example.aquacontrol.auth

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquacontrol.BuildConfig
import com.example.aquacontrol.auth.remote.AuthApiClient
import com.example.aquacontrol.auth.remote.UsuarioApiService
import com.example.aquacontrol.model.perfil.PerfilUsuario
import kotlinx.coroutines.CancellationException

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLogin: (String, PerfilUsuario) -> Unit
) {

    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }

    val estado by loginViewModel.uiState.collectAsState()

    // Procesamos el resultado cuando Spring Boot autentica al usuario.
    LaunchedEffect(estado.respuesta) {

        val respuesta = estado.respuesta
            ?: return@LaunchedEffect

        val perfil = when (respuesta.rol) {
            "ROLE_OPERARIO" -> PerfilUsuario.OPERARIO
            "ROLE_SUPERVISOR" -> PerfilUsuario.SUPERVISOR
            else -> null
        }

        if (perfil != null) {

            // PASO 1: Guardamos el JWT en memoria.
            AuthSessionManager.guardarSesion(respuesta)

            Log.d(
                "JWT_TEST",
                "JWT almacenado: ${AuthSessionManager.obtenerToken() != null}"
            )

            // PASO 2: Consultamos un endpoint protegido con Retrofit.
            try {

                val servicio = AuthApiClient(BuildConfig.BACKEND_URL)
                    .crearServicio(UsuarioApiService::class.java)

                val usuarioBackend = servicio.obtenerMiPerfil()

                Log.d(
                    "JWT_TEST",
                    "Endpoint protegido: HTTP 200"
                )

                Log.d(
                    "JWT_TEST",
                    "Usuario: ${usuarioBackend.username}"
                )

                Log.d(
                    "JWT_TEST",
                    "Rol: ${usuarioBackend.rol}"
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {

                Log.e(
                    "JWT_TEST",
                    "Error al consultar endpoint protegido",
                    e
                )
            }

            // PASO 3: Limpiamos el resultado del login.
            loginViewModel.limpiarResultado()
            clave = ""

            // PASO 4: Navegamos sin modificar la lógica existente.
            onLogin(usuario.trim(), perfil)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "AquaControl · Iniciar sesión",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = {
                usuario = it
            },
            label = {
                Text("Usuario")
            },
            singleLine = true,
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = {
                clave = it
            },
            label = {
                Text("Contraseña")
            },
            singleLine = true,
            enabled = !estado.cargando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        if (estado.cargando) {
            CircularProgressIndicator()
        }

        estado.error?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (
            estado.respuesta != null &&
            estado.respuesta?.rol !in listOf(
                "ROLE_OPERARIO",
                "ROLE_SUPERVISOR"
            )
        ) {
            Text(
                text = "El usuario no tiene un rol compatible.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                loginViewModel.ingresar(
                    username = usuario,
                    password = clave
                )
            },
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (estado.cargando) {
                    "Validando..."
                } else {
                    "Ingresar"
                }
            )
        }
    }
}
