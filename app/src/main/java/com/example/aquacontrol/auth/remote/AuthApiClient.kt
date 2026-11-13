
package com.example.aquacontrol.auth.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Cliente Retrofit para AquaControl Backend.
 *
 * Configura la conexión HTTP y agrega automáticamente
 * el JWT mediante AuthInterceptor.
 */
class AuthApiClient(baseUrl: String) {

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> crearServicio(interfaz: Class<T>): T {
        return retrofit.create(interfaz)
    }
}
