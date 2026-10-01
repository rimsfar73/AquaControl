package com.example.aquacontrol.viewmodel.flushing

import com.example.aquacontrol.model.flushing.EventoFlushing

sealed class FlushingUiState {

    object Loading : FlushingUiState()

    data class Success(
        val eventos: List<EventoFlushing>
    ) : FlushingUiState()

    object Empty : FlushingUiState()

    data class Error(
        val mensaje: String
    ) : FlushingUiState()
}