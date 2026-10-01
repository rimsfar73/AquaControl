package com.example.aquacontrol.viewmodel.lineas

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LineaViewModel(
    private val repo: BebederoRepository = BebederoRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<LineaUiState>(LineaUiState.Loading)

    val uiState: StateFlow<LineaUiState> =
        _uiState.asStateFlow()

    fun cargarLineas(galponId: Int) {
        _uiState.value = LineaUiState.Loading

        try {
            val resultado = repo.obtenerLineas(galponId)

            _uiState.value = if (resultado.isEmpty()) {
                LineaUiState.Empty
            } else {
                LineaUiState.Success(resultado)
            }
        } catch (e: Exception) {
            _uiState.value = LineaUiState.Error(
                mensaje = "No se pudieron cargar las líneas."
            )
        }
    }
}