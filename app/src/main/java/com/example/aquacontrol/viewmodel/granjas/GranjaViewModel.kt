package com.example.aquacontrol.viewmodel.granjas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.remote.AquaControlApiClient
import com.example.aquacontrol.model.granja.Granja
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GranjaViewModel : ViewModel() {

    private val _uiState =
        MutableStateFlow<GranjaUiState>(GranjaUiState.Loading)

    val uiState: StateFlow<GranjaUiState> =
        _uiState.asStateFlow()

    init {
        cargarGranjas()
    }

    fun cargarGranjas() {
        viewModelScope.launch {
            _uiState.value = GranjaUiState.Loading

            try {
                val granjas = AquaControlApiClient.service
                    .listarGranjas()
                    .map { granja ->
                        Granja(
                            id = Math.toIntExact(granja.id),
                            nombre = granja.nombre
                        )
                    }

                _uiState.value = if (granjas.isEmpty()) {
                    GranjaUiState.Empty
                } else {
                    GranjaUiState.Success(granjas)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = GranjaUiState.Error(
                    mensaje = "No se pudieron cargar las granjas desde el servidor."
                )
            }
        }
    }
}