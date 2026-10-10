package com.example.aquacontrol.data.temperatura.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicionTemperaturaDao {

    @Insert
    suspend fun insertarMedicion(
        medicion: MedicionTemperaturaEntity
    ): Long

    @Query(
        """
        SELECT * FROM mediciones_temperatura
        WHERE lineaId = :lineaId
        ORDER BY fechaHora DESC, id DESC
        """
    )
    fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperaturaEntity>>

    @Query(
        """
        SELECT * FROM mediciones_temperatura
        WHERE lineaId = :lineaId
        ORDER BY fechaHora DESC, id DESC
        LIMIT 1
        """
    )
    fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperaturaEntity?>

    @Query(
        """
        SELECT m.*
        FROM mediciones_temperatura AS m
        WHERE m.id = (
            SELECT reciente.id
            FROM mediciones_temperatura AS reciente
            WHERE reciente.lineaId = m.lineaId
            ORDER BY reciente.fechaHora DESC, reciente.id DESC
            LIMIT 1
        )
        ORDER BY m.lineaId ASC
        """
    )
    fun observarUltimasMediciones():
            Flow<List<MedicionTemperaturaEntity>>
}