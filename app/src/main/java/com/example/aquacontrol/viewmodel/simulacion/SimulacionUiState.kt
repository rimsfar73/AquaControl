package com.example.aquacontrol.viewmodel.simulacion

import com.example.aquacontrol.model.simulacion.EscenarioSimulacion

data class LineaSimulacion(
    val id: Int,
    val nombreGranja: String,
    val nombreGalpon: String,
    val nombreLinea: String
)

data class SimulacionUiState(
    val activa: Boolean = false,
    val medicionesGeneradas: Int = 0,
    val ultimaActualizacion: Long? = null,
    val error: String? = null,
    val lineas: List<LineaSimulacion> = emptyList(),
    val lineaSeleccionadaId: Int? = null,
    val escenarioSeleccionado: EscenarioSimulacion =
        EscenarioSimulacion.CRITICO_CALOR,
    val mensajeEscenario: String? = null
) {
    val puedeAplicarEscenario: Boolean
        get() = activa && lineas.any {
            it.id == lineaSeleccionadaId
        }
}