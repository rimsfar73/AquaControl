package com.example.aquacontrol.data.weather.remote

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.aquacontrol.BuildConfig
import com.example.aquacontrol.data.remote.ApiClient

interface WeatherApiService {

    @GET("api/weather")
    suspend fun obtenerClima(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): WeatherResponse
}

object WeatherApiClient {
    // Comparte dirección y JWT con el resto de Retrofit. Sirve en emulador y teléfono.
    val service: WeatherApiService by lazy {
        ApiClient(BuildConfig.BACKEND_URL)
            .crearServicio(WeatherApiService::class.java)
    }
}
