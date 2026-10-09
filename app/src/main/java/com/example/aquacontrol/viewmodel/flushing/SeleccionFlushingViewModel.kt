package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.model.galpon.Galpon
import com.example.aquacontrol.model.granja.Granja
import com.example.aquacontrol.model.linea.LineaBebedero
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SeleccionFlushingUiState(
    val granjas: List<Granja> = emptyList(),
    val galpones: List<Galpon> = emptyList(),
    val lineas: List<LineaBebedero> = emptyList(),
    val granjaId: Int? = null,
    val galponId: Int? = null,
    val lineaId: Int? = null,
    val error: String? = null
) {
    val puedeContinuar: Boolean
        get() = lineaId != null && lineas.any { it.id == lineaId }
}

class SeleccionFlushingViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _uiState = MutableStateFlow(SeleccionFlushingUiState())

    val uiState: StateFlow<SeleccionFlushingUiState> =
        _uiState.asStateFlow()

    init {
        cargarGranjas()
    }

    fun cargarGranjas() {
        _uiState.value = SeleccionFlushingUiState()

        try {
            _uiState.value = SeleccionFlushingUiState(
                granjas = repository.obtenerGranjas()
            )
        } catch (e: Exception) {
            _uiState.value = SeleccionFlushingUiState(
                error = "No se pudieron cargar las granjas."
            )
        }
    }

    fun seleccionarGranja(granjaId: Int) {
        val actual = _uiState.value

        if (actual.granjas.none { it.id == granjaId }) return

        val nuevaSeleccion = actual.copy(
            granjaId = granjaId,
            galponId = null,
            lineaId = null,
            galpones = emptyList(),
            lineas = emptyList(),
            error = null
        )

        _uiState.value = nuevaSeleccion

        try {
            _uiState.value = nuevaSeleccion.copy(
                galpones = repository.obtenerGalpones(granjaId)
            )
        } catch (e: Exception) {
            _uiState.value = nuevaSeleccion.copy(
                error = "No se pudieron cargar los galpones. " +
                        "Selecciona nuevamente la granja para reintentar."
            )
        }
    }

    fun seleccionarGalpon(galponId: Int) {
        val actual = _uiState.value

        val perteneceALaGranja = actual.galpones.any {
            it.id == galponId && it.granjaId == actual.granjaId
        }

        if (!perteneceALaGranja) return

        val nuevaSeleccion = actual.copy(
            galponId = galponId,
            lineaId = null,
            lineas = emptyList(),
            error = null
        )

        _uiState.value = nuevaSeleccion

        try {
            _uiState.value = nuevaSeleccion.copy(
                lineas = repository.obtenerLineas(galponId)
            )
        } catch (e: Exception) {
            _uiState.value = nuevaSeleccion.copy(
                error = "No se pudieron cargar las líneas. " +
                        "Selecciona nuevamente el galpón para reintentar."
            )
        }
    }

    fun seleccionarLinea(lineaId: Int) {
        val actual = _uiState.value

        val perteneceAlGalpon = actual.lineas.any {
            it.id == lineaId && it.galponId == actual.galponId
        }

        if (!perteneceAlGalpon) return

        _uiState.value = actual.copy(
            lineaId = lineaId,
            error = null
        )
    }
}