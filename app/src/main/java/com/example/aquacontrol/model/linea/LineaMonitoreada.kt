package com.example.aquacontrol.model.linea

import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.temperatura.MedicionTemperatura

data class LineaMonitoreada(
    val id: Int,
    val nombre: String,
    val galponId: Int,
    val ultimaMedicion: MedicionTemperatura?
) {
    val estado: EstadoLinea?
        get() = ultimaMedicion?.estado
}