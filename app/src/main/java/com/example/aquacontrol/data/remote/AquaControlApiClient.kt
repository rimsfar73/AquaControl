package com.example.aquacontrol.data.remote

import com.example.aquacontrol.BuildConfig

object AquaControlApiClient {

    private val cliente by lazy {
        ApiClient(BuildConfig.BACKEND_URL)
    }

    val service: AquaControlApiService by lazy {
        cliente.crearServicio(AquaControlApiService::class.java)
    }
}