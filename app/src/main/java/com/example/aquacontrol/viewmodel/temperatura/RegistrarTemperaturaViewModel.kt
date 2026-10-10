package com.example.aquacontrol.viewmodel.temperatura

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistrarTemperaturaViewModel(
    private val repository: TemperaturaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RegistrarTemperaturaUiState()
    )

    val uiState: StateFlow<RegistrarTemperaturaUiState> =
        _uiState.asStateFlow()

    fun seleccionarLinea(lineaId: Int) {
        val actual = _uiState.value

        if (actual.guardando) return
        if (actual.lineaId == lineaId) return

        _uiState.value = RegistrarTemperaturaUiState(
            lineaId = lineaId,
            errorLinea = if (lineaId <= 0) {
                "Selecciona una línea válida."
            } else {
                null
            }
        )
    }

    fun actualizarTemperatura(valor: String) {
        val actual = _uiState.value

        if (actual.guardando || actual.medicionGuardada != null) return

        _uiState.value = actual.copy(
            temperatura = valor,
            errorTemperatura = null,
            errorGuardado = null
        )
    }

    fun registrarTemperatura() {
        val actual = _uiState.value

        if (actual.guardando || actual.medicionGuardada != null) return

        val lineaId = actual.lineaId
        val temperatura = actual.temperatura
            .trim()
            .replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it.isFinite() }

        val errorLinea = if (lineaId == null || lineaId <= 0) {
            "Selecciona una línea válida."
        } else {
            null
        }

        val errorTemperatura = when {
            actual.temperatura.isBlank() ->
                "Ingresa la temperatura."

            temperatura == null ->
                "Ingresa una temperatura numérica válida."

            else -> null
        }

        val estadoValidado = actual.copy(
            errorLinea = errorLinea,
            errorTemperatura = errorTemperatura,
            errorGuardado = null
        )

        _uiState.value = estadoValidado

        if (errorLinea != null || errorTemperatura != null) return
        if (lineaId == null || temperatura == null) return

        _uiState.value = estadoValidado.copy(
            guardando = true
        )

        viewModelScope.launch {
            try {
                val medicion = repository.registrarMedicion(
                    lineaId = lineaId,
                    temperatura = temperatura,
                    origen = OrigenMedicion.MANUAL
                )

                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGuardado = null,
                    medicionGuardada = medicion
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGuardado = e.message
                        ?: "Revisa la línea y la temperatura ingresada."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    errorGuardado =
                        "No se pudo guardar la temperatura. Inténtalo nuevamente."
                )
            } finally {
                if (_uiState.value.guardando) {
                    _uiState.value = _uiState.value.copy(
                        guardando = false
                    )
                }
            }
        }
    }
}