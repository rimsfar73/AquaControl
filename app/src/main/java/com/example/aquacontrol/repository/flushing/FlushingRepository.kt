package com.example.aquacontrol.repository.flushing

import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.flushing.FlushingDTO

interface FlushingRepository {

    suspend fun registrarFlushing(dto: FlushingDTO): EventoFlushing

    suspend fun obtenerFlushingPorLinea(lineaId: Int): List<EventoFlushing>
}