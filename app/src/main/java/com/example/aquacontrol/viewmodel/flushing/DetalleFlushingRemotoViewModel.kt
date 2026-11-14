package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity
import com.example.aquacontrol.repository.data.FlushingConsultaRepository
import com.example.aquacontrol.repository.data.FotoFlushingRepository
import com.example.aquacontrol.repository.data.ResultadoFotoFlushing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class FotoDetalleFlushingUiState {
    object Cargando : FotoDetalleFlushingUiState()

    data class Disponible(
        val nombreArchivo: String,
        val desdeCopiaLocal: Boolean
    ) : FotoDetalleFlushingUiState()

    object SinFoto : FotoDetalleFlushingUiState()
    object SinCopiaLocal : FotoDetalleFlushingUiState()

    data class Error(
        val mensaje: String
    ) : FotoDetalleFlushingUiState()
}

sealed class DetalleFlushingRemotoUiState {
    object Cargando : DetalleFlushingRemotoUiState()

    data class Disponible(
        val registro: FlushingRemotoEntity,
        val desdeCopiaLocal: Boolean,
        val foto: FotoDetalleFlushingUiState =
            FotoDetalleFlushingUiState.Cargando
    ) : DetalleFlushingRemotoUiState()

    object NoEncontrado : DetalleFlushingRemotoUiState()

    data class Error(
        val mensaje: String
    ) : DetalleFlushingRemotoUiState()
}

class DetalleFlushingRemotoViewModel(
    private val repository: FlushingConsultaRepository,
    private val fotosRepository: FotoFlushingRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<DetalleFlushingRemotoUiState>(
            DetalleFlushingRemotoUiState.Cargando
        )

    val uiState: StateFlow<DetalleFlushingRemotoUiState> =
        _uiState.asStateFlow()

    private var cargaActual: Job? = null
    private var cargaFoto: Job? = null

    fun cargar(lineaId: Long, registroId: Long) {
        // Cancelamos las consultas anteriores al cambiar o actualizar.
        cargaActual?.cancel()
        cargaFoto?.cancel()

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

                if (registro == null) {
                    _uiState.value =
                        DetalleFlushingRemotoUiState.NoEncontrado
                    return@launch
                }

                // Mostramos primero los datos, sin esperar a la imagen.
                val detalle = DetalleFlushingRemotoUiState.Disponible(
                    registro = registro,
                    desdeCopiaLocal = resultado.desdeCopiaLocal
                )

                _uiState.value = detalle

                cargarFoto(
                    detalle = detalle,
                    soloCopiaLocal = resultado.desdeCopiaLocal
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                _uiState.value = DetalleFlushingRemotoUiState.Error(
                    describirErrorHttp(e)
                )
            } catch (e: IOException) {
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

    fun reintentarFoto() {
        val detalle =
            _uiState.value as? DetalleFlushingRemotoUiState.Disponible
                ?: return

        if (detalle.foto is FotoDetalleFlushingUiState.Cargando) return

        // Al reintentar comprobamos nuevamente la conexión con el servidor.
        cargarFoto(detalle, soloCopiaLocal = false)
    }

    private fun cargarFoto(
        detalle: DetalleFlushingRemotoUiState.Disponible,
        soloCopiaLocal: Boolean
    ) {
        cargaFoto?.cancel()

        _uiState.value = detalle.copy(
            foto = FotoDetalleFlushingUiState.Cargando
        )

        cargaFoto = viewModelScope.launch {
            val estadoFoto = try {
                when (
                    val resultado = fotosRepository.consultar(
                        registro = detalle.registro,
                        soloCopiaLocal = soloCopiaLocal
                    )
                ) {
                    is ResultadoFotoFlushing.Disponible ->
                        FotoDetalleFlushingUiState.Disponible(
                            nombreArchivo = resultado.nombreArchivo,
                            desdeCopiaLocal = resultado.desdeCopiaLocal
                        )

                    ResultadoFotoFlushing.SinFoto ->
                        FotoDetalleFlushingUiState.SinFoto

                    ResultadoFotoFlushing.SinCopiaLocal ->
                        FotoDetalleFlushingUiState.SinCopiaLocal
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                FotoDetalleFlushingUiState.Error(
                    describirErrorHttp(e)
                )
            } catch (e: IOException) {
                FotoDetalleFlushingUiState.Error(
                    e.message ?: "No se pudo descargar o conservar la foto."
                )
            } catch (e: IllegalArgumentException) {
                FotoDetalleFlushingUiState.Error(
                    e.message ?: "La imagen recibida no es válida."
                )
            } catch (e: Exception) {
                FotoDetalleFlushingUiState.Error(
                    "No se pudo cargar la foto. Intenta nuevamente."
                )
            }

            // Actualizamos únicamente la foto del detalle que sigue abierto.
            val actual =
                _uiState.value as? DetalleFlushingRemotoUiState.Disponible

            if (
                actual != null &&
                actual.registro.id == detalle.registro.id &&
                actual.registro.servidor == detalle.registro.servidor
            ) {
                _uiState.value = actual.copy(foto = estadoFoto)
            }
        }
    }

    private fun describirErrorHttp(error: HttpException): String {
        return when (error.code()) {
            401 ->
                "La sesión no es válida o venció. Inicia sesión nuevamente."

            403 ->
                "Tu usuario no tiene permiso para consultar esta información."

            else ->
                "El servidor no pudo completar la consulta (${error.code()})."
        }
    }
}