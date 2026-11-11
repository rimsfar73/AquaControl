package com.example.aquacontrol.data.weather

import com.example.aquacontrol.data.weather.remote.WeatherApiClient
import com.example.aquacontrol.data.weather.remote.WeatherResponse

class WeatherRepository {

    suspend fun obtenerClima(
        latitude: Double,
        longitude: Double
    ): WeatherResponse {
        return WeatherApiClient.service.obtenerClima(
            latitude = latitude,
            longitude = longitude
        )
    }
}