package com.example.aquacontrol.viewmodel.alertas

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.linea.LineaBebedero
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AlertaLinea(
    val nombreGranja: String,
    val nombreGalpon: String,
    val linea: LineaBebedero
)

sealed interface AlertasUiState {
    object Loading : AlertasUiState

    data class Success(
        val alertas: List<AlertaLinea>
    ) : AlertasUiState

    object Empty : AlertasUiState

    data class Error(
        val mensaje: String
    ) : AlertasUiState
}

class AlertasViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _uiState =
        MutableStateFlow<AlertasUiState>(AlertasUiState.Loading)

    val uiState: StateFlow<AlertasUiState> =
        _uiState.asStateFlow()

    init {
        cargarAlertas()
    }

    fun cargarAlertas() {
        _uiState.value = AlertasUiState.Loading

        try {
            val alertas = mutableListOf<AlertaLinea>()

            for (granja in repository.obtenerGranjas()) {
                for (galpon in repository.obtenerGalpones(granja.id)) {
                    for (linea in repository.obtenerLineas(galpon.id)) {
                        val requiereAtencion = when (linea.estado) {
                            EstadoLinea.NORMAL -> false
                            EstadoLinea.ADVERTENCIA -> true
                            EstadoLinea.CRITICO -> true
                        }

                        if (requiereAtencion) {
                            alertas.add(
                                AlertaLinea(
                                    nombreGranja = granja.nombre,
                                    nombreGalpon = galpon.nombre,
                                    linea = linea
                                )
                            )
                        }
                    }
                }
            }

            val alertasOrdenadas = alertas.sortedWith(
                compareBy<AlertaLinea> {
                    when (it.linea.estado) {
                        EstadoLinea.CRITICO -> 0
                        EstadoLinea.ADVERTENCIA -> 1
                        EstadoLinea.NORMAL -> 2
                    }
                }
                    .thenBy { it.nombreGranja }
                    .thenBy { it.nombreGalpon }
                    .thenBy { it.linea.nombre }
            )

            _uiState.value = if (alertasOrdenadas.isEmpty()) {
                AlertasUiState.Empty
            } else {
                AlertasUiState.Success(alertasOrdenadas)
            }
        } catch (e: Exception) {
            _uiState.value = AlertasUiState.Error(
                mensaje = "No se pudieron cargar las alertas. Intenta nuevamente."
            )
        }
    }
}