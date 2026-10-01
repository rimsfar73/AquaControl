package com.example.aquacontrol.viewmodel.galpones

import com.example.aquacontrol.model.galpon.Galpon

sealed class GalponUiState {

    object Loading : GalponUiState()

    data class Success(
        val galpones: List<Galpon>
    ) : GalponUiState()

    object Empty : GalponUiState()

    data class Error(
        val mensaje: String
    ) : GalponUiState()
}