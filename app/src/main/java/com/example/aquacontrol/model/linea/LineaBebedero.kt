package com.example.aquacontrol.model.linea

import com.example.aquacontrol.model.estado.EstadoLinea

data class LineaBebedero(
    val id: Int,
    val nombre: String,
    val temperatura: Double,
    val actualizado: String,
    val galponId: Int
) {
    val estado: EstadoLinea = EstadoLinea.desdeTemperatura(temperatura)
}