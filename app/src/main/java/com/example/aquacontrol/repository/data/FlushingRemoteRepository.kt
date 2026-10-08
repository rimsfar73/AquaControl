package com.example.aquacontrol.repository.data

import com.example.aquacontrol.data.flushing.remote.FlushingApiClient
import com.example.aquacontrol.data.flushing.remote.FlushingApiService
import com.example.aquacontrol.model.flushing.FlushingDTO

class FlushingRemoteRepository(
    private val api: FlushingApiService = FlushingApiClient.service
) {

    suspend fun registrar(dto: FlushingDTO): Boolean {
        return api.registrarFlushing(dto)
    }

    suspend fun obtenerPorLinea(lineaId: Int): List<FlushingDTO> {
        return api.obtenerFlushingPorLinea(lineaId)
    }
}
