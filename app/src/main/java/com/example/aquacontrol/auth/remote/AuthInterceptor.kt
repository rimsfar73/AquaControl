
package com.example.aquacontrol.auth.remote

import com.example.aquacontrol.auth.AuthSessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Agrega el JWT a las solicitudes dirigidas
 * a AquaControl Backend.
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val solicitudOriginal = chain.request()

        val token = AuthSessionManager.obtenerToken()

        val solicitud = solicitudOriginal.newBuilder().apply {
            if (!token.isNullOrBlank()) {
                header("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(solicitud)
    }
}
