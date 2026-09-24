package com.example.aquacontrol.model.linea

import com.example.aquacontrol.model.estado.EstadoLinea

data class LineaBebedero(
    val id: Int,
    val nombre: String,
    val temperatura: Double,
    val estado: EstadoLinea,
    val actualizado: String
)