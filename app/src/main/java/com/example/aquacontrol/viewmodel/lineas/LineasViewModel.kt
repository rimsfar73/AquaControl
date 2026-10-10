package com.example.aquacontrol.viewmodel.lineas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.linea.LineaMonitoreada
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LineaViewModel(
    private val temperaturaRepository: TemperaturaRepository,
    private val bebederoRepository: BebederoRepository = BebederoRepository()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<LineaUiState>(LineaUiState.Loading)

    val uiState: StateFlow<LineaUiState> =
        _uiState.asStateFlow()

    private var observacionActual: Job? = null

    fun cargarLineas(galponId: Int) {
        observacionActual?.cancel()

        if (galponId <= 0) {
            _uiState.value = LineaUiState.Error(
                mensaje = "Selecciona un galpón válido."
            )
            return
        }

        _uiState.value = LineaUiState.Loading

        observacionActual = viewModelScope.launch {
            try {
                val existeGalpon = bebederoRepository.obtenerGranjas()
                    .any { granja ->
                        bebederoRepository.obtenerGalpones(granja.id)
                            .any { galpon -> galpon.id == galponId }
                    }

                if (!existeGalpon) {
                    _uiState.value = LineaUiState.Error(
                        mensaje = "El galpón seleccionado no existe."
                    )
                    return@launch
                }

                val lineas = bebederoRepository.obtenerLineas(galponId)

                if (lineas.isEmpty()) {
                    _uiState.value = LineaUiState.Empty
                    return@launch
                }

                temperaturaRepository.observarUltimasMediciones()
                    .collect { mediciones ->
                        val medicionesPorLinea = mediciones.associateBy {
                            it.lineaId
                        }

                        val lineasMonitoreadas = lineas.map { linea ->
                            LineaMonitoreada(
                                id = linea.id,
                                nombre = linea.nombre,
                                galponId = linea.galponId,
                                ultimaMedicion = medicionesPorLinea[linea.id]
                            )
                        }

                        _uiState.value = LineaUiState.Success(
                            lineas = lineasMonitoreadas
                        )
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = LineaUiState.Error(
                    mensaje = "No se pudieron cargar las líneas y sus mediciones. Intenta nuevamente."
                )
            }
        }
    }
}