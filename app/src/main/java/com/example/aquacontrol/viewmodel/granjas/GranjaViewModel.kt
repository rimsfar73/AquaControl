package com.example.aquacontrol.viewmodel.granjas

import androidx.lifecycle.ViewModel

import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GranjaViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _uiState =
        MutableStateFlow<GranjaUiState>(GranjaUiState.Loading)

    val uiState: StateFlow<GranjaUiState> =
        _uiState.asStateFlow()

    init {
        cargarGranjas()
    }

    fun cargarGranjas() {
        _uiState.value = GranjaUiState.Loading

        try {
            val resultado = repository.obtenerGranjas()


            _uiState.value = if (resultado.isEmpty()) {
                GranjaUiState.Empty
            } else {
                GranjaUiState.Success(resultado)
            }
        } catch (e: Exception) {
            _uiState.value = GranjaUiState.Error(
                mensaje = "No se pudieron cargar las granjas."
            )
        }
    }
}