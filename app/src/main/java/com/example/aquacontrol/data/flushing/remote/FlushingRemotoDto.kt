package com.example.aquacontrol.data.flushing.remote

// Formato que intercambia Retrofit con /api/v1/flushing en el backend.
data class FlushingRemotoDto(
    // Al crear un registro se omite el ID. El servidor lo asigna y lo devuelve.
    val id: Long? = null,
    val lineaId: Long,

    // Fecha y hora local que espera Spring Boot. Ejemplo: 2026-11-03T10:30:00
    val fechaHora: String,
    val duracionSegundos: Int,
    val observacion: String? = null
)