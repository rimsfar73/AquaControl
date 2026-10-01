package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlushingViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _uiState =
        MutableStateFlow<FlushingUiState>(FlushingUiState.Loading)

    val uiState: StateFlow<FlushingUiState> =
        _uiState.asStateFlow()

    fun cargarFlushing(lineaId: Int) {
        _uiState.value = FlushingUiState.Loading

        try {
            val resultado = repository.obtenerFlushing(lineaId)

            _uiState.value = if (resultado.isEmpty()) {
                FlushingUiState.Empty
            } else {
                FlushingUiState.Success(resultado)
            }
        } catch (e: Exception) {
            _uiState.value = FlushingUiState.Error(
                mensaje = "No se pudieron cargar los eventos de flushing."
            )
        }
    }
}