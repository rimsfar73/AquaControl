package com.example.aquacontrol.viewmodel.granjas

import com.example.aquacontrol.model.granja.Granja

sealed class GranjaUiState {

    object Loading : GranjaUiState()

    data class Success(
        val granjas: List<Granja>
    ) : GranjaUiState()

    object Empty : GranjaUiState()

    data class Error(
        val mensaje: String
    ) : GranjaUiState()
}