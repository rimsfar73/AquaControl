package com.example.aquacontrol.viewmodel.simulacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.simulacion.GeneradorTemperatura
import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
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

    init {
        cargarLineas()
    }

    private fun cargarLineas(): Boolean {
        return try {
            val lineas = mutableListOf<LineaSimulacion>()

            for (granja in bebederoRepository.obtenerGranjas()) {
                for (galpon in bebederoRepository.obtenerGalpones(granja.id)) {
                    for (linea in bebederoRepository.obtenerLineas(galpon.id)) {
                        lineas.add(
                            LineaSimulacion(
                                id = linea.id,
                                nombreGranja = granja.nombre,
                                nombreGalpon = galpon.nombre,
                                nombreLinea = linea.nombre
                            )
                        )
                    }
                }
            }

            val lineasDisponibles = lineas.distinctBy { it.id }
            val seleccionAnterior = _uiState.value.lineaSeleccionadaId

            val seleccionValida = seleccionAnterior?.takeIf { id ->
                lineasDisponibles.any { it.id == id }
            }

            _uiState.value = _uiState.value.copy(
                lineas = lineasDisponibles,
                lineaSeleccionadaId = seleccionValida,
                error = if (lineasDisponibles.isEmpty()) {
                    "No hay líneas disponibles para iniciar la simulación."
                } else {
                    null
                }
            )

            lineasDisponibles.isNotEmpty()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                lineas = emptyList(),
                lineaSeleccionadaId = null,
                error = "No se pudieron cargar las líneas para simular."
            )

            false
        }
    }

    fun seleccionarLinea(lineaId: Int) {
        if (_uiState.value.lineas.none { it.id == lineaId }) return

        _uiState.value = _uiState.value.copy(
            lineaSeleccionadaId = lineaId,
            mensajeEscenario = null
        )
    }

    fun seleccionarEscenario(escenario: EscenarioSimulacion) {
        _uiState.value = _uiState.value.copy(
            escenarioSeleccionado = escenario,
            mensajeEscenario = null
        )
    }

    fun aplicarEscenario() {
        val actual = _uiState.value

        if (!actual.puedeAplicarEscenario) return
        if (trabajoSimulacion?.isActive != true) return

        val linea = actual.lineas.firstOrNull {
            it.id == actual.lineaSeleccionadaId
        } ?: return

        generador.seleccionarEscenario(
            lineaId = linea.id,
            escenario = actual.escenarioSeleccionado
        )

        val nombreEscenario = when (actual.escenarioSeleccionado) {
            EscenarioSimulacion.ESTABILIDAD -> "Estabilidad"
            EscenarioSimulacion.CALENTAMIENTO -> "Calentamiento"
            EscenarioSimulacion.ENFRIAMIENTO -> "Enfriamiento"
            EscenarioSimulacion.CRITICO_CALOR -> "Crítico por calor"
            EscenarioSimulacion.CRITICO_FRIO -> "Crítico por frío"
            EscenarioSimulacion.RECUPERACION -> "Recuperación"
        }

        _uiState.value = _uiState.value.copy(
            mensajeEscenario =
                "Escenario solicitado: $nombreEscenario. " +
                        "${linea.nombreGranja} · ${linea.nombreGalpon} · " +
                        "${linea.nombreLinea}. La temperatura cambiará " +
                        "gradualmente en las próximas lecturas."
        )
    }

    fun iniciar() {
        // Espera a que termine cualquier ejecución anterior.
        if (trabajoSimulacion?.isCompleted == false) return

        if (!cargarLineas()) return

        val lineasIds = _uiState.value.lineas.map { it.id }

        generador.reiniciar()

        _uiState.value = _uiState.value.copy(
            activa = true,
            medicionesGeneradas = 0,
            ultimaActualizacion = null,
            error = null,
            mensajeEscenario = null
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
                    activa = false,
                    mensajeEscenario = null
                )
            }
        }

        trabajoSimulacion = nuevoTrabajo
        nuevoTrabajo.start()
    }

    fun detener() {
        trabajoSimulacion?.cancel()

        _uiState.value = _uiState.value.copy(
            activa = false,
            mensajeEscenario = null
        )
    }

    companion object {
        private const val INTERVALO_MILISEGUNDOS = 5_000L
    }
}