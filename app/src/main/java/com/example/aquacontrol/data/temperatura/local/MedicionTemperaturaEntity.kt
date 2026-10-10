package com.example.aquacontrol.data.temperatura.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.aquacontrol.model.temperatura.OrigenMedicion

@Entity(
    tableName = "mediciones_temperatura",
    indices = [
        Index(value = ["lineaId", "fechaHora"])
    ]
)
data class MedicionTemperaturaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val lineaId: Int,

    val temperatura: Double,

    val fechaHora: Long,

    @ColumnInfo(defaultValue = "'MANUAL'")
    val origen: String = OrigenMedicion.MANUAL.name
)