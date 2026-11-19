package com.example.aquacontrol.viewmodel.simulacion

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.remote.AquaControlApiClient
import com.example.aquacontrol.data.remote.AquaControlApiService
import com.example.aquacontrol.data.simulacion.GeneradorTemperatura
import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Estado del escenario que el usuario ha fijado para una línea. */
data class ControlManualSimulacion(
    val escenario: EscenarioSimulacion,
    val objetivo: Double
)

class SimulacionViewModel(
    private val temperaturaRepository: TemperaturaRepository,
    private val generador: GeneradorTemperatura = GeneradorTemperatura(),
    private val api: AquaControlApiService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulacionUiState())
    val uiState: StateFlow<SimulacionUiState> = _uiState.asStateFlow()

    private val _soloLineaSeleccionada = MutableStateFlow(false)
    val soloLineaSeleccionada: StateFlow<Boolean> =
        _soloLineaSeleccionada.asStateFlow()

    private val _lineasEnSimulacion = MutableStateFlow<Set<Int>>(emptySet())
    val lineasEnSimulacion: StateFlow<Set<Int>> =
        _lineasEnSimulacion.asStateFlow()

    private val _escenariosManuales =
        MutableStateFlow<Map<Int, ControlManualSimulacion>>(emptyMap())
    val escenariosManuales: StateFlow<Map<Int, ControlManualSimulacion>> =
        _escenariosManuales.asStateFlow()

    private var trabajoInicio: Job? = null
    private var trabajoSimulacion: Job? = null

    init {
        actualizarLineas()
    }

    fun actualizarLineas() {
        if (_uiState.value.activa || trabajoInicio?.isActive == true) return
        viewModelScope.launch { cargarLineasRemotas() }
    }

    private suspend fun cargarLineasRemotas(): Boolean {
        return try {
            val servicio = api ?: AquaControlApiClient.service
            val lineas = mutableListOf<LineaSimulacion>()

            for (granja in servicio.listarGranjas()) {
                for (galpon in servicio.listarGalpones(granja.id)) {
                    for (linea in servicio.listarLineas(galpon.id)) {
                        if (linea.id !in 1..Int.MAX_VALUE.toLong()) continue
                        lineas.add(
                            LineaSimulacion(
                                id = linea.id.toInt(),
                                nombreGranja = granja.nombre,
                                nombreGalpon = galpon.nombre,
                                nombreLinea = linea.nombre
                            )
                        )
                    }
                }
            }

            val disponibles = lineas.distinctBy { it.id }
            val seleccionPrevia = _uiState.value.lineaSeleccionadaId
            _uiState.value = _uiState.value.copy(
                lineas = disponibles,
                lineaSeleccionadaId = seleccionPrevia?.takeIf { id ->
                    disponibles.any { it.id == id }
                },
                error = if (disponibles.isEmpty()) {
                    "No hay líneas disponibles para iniciar la simulación."
                } else null
            )
            disponibles.isNotEmpty()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            runCatching { Log.e("AquaSimulacion", "Error cargando catálogo", e) }
            _uiState.value = _uiState.value.copy(
                lineas = emptyList(),
                lineaSeleccionadaId = null,
                error = "No se pudieron cargar las líneas desde el servidor."
            )
            false
        }
    }

    /** Se configura antes de iniciar. false = todas; true = solo la elegida. */
    fun establecerSoloLineaSeleccionada(soloUna: Boolean) {
        if (_uiState.value.activa || trabajoInicio?.isActive == true) return
        _soloLineaSeleccionada.value = soloUna
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun seleccionarLinea(lineaId: Int) {
        if (_uiState.value.lineas.none { it.id == lineaId }) return
        // Evita cambiar silenciosamente la línea en ejecución del modo individual.
        if (_uiState.value.activa && _soloLineaSeleccionada.value) return
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

    fun iniciar() {
        if (_uiState.value.activa || trabajoInicio?.isActive == true ||
            trabajoSimulacion?.isActive == true
        ) return

        trabajoInicio = viewModelScope.launch {
            if (cargarLineasRemotas()) iniciarTrabajo()
        }
    }

    private fun iniciarTrabajo() {
        val disponibles = _uiState.value.lineas.map { it.id }
        val ids = if (_soloLineaSeleccionada.value) {
            listOfNotNull(_uiState.value.lineaSeleccionadaId)
                .filter { it in disponibles }
        } else {
            disponibles
        }

        if (ids.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Selecciona una línea válida antes de iniciar la simulación."
            )
            return
        }

        generador.reiniciar()
        _escenariosManuales.value = emptyMap()
        _lineasEnSimulacion.value = ids.toSet()
        _uiState.value = _uiState.value.copy(
            activa = true,
            medicionesGeneradas = 0,
            ultimaActualizacion = null,
            error = null,
            mensajeEscenario = null
        )

        val nuevoTrabajo = viewModelScope.launch(start = CoroutineStart.LAZY) {
            try {
                while (isActive) {
                    for (lineaId in ids) {
                        if (!isActive) break
                        val temperatura = generador.generarTemperatura(lineaId)
                        val medicion = temperaturaRepository.registrarMedicion(
                            lineaId = lineaId,
                            temperatura = temperatura,
                            origen = OrigenMedicion.SIMULADA
                        )
                        _uiState.value = _uiState.value.copy(
                            medicionesGeneradas = _uiState.value.medicionesGeneradas + 1,
                            ultimaActualizacion = medicion.fechaHora
                        )
                    }
                    delay(INTERVALO_MILISEGUNDOS)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                runCatching { Log.e("AquaSimulacion", "Error generando lecturas", e) }
                _uiState.value = _uiState.value.copy(
                    error = "La simulación se detuvo por un error. " +
                            "Las mediciones ya guardadas se conservan."
                )
            } finally {
                // Una simulación anterior no debe apagar una nueva sesión.
                if (trabajoSimulacion === currentCoroutineContext()[Job]) {
                    trabajoSimulacion = null
                    _lineasEnSimulacion.value = emptySet()
                    _escenariosManuales.value = emptyMap()
                    _uiState.value = _uiState.value.copy(
                        activa = false,
                        mensajeEscenario = null
                    )
                }
            }
        }
        trabajoSimulacion = nuevoTrabajo
        nuevoTrabajo.start()
    }

    /** Fija el escenario solo si la línea pertenece a la sesión activa. */
    fun aplicarEscenario() {
        val actual = _uiState.value
        if (!actual.puedeAplicarEscenario || trabajoSimulacion?.isActive != true) return

        val linea = actual.lineas.firstOrNull {
            it.id == actual.lineaSeleccionadaId
        } ?: return
        if (linea.id !in _lineasEnSimulacion.value) {
            _uiState.value = actual.copy(
                error = "Esa línea no está incluida en la simulación actual."
            )
            return
        }

        val escenario = actual.escenarioSeleccionado
        generador.seleccionarEscenario(linea.id, escenario)
        val objetivo = generador.objetivoDeLinea(linea.id) ?: return
        _escenariosManuales.value = _escenariosManuales.value +
                (linea.id to ControlManualSimulacion(escenario, objetivo))

        _uiState.value = _uiState.value.copy(
            error = null,
            mensajeEscenario = "${nombreEscenario(escenario)} aplicado a " +
                    "${linea.nombreGranja} · ${linea.nombreGalpon} · ${linea.nombreLinea}. " +
                    "Objetivo: $objetivo °C. Se mantiene hasta liberar el control " +
                    "o detener la simulación. El cambio es gradual."
        )
    }

    fun liberarEscenarioSeleccionado() {
        val lineaId = _uiState.value.lineaSeleccionadaId ?: return
        if (!_uiState.value.activa || lineaId !in _escenariosManuales.value) return
        generador.liberarEscenario(lineaId)
        _escenariosManuales.value = _escenariosManuales.value - lineaId
        _uiState.value = _uiState.value.copy(
            mensajeEscenario = "Línea devuelta al modo automático. " +
                    "El cambio de temperatura será gradual."
        )
    }

    fun detener() {
        trabajoInicio?.cancel()
        trabajoInicio = null
        trabajoSimulacion?.cancel()
        trabajoSimulacion = null
        _lineasEnSimulacion.value = emptySet()
        _escenariosManuales.value = emptyMap()
        _uiState.value = _uiState.value.copy(
            activa = false,
            mensajeEscenario = null
        )
    }

    private fun nombreEscenario(escenario: EscenarioSimulacion): String =
        when (escenario) {
            EscenarioSimulacion.ESTABILIDAD -> "Estabilidad"
            EscenarioSimulacion.CALENTAMIENTO -> "Calentamiento"
            EscenarioSimulacion.ENFRIAMIENTO -> "Enfriamiento"
            EscenarioSimulacion.CRITICO_CALOR -> "Crítico por calor"
            EscenarioSimulacion.CRITICO_FRIO -> "Crítico por frío"
            EscenarioSimulacion.RECUPERACION -> "Recuperación"
        }

    companion object {
        private const val INTERVALO_MILISEGUNDOS = 5_000L
    }
}
