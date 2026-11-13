
package com.example.aquacontrol.auth

import com.example.aquacontrol.auth.dto.LoginResponse

/**
 * Administra el JWT de la sesión activa.
 * No guarda credenciales ni persiste el token en disco.
 */
object AuthSessionManager {

    @Volatile
    private var accessToken: String? = null

    @Volatile
    private var expiresAtMillis: Long = 0L

    @Volatile
    private var rol: String? = null

    @Synchronized
    fun guardarSesion(respuesta: LoginResponse) {
        require(respuesta.accessToken.isNotBlank()) {
            "El JWT no puede estar vacío."
        }

        require(respuesta.tokenType.equals("Bearer", ignoreCase = true)) {
            "Tipo de token no compatible."
        }

        require(respuesta.expiresIn > 0) {
            "El JWT debe tener una duración válida."
        }

        accessToken = respuesta.accessToken
        rol = respuesta.rol

        expiresAtMillis = System.currentTimeMillis() +
                respuesta.expiresIn * 1000L
    }

    @Synchronized
    fun obtenerToken(): String? {
        if (System.currentTimeMillis() >= expiresAtMillis) {
            limpiarSesion()
            return null
        }

        return accessToken
    }

    @Synchronized
    fun obtenerRol(): String? {
        return if (obtenerToken() != null) rol else null
    }

    @Synchronized
    fun limpiarSesion() {
        accessToken = null
        expiresAtMillis = 0L
        rol = null
    }
}
