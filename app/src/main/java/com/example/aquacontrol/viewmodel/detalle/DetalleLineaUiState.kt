package com.example.aquacontrol.viewmodel.detalle

import com.example.aquacontrol.model.linea.LineaMonitoreada
import com.example.aquacontrol.model.temperatura.MedicionTemperatura

sealed class DetalleLineaUiState {

    object Loading : DetalleLineaUiState()

    data class Success(
        val nombreGranja: String,
        val nombreGalpon: String,
        val linea: LineaMonitoreada,
        val historial: List<MedicionTemperatura>
    ) : DetalleLineaUiState()

    data class Error(
        val mensaje: String
    ) : DetalleLineaUiState()
}