package com.example.aquacontrol.data.flushing.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FlushingDao {

    @Insert
    suspend fun insertarFlushing(evento: FlushingEntity): Long

    @Query(
        "SELECT * FROM flushing " +
                "WHERE lineaId = :lineaId " +
                "ORDER BY fechaHora DESC, id DESC"
    )
    suspend fun obtenerFlushingPorLinea(lineaId: Int): List<FlushingEntity>
}