package com.example.aquacontrol.data.flushing

import com.example.aquacontrol.data.flushing.local.FlushingEntity

interface FlushingDataSource {
    suspend fun guardarFlushing(entity: FlushingEntity)
    suspend fun obtenerFlushingPorLinea(lineaId: Int): List<FlushingEntity>
}