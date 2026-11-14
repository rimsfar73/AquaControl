package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity
import com.example.aquacontrol.repository.data.FlushingConsultaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Representamos los resultados posibles de consultar el detalle.
sealed class DetalleFlushingRemotoUiState {

    object Cargando : DetalleFlushingRemotoUiState()

    data class Disponible(
        val registro: FlushingRemotoEntity,
        val desdeCopiaLocal: Boolean
    ) : DetalleFlushingRemotoUiState()

    object NoEncontrado : DetalleFlushingRemotoUiState()

    data class Error(
        val mensaje: String
    ) : DetalleFlushingRemotoUiState()
}

// Conectamos la pantalla de detalle con el repositorio de consulta.
// No buscamos estos identificadores en los registros locales antiguos.
class DetalleFlushingRemotoViewModel(
    private val repository: FlushingConsultaRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<DetalleFlushingRemotoUiState>(
            DetalleFlushingRemotoUiState.Cargando
        )

    val uiState: StateFlow<DetalleFlushingRemotoUiState> =
        _uiState.asStateFlow()

    private var cargaActual: Job? = null

    fun cargar(lineaId: Long, registroId: Long) {
        // Cancelamos la carga anterior para evitar resultados atrasados.
        cargaActual?.cancel()

        if (lineaId <= 0 || registroId <= 0) {
            _uiState.value = DetalleFlushingRemotoUiState.Error(
                "No se pudo identificar el registro solicitado."
            )
            return
        }

        _uiState.value = DetalleFlushingRemotoUiState.Cargando

        cargaActual = viewModelScope.launch {
            try {
                val resultado = repository.obtenerDetalle(
                    lineaId = lineaId,
                    id = registroId
                )

                val registro = resultado.registro

                _uiState.value = if (registro == null) {
                    // El servidor confirmó que el registro ya no existe.
                    DetalleFlushingRemotoUiState.NoEncontrado
                } else {
                    DetalleFlushingRemotoUiState.Disponible(
                        registro = registro,
                        desdeCopiaLocal = resultado.desdeCopiaLocal
                    )
                }
            } catch (e: CancellationException) {
                // Respetamos la cancelación al abandonar o cambiar el detalle.
                throw e
            } catch (e: HttpException) {
                val mensaje = when (e.code()) {
                    401 -> "La sesión no es válida o venció. Inicia sesión nuevamente."
                    403 -> "Tu usuario no tiene permiso para consultar este registro."
                    else -> "El servidor no pudo entregar el detalle (${e.code()})."
                }

                _uiState.value =
                    DetalleFlushingRemotoUiState.Error(mensaje)
            } catch (e: IOException) {
                // El repositorio ya intentó recuperar la copia local.
                _uiState.value = DetalleFlushingRemotoUiState.Error(
                    e.message ?: "No se pudo recuperar el registro."
                )
            } catch (e: Exception) {
                _uiState.value = DetalleFlushingRemotoUiState.Error(
                    "No se pudo cargar o actualizar la copia del registro. " +
                            "Intenta nuevamente."
                )
            }
        }
    }
}