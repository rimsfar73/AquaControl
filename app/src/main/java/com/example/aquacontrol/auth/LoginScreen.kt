package com.example.aquacontrol.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aquacontrol.BuildConfig
import com.example.aquacontrol.R
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

    // Procesa la respuesta del servidor y conserva el flujo JWT existente.
    LaunchedEffect(estado.respuesta) {
        val respuesta = estado.respuesta ?: return@LaunchedEffect

        val perfil = when (respuesta.rol) {
            "ROLE_OPERARIO" -> PerfilUsuario.OPERARIO
            "ROLE_SUPERVISOR" -> PerfilUsuario.SUPERVISOR
            else -> null
        }

        if (perfil != null) {
            AuthSessionManager.guardarSesion(respuesta)

            Log.d(
                "JWT_TEST",
                "JWT almacenado: ${AuthSessionManager.obtenerToken() != null}"
            )

            try {
                val servicio = AuthApiClient(BuildConfig.BACKEND_URL)
                    .crearServicio(UsuarioApiService::class.java)
                val usuarioBackend = servicio.obtenerMiPerfil()

                Log.d("JWT_TEST", "Endpoint protegido: HTTP 200")
                Log.d("JWT_TEST", "Usuario: ${usuarioBackend.username}")
                Log.d("JWT_TEST", "Rol: ${usuarioBackend.rol}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    "JWT_TEST",
                    "Error al consultar endpoint protegido",
                    e
                )
            }

            loginViewModel.limpiarResultado()
            clave = ""
            onLogin(usuario.trim(), perfil)
        }
    }

    val azulAqua = Color(0xFF082C42)
    val rojoAriztia = Color(0xFFE1241B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(azulAqua)
    ) {
        Image(
            painter = painterResource(R.drawable.fondo_bienvenida),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                // Mantiene la proporción panorámica original (740 × 230).
                // Así se ve completa y no se amplía para llenar una franja alta.
                .aspectRatio(740f / 230f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            azulAqua.copy(alpha = 0.18f),
                            azulAqua.copy(alpha = 0.92f),
                            azulAqua
                        )
                    )
                )
        )

        // Esta columna coloca el logo, los textos y el formulario en vertical.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo_ariztia_transparente),
                contentDescription = "Ariztía, hecho en casa",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = 240.dp, height = 180.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AquaControl",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Monitoreo y control de operaciones",
                color = Color.White.copy(alpha = 0.90f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.97f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Iniciar sesión",
                        color = azulAqua,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text("Ingresa con tu cuenta para continuar.")

                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { usuario = it },
                        label = { Text("Usuario") },
                        singleLine = true,
                        enabled = !estado.cargando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        enabled = !estado.cargando,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    estado.error?.let { mensaje ->
                        Text(
                            text = mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
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
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (estado.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(28.dp),
                            color = rojoAriztia,
                            strokeWidth = 3.dp
                        )
                    }

                    Button(
                        onClick = {
                            loginViewModel.ingresar(
                                username = usuario,
                                password = clave
                            )
                        },
                        enabled = !estado.cargando,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = rojoAriztia
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Text(
                            text = if (estado.cargando) {
                                "Validando..."
                            } else {
                                "Ingresar"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Acceso para personal autorizado",
                color = Color.White.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
