package com.example.aquacontrol.viewmodel.galpones

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalponViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _uiState =
        MutableStateFlow<GalponUiState>(GalponUiState.Loading)

    val uiState: StateFlow<GalponUiState> =
        _uiState.asStateFlow()


    fun cargarGalpones(granjaId: Int) {
        _uiState.value = GalponUiState.Loading

        try {
            val resultado = repository.obtenerGalpones(granjaId)

            _uiState.value = if (resultado.isEmpty()) {
                GalponUiState.Empty
            } else {
                GalponUiState.Success(resultado)
            }
        } catch (e: Exception) {
            _uiState.value = GalponUiState.Error(
                mensaje = "No se pudieron cargar los galpones."
            )
        }
    }
}