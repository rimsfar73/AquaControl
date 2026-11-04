package com.example.aquacontrol.data.flushing.remote

import com.example.aquacontrol.BuildConfig
import com.example.aquacontrol.data.remote.ApiClient

// Conectamos el repositorio de flushing con las rutas que definimos en FlushingApiService.
object FlushingApiClient {

    // Con lazy creamos el servicio la primera vez que lo necesitamos.
    // Después reutilizamos el mismo servicio para consultar, guardar, editar y eliminar.
    val service: FlushingApiService by lazy {
        val url = BuildConfig.BACKEND_URL

        // Si falta la dirección, indicamos qué propiedad debemos configurar en Gradle.
        require(url.isNotBlank()) {
            "Configura AQUA_BACKEND_URL en Gradle para conectar AquaControl con el servidor."
        }

        ApiClient(url).crearServicio(FlushingApiService::class.java)
    }
}