package com.example.aquacontrol.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Recibimos la dirección del backend para poder elegirla según dónde probamos la app.
class ApiClient(baseUrl: String) {

    // Configuramos Retrofit con la dirección del servidor y usamos Gson
    // para convertir nuestros DTO a JSON al enviar datos y leerlos al recibirlos.
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Pasamos una interfaz, como FlushingApiService, y Retrofit usa sus
    // anotaciones para saber qué ruta llamar y qué datos enviar.
    fun <T> crearServicio(interfaz: Class<T>): T {
        return retrofit.create(interfaz)
    }
}