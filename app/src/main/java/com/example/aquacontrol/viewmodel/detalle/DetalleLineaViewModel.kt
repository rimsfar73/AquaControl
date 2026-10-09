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

        if (lineaId <= 0) {
            _uiState.value = DetalleLineaUiState.Error(
                mensaje = "No se pudo identificar la línea seleccionada."
            )
            return
        }

        try {
            for (granja in repository.obtenerGranjas()) {
                for (galpon in repository.obtenerGalpones(granja.id)) {
                    val linea = repository.obtenerLineas(galpon.id)
                        .firstOrNull { it.id == lineaId }

                    if (linea != null) {
                        val historial = repository
                            .obtenerHistorial(linea.id)
                            .sortedByDescending { it.fechaHora }

                        _uiState.value = DetalleLineaUiState.Success(
                            nombreGranja = granja.nombre,
                            nombreGalpon = galpon.nombre,
                            linea = linea,
                            historial = historial
                        )

                        return
                    }
                }
            }

            _uiState.value = DetalleLineaUiState.Error(
                mensaje = "La línea seleccionada no fue encontrada."
            )
        } catch (e: Exception) {
            _uiState.value = DetalleLineaUiState.Error(
                mensaje = "No se pudieron cargar los datos de la línea. Intenta nuevamente."
            )
        }
    }
}