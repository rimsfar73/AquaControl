package com.example.aquacontrol.data.flushing.remote

import com.example.aquacontrol.model.flushing.FlushingDTO

class FlushingRemoteDataSource(
    private val api: FlushingApiService
) {

    suspend fun registrarFlushingRemoto(dto: FlushingDTO): Boolean {
        return api.registrarFlushing(dto)
    }

    suspend fun obtenerFlushingPorLineaRemoto(lineaId: Int): List<FlushingDTO> {
        return api.obtenerFlushingPorLinea(lineaId)
    }
}
