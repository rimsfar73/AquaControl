package com.example.aquacontrol.viewmodel.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class AlertaLinea(
    val nombreGranja: String,
    val nombreGalpon: String,
    val nombreLinea: String,
    val medicion: MedicionTemperatura
)

sealed interface AlertasUiState {

    object Loading : AlertasUiState

    data class Success(
        val alertas: List<AlertaLinea>,
        val totalLineas: Int,
        val lineasSinMediciones: Int
    ) : AlertasUiState

    data class Error(
        val mensaje: String
    ) : AlertasUiState
}

class AlertasViewModel(
    private val temperaturaRepository: TemperaturaRepository,
    private val bebederoRepository: BebederoRepository = BebederoRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<AlertasUiState>(AlertasUiState.Loading)

    val uiState: StateFlow<AlertasUiState> =
        _uiState.asStateFlow()

    private var observacionActual: Job? = null

    init {
        cargarAlertas()
    }

    fun cargarAlertas() {
        observacionActual?.cancel()
        _uiState.value = AlertasUiState.Loading

        observacionActual = viewModelScope.launch {
            try {
                val ubicaciones = cargarUbicaciones()

                temperaturaRepository.observarUltimasMediciones()
                    .collect { mediciones ->
                        val medicionesPorLinea = mediciones.associateBy {
                            it.lineaId
                        }

                        val alertas = mutableListOf<AlertaLinea>()
                        var sinMediciones = 0

                        for (ubicacion in ubicaciones) {
                            val medicion =
                                medicionesPorLinea[ubicacion.lineaId]

                            if (medicion == null) {
                                sinMediciones++
                                continue
                            }

                            val requiereAtencion = when (medicion.estado) {
                                EstadoLinea.NORMAL -> false
                                EstadoLinea.ADVERTENCIA -> true
                                EstadoLinea.CRITICO -> true
                            }

                            if (requiereAtencion) {
                                alertas.add(
                                    AlertaLinea(
                                        nombreGranja = ubicacion.nombreGranja,
                                        nombreGalpon = ubicacion.nombreGalpon,
                                        nombreLinea = ubicacion.nombreLinea,
                                        medicion = medicion
                                    )
                                )
                            }
                        }

                        val alertasOrdenadas = alertas.sortedWith(
                            compareBy<AlertaLinea> {
                                when (it.medicion.estado) {
                                    EstadoLinea.CRITICO -> 0
                                    EstadoLinea.ADVERTENCIA -> 1
                                    EstadoLinea.NORMAL -> 2
                                }
                            }
                                .thenBy { it.nombreGranja }
                                .thenBy { it.nombreGalpon }
                                .thenBy { it.nombreLinea }
                        )

                        _uiState.value = AlertasUiState.Success(
                            alertas = alertasOrdenadas,
                            totalLineas = ubicaciones.size,
                            lineasSinMediciones = sinMediciones
                        )
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = AlertasUiState.Error(
                    mensaje = "No se pudieron cargar las alertas. Intenta nuevamente."
                )
            }
        }
    }

    private fun cargarUbicaciones(): List<UbicacionLinea> {
        val ubicaciones = mutableListOf<UbicacionLinea>()

        for (granja in bebederoRepository.obtenerGranjas()) {
            for (galpon in bebederoRepository.obtenerGalpones(granja.id)) {
                for (linea in bebederoRepository.obtenerLineas(galpon.id)) {
                    ubicaciones.add(
                        UbicacionLinea(
                            lineaId = linea.id,
                            nombreGranja = granja.nombre,
                            nombreGalpon = galpon.nombre,
                            nombreLinea = linea.nombre
                        )
                    )
                }
            }
        }

        return ubicaciones
    }

    private data class UbicacionLinea(
        val lineaId: Int,
        val nombreGranja: String,
        val nombreGalpon: String,
        val nombreLinea: String
    )
}