package com.example.aquacontrol.model.temperatura

import com.example.aquacontrol.model.estado.EstadoLinea

data class MedicionTemperatura(
    val id: Long,
    val lineaId: Int,
    val temperatura: Double,
    val fechaHora: Long,
    val origen: OrigenMedicion
) {
    val estado: EstadoLinea
        get() = EstadoLinea.desdeTemperatura(temperatura)
}