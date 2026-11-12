package com.example.aquacontrol.data.weather.remote

import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.gson.GsonConverterFactory

interface WeatherApiService {

    @GET("api/weather")
    suspend fun obtenerClima(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): WeatherResponse
}

object WeatherApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    val service: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}