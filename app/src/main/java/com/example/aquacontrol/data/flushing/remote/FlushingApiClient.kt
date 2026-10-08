package com.example.aquacontrol.data.flushing.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object FlushingApiClient {

    private const val BASE_URL = "https://TU_API/"

    val service: FlushingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FlushingApiService::class.java)
    }
}
