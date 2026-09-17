package com.example.aquacontrol.model.linea

enum class EstadoLinea { NORMAL, ADVERTENCIA, CRITICO }

data class LineaBebedero(
    val id: Int,
    val nombre: String,
    val temperatura: Double,
    val estado: EstadoLinea,
    val actualizado: String
)
