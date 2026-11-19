package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.remote.AquaControlApiClient
import com.example.aquacontrol.data.remote.AquaControlApiService
import com.example.aquacontrol.model.galpon.Galpon
import com.example.aquacontrol.model.granja.Granja
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LineaSeleccionFlushing(val id: Int, val nombre: String, val galponId: Int)

data class SeleccionFlushingUiState(
    val granjas: List<Granja> = emptyList(),
    val galpones: List<Galpon> = emptyList(),
    val lineas: List<LineaSeleccionFlushing> = emptyList(),
    val granjaId: Int? = null,
    val galponId: Int? = null,
    val lineaId: Int? = null,
    val error: String? = null
) {
    val puedeContinuar: Boolean
        get() = lineaId != null && lineas.any { it.id == lineaId }
}

// La selección usa los mismos IDs que el backend. Se elimina el catálogo de demo.
class SeleccionFlushingViewModel(
    private val api: AquaControlApiService? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(SeleccionFlushingUiState())
    val uiState: StateFlow<SeleccionFlushingUiState> = _uiState.asStateFlow()
    private val servicio: AquaControlApiService
        get() = api ?: AquaControlApiClient.service

    init { cargarGranjas() }

    fun cargarGranjas() {
        _uiState.value = SeleccionFlushingUiState()
        viewModelScope.launch {
            try {
                val granjas = servicio.listarGranjas().map { Granja(Math.toIntExact(it.id), it.nombre) }
                _uiState.value = _uiState.value.copy(granjas = granjas)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "No se pudieron cargar las granjas.")
            }
        }
    }

    fun seleccionarGranja(granjaId: Int) {
        val actual = _uiState.value
        if (actual.granjas.none { it.id == granjaId }) return
        _uiState.value = actual.copy(granjaId = granjaId, galponId = null, lineaId = null,
            galpones = emptyList(), lineas = emptyList(), error = null)
        viewModelScope.launch {
            try {
                val galpones = servicio.listarGalpones(granjaId.toLong()).map {
                    Galpon(Math.toIntExact(it.id), it.nombre, Math.toIntExact(it.granjaId))
                }
                if (_uiState.value.granjaId == granjaId) {
                    _uiState.value = _uiState.value.copy(galpones = galpones)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (_uiState.value.granjaId == granjaId) {
                    _uiState.value = _uiState.value.copy(error = "No se pudieron cargar los galpones.")
                }
            }
        }
    }

    fun seleccionarGalpon(galponId: Int) {
        val actual = _uiState.value
        if (actual.galpones.none { it.id == galponId && it.granjaId == actual.granjaId }) return
        _uiState.value = actual.copy(galponId = galponId, lineaId = null, lineas = emptyList(), error = null)
        viewModelScope.launch {
            try {
                val lineas = servicio.listarLineas(galponId.toLong()).map {
                    LineaSeleccionFlushing(Math.toIntExact(it.id), it.nombre, Math.toIntExact(it.galponId))
                }
                if (_uiState.value.galponId == galponId) {
                    _uiState.value = _uiState.value.copy(lineas = lineas)
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (_uiState.value.galponId == galponId) {
                    _uiState.value = _uiState.value.copy(error = "No se pudieron cargar las líneas.")
                }
            }
        }
    }

    fun seleccionarLinea(lineaId: Int) {
        val actual = _uiState.value
        if (actual.lineas.none { it.id == lineaId && it.galponId == actual.galponId }) return
        _uiState.value = actual.copy(lineaId = lineaId, error = null)
    }
}
