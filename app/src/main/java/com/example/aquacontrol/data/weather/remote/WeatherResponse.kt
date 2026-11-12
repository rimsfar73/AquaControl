package com.example.aquacontrol.data.weather.remote

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val fechaHora: String,
    val temperaturaExterior: Double,
    val humedadRelativa: Int,
    val fuente: String
)