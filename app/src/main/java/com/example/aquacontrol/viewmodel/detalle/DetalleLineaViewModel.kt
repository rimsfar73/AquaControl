package com.example.aquacontrol.viewmodel.detalle

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetalleLineaViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _uiState =
        MutableStateFlow<DetalleLineaUiState>(
            DetalleLineaUiState.Loading
        )

    val uiState: StateFlow<DetalleLineaUiState> =
        _uiState.asStateFlow()

    fun cargarHistorial(lineaId: Int) {
        _uiState.value = DetalleLineaUiState.Loading

        try {
            val resultado = repository.obtenerHistorial(lineaId)


            _uiState.value = if (resultado.isEmpty()) {
                DetalleLineaUiState.Empty
            } else {
                DetalleLineaUiState.Success(resultado)
            }
        } catch (e: Exception) {
            _uiState.value = DetalleLineaUiState.Error(
                mensaje = "No se pudo cargar el historial."
            )
        }
    }
}