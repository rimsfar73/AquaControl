package com.example.aquacontrol.viewmodel.detalle

import com.example.aquacontrol.model.historial.HistorialTemperatura

sealed class DetalleLineaUiState {

    object Loading : DetalleLineaUiState()

    data class Success(
        val historial: List<HistorialTemperatura>
    ) : DetalleLineaUiState()

    object Empty : DetalleLineaUiState()

    data class Error(
        val mensaje: String
    ) : DetalleLineaUiState()
}