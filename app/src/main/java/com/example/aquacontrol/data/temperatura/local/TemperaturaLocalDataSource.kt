package com.example.aquacontrol.data.temperatura.local

import kotlinx.coroutines.flow.Flow

class TemperaturaLocalDataSource(
    private val dao: MedicionTemperaturaDao
) {

    suspend fun guardarMedicion(
        medicion: MedicionTemperaturaEntity
    ): Long {
        return dao.insertarMedicion(medicion)
    }

    fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperaturaEntity>> {
        return dao.observarHistorialPorLinea(lineaId)
    }

    fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperaturaEntity?> {
        return dao.observarUltimaMedicionPorLinea(lineaId)
    }

    fun observarUltimasMediciones():
            Flow<List<MedicionTemperaturaEntity>> {
        return dao.observarUltimasMediciones()
    }
}