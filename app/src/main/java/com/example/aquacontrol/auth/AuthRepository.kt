
package com.example.aquacontrol.auth

import com.example.aquacontrol.BuildConfig
import com.example.aquacontrol.auth.dto.LoginRequest
import com.example.aquacontrol.auth.dto.LoginResponse
import com.example.aquacontrol.auth.remote.AuthApiClient
import com.example.aquacontrol.auth.remote.AuthApiService
import retrofit2.HttpException
import java.io.IOException

class AuthRepository {

    private val api: AuthApiService by lazy {
        val url = BuildConfig.BACKEND_URL

        require(url.isNotBlank()) {
            "Configura AQUA_BACKEND_URL en Gradle."
        }

        AuthApiClient(url).crearServicio(AuthApiService::class.java)
    }

    suspend fun ingresar(
        username: String,
        password: String
    ): Result<LoginResponse> {

        return try {
            val respuesta = api.login(
                LoginRequest(
                    username = username.trim(),
                    password = password
                )
            )

            if (
                respuesta.accessToken.isBlank() ||
                !respuesta.tokenType.equals(
                    "Bearer",
                    ignoreCase = true
                ) ||
                respuesta.expiresIn <= 0
            ) {
                Result.failure(
                    IllegalStateException(
                        "Respuesta de autenticación inválida."
                    )
                )
            } else {
                Result.success(respuesta)
            }

        } catch (e: HttpException) {
            val mensaje = when (e.code()) {
                400 -> "Solicitud de acceso inválida."
                401 -> "Usuario o contraseña incorrectos."
                403 -> "Acceso denegado."
                else -> "Error del servidor (${e.code()})."
            }

            Result.failure(IllegalStateException(mensaje))

        } catch (e: IOException) {
            Result.failure(
                IOException(
                    "No se pudo conectar con AquaControl Backend.",
                    e
                )
            )

        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
