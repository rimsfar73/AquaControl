package com.example.aquacontrol.data.temperatura.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.aquacontrol.model.temperatura.OrigenMedicion

@Entity(
    tableName = "mediciones_temperatura",
    indices = [
        Index(value = ["lineaId", "fechaHora"]),
        Index(value = ["idServidor"], unique = true)
    ]
)
data class MedicionTemperaturaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val lineaId: Int,

    val temperatura: Double,

    val fechaHora: Long,

    @ColumnInfo(defaultValue = "'MANUAL'")
    val origen: String = OrigenMedicion.MANUAL.name,

    // ID asignado por PostgreSQL. El ID local sigue siendo la clave primaria de Room.
    val idServidor: Long? = null,

    // Conservamos la observación del backend para no perderla al editar la lectura.
    val observacion: String? = null
)