package com.example.aquacontrol.viewmodel.simulacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.simulacion.GeneradorTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SimulacionViewModel(
    private val temperaturaRepository: TemperaturaRepository,
    private val bebederoRepository: BebederoRepository = BebederoRepository(),
    private val generador: GeneradorTemperatura = GeneradorTemperatura()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulacionUiState())

    val uiState: StateFlow<SimulacionUiState> =
        _uiState.asStateFlow()

    private var trabajoSimulacion: Job? = null

    fun iniciar() {
        // Evita iniciar dos simulaciones o reiniciar mientras
        // la ejecución anterior todavía se está deteniendo.
        if (trabajoSimulacion?.isCompleted == false) return

        val lineasIds = try {
            bebederoRepository.obtenerGranjas()
                .flatMap { granja ->
                    bebederoRepository.obtenerGalpones(granja.id)
                }
                .flatMap { galpon ->
                    bebederoRepository.obtenerLineas(galpon.id)
                }
                .map { linea -> linea.id }
                .distinct()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                activa = false,
                error = "No se pudieron cargar las líneas para simular."
            )
            return
        }

        if (lineasIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                activa = false,
                error = "No hay líneas disponibles para iniciar la simulación."
            )
            return
        }

        generador.reiniciar()

        _uiState.value = SimulacionUiState(
            activa = true
        )

        val nuevoTrabajo = viewModelScope.launch(
            start = CoroutineStart.LAZY
        ) {
            try {
                while (isActive) {
                    for (lineaId in lineasIds) {
                        if (!isActive) break

                        val temperatura =
                            generador.generarTemperatura(lineaId)

                        val medicion =
                            temperaturaRepository.registrarMedicion(
                                lineaId = lineaId,
                                temperatura = temperatura,
                                origen = OrigenMedicion.SIMULADA
                            )

                        _uiState.value = _uiState.value.copy(
                            medicionesGeneradas =
                                _uiState.value.medicionesGeneradas + 1,
                            ultimaActualizacion = medicion.fechaHora
                        )
                    }

                    delay(INTERVALO_MILISEGUNDOS)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "La simulación se detuvo por un error. " +
                            "Las mediciones ya guardadas se conservan."
                )
            } finally {
                _uiState.value = _uiState.value.copy(
                    activa = false
                )
            }
        }

        trabajoSimulacion = nuevoTrabajo
        nuevoTrabajo.start()
    }

    fun detener() {
        trabajoSimulacion?.cancel()

        _uiState.value = _uiState.value.copy(
            activa = false
        )
    }

    companion object {
        private const val INTERVALO_MILISEGUNDOS = 5_000L
    }
}