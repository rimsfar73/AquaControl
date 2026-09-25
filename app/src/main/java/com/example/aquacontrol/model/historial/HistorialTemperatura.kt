package com.example.aquacontrol.model.historial

data class HistorialTemperatura(
    val id: Int,
    val lineaId: Int,
    val temperatura: Double,
    val fechaHora: String
)
