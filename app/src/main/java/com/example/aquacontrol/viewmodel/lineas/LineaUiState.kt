package com.example.aquacontrol.viewmodel.lineas

import com.example.aquacontrol.model.linea.LineaBebedero

sealed class LineaUiState {

    object Loading : LineaUiState()

    data class Success(
        val lineas: List<LineaBebedero>
    ) : LineaUiState()

    object Empty : LineaUiState()

    data class Error(
        val mensaje: String
    ) : LineaUiState()
}