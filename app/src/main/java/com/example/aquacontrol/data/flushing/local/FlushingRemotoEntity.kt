package com.example.aquacontrol.data.flushing.local

import androidx.room.Entity
import androidx.room.Index

// Conservamos una copia de los registros confirmados por el backend.
// Usamos otra tabla para no mezclar estos datos con el flushing local antiguo.
@Entity(
    tableName = "flushing_remoto",
    primaryKeys = ["servidor", "id"],
    indices = [
        Index(value = ["servidor", "lineaId", "fechaHora"])
    ]
)
data class FlushingRemotoEntity(

    // Identificamos el backend de origen para separar sus registros
    // si cambiamos la dirección del servidor utilizado por la app.
    val servidor: String,

    // Conservamos el ID recibido de PostgreSQL; Room no genera otro.
    val id: Long,

    // Conservamos la línea y la fecha recibidas del servidor.
    val lineaId: Long,
    val fechaHora: String,

    val duracionSegundos: Int,
    val observacion: String?,

    // Los registros antiguos del servidor pueden no tener temperaturas.
    // Conservamos null para no mostrar cero grados como si fuera una lectura.
    val temperaturaAntes: Double?,
    val temperaturaDespues: Double?,

    // Guardamos el nombre de la copia de la foto descargada, si existe.
    val fotoRuta: String? = null,

    // Registramos cuándo descargamos los datos para informar su antigüedad.
    val descargadoEn: Long
)