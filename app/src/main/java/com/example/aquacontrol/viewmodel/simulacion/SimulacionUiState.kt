package com.example.aquacontrol.viewmodel.simulacion

data class SimulacionUiState(
    val activa: Boolean = false,
    val medicionesGeneradas: Int = 0,
    val ultimaActualizacion: Long? = null,
    val error: String? = null
)