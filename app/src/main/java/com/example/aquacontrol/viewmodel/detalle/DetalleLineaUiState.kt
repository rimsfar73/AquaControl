package com.example.aquacontrol.viewmodel.detalle

import com.example.aquacontrol.model.historial.HistorialTemperatura
import com.example.aquacontrol.model.linea.LineaBebedero

sealed class DetalleLineaUiState {

    object Loading : DetalleLineaUiState()

    data class Success(
        val nombreGranja: String,
        val nombreGalpon: String,
        val linea: LineaBebedero,
        val historial: List<HistorialTemperatura>
    ) : DetalleLineaUiState()

    data class Error(
        val mensaje: String
    ) : DetalleLineaUiState()
}