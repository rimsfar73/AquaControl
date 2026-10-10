package com.example.aquacontrol.viewmodel.lineas

import com.example.aquacontrol.model.linea.LineaMonitoreada

sealed class LineaUiState {

    object Loading : LineaUiState()

    data class Success(
        val lineas: List<LineaMonitoreada>
    ) : LineaUiState()

    object Empty : LineaUiState()

    data class Error(
        val mensaje: String
    ) : LineaUiState()
}