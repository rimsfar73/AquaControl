package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.repository.flushing.FlushingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlushingViewModel(
    private val repository: FlushingRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<FlushingUiState>(FlushingUiState.Loading)

    val uiState: StateFlow<FlushingUiState> =
        _uiState.asStateFlow()

    private var cargaActual: Job? = null

    fun cargarFlushing(lineaId: Int) {
        cargaActual?.cancel()

        if (lineaId <= 0) {
            _uiState.value = FlushingUiState.Error(
                mensaje = "Selecciona una línea válida para consultar su historial."
            )
            return
        }

        _uiState.value = FlushingUiState.Loading

        cargaActual = viewModelScope.launch {
            try {
                val resultado =
                    repository.obtenerFlushingPorLinea(lineaId)

                _uiState.value = if (resultado.isEmpty()) {
                    FlushingUiState.Empty
                } else {
                    FlushingUiState.Success(resultado)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = FlushingUiState.Error(
                    mensaje = "No se pudieron cargar los eventos de flushing. Intenta nuevamente."
                )
            }
        }
    }
}