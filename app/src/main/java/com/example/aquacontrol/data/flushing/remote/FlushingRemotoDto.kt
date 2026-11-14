package com.example.aquacontrol.data.flushing.remote

// Intercambiamos estos datos con /api/v1/flushing.
// Los nombres coinciden con FlushingDTO del backend.
data class FlushingRemotoDto(

    // Al crear enviamos el ID vacío; PostgreSQL lo genera
    // y el backend lo devuelve para consultar o modificar el registro.
    val id: Long? = null,

    // Identificamos la línea a la que pertenece el flushing.
    val lineaId: Long,

    // Enviamos la fecha en el formato que recibe Spring Boot.
    // Ejemplo: 2026-11-13T15:30:00.
    val fechaHora: String,

    // Registramos cuánto duró el procedimiento, en segundos.
    val duracionSegundos: Int,

    // Compartimos la observación con el backend.
    // El servidor admite un máximo de 500 caracteres.
    val observacion: String? = null,

    // Incluimos las temperaturas antes y después del flushing.
    // Admitimos null para leer registros antiguos sin estos datos;
    // una temperatura desconocida no equivale a cero grados.
    val temperaturaAntes: Double? = null,
    val temperaturaDespues: Double? = null
)