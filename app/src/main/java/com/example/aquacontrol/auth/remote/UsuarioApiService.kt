
package com.example.aquacontrol.auth.remote

import com.example.aquacontrol.auth.dto.UsuarioPerfilResponse
import retrofit2.http.GET

/**
 * Consulta el perfil del usuario autenticado.
 *
 * El endpoint requiere un JWT válido.
 * AuthInterceptor agrega el token automáticamente.
 */
interface UsuarioApiService {

    @GET("api/v1/usuarios/me")
    suspend fun obtenerMiPerfil(): UsuarioPerfilResponse
}
