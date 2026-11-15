package com.example.aquacontrol.viewmodel.lineas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.remote.AquaControlApiClient
import com.example.aquacontrol.model.linea.LineaMonitoreada
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LineaViewModel(
    private val temperaturaRepository: TemperaturaRepository
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
                val lineas = AquaControlApiClient.service
                    .listarLineas(galponId.toLong())
                    .map { remoto ->
                        LineaMonitoreada(
                            id = Math.toIntExact(remoto.id),
                            nombre = remoto.nombre,
                            galponId = Math.toIntExact(remoto.galponId),
                            ultimaMedicion = null
                        )
                    }

                if (lineas.isEmpty()) {
                    _uiState.value = LineaUiState.Empty
                    return@launch
                }

                temperaturaRepository.observarUltimasMediciones()
                    .collect { mediciones ->
                        val medicionesPorLinea =
                            mediciones.associateBy { it.lineaId }

                        val monitoreadas = lineas.map { linea ->
                            linea.copy(
                                ultimaMedicion =
                                    medicionesPorLinea[linea.id]
                            )
                        }

                        _uiState.value = LineaUiState.Success(
                            lineas = monitoreadas
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