package com.example.aquacontrol.data.flushing.local

class FlushingLocalDataSource(
    private val dao: FlushingDao
) {

    suspend fun guardarFlushing(entity: FlushingEntity): Long {
        return dao.insertarFlushing(entity)
    }

    suspend fun obtenerFlushingPorLinea(lineaId: Int): List<FlushingEntity> {
        return dao.obtenerFlushingPorLinea(lineaId)
    }
}