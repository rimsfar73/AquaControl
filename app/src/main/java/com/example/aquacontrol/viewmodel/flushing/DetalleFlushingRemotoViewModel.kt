package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity
import com.example.aquacontrol.repository.data.FlushingConsultaRepository
import com.example.aquacontrol.repository.data.FotoFlushingRepository
import com.example.aquacontrol.repository.data.ResultadoFotoFlushing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
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

data class EdicionFlushingUiState(
    val observacion: String,
    val duracion: String,
    val temperaturaAntes: String,
    val temperaturaDespues: String,
    val guardando: Boolean = false,
    val error: String? = null
)

sealed class DetalleFlushingRemotoUiState {
    object Cargando : DetalleFlushingRemotoUiState()

    data class Disponible(
        val registro: FlushingRemotoEntity,
        val desdeCopiaLocal: Boolean,
        val foto: FotoDetalleFlushingUiState =
            FotoDetalleFlushingUiState.Cargando,
        val edicion: EdicionFlushingUiState? = null,
        val mensajeEdicion: String? = null,
        val eliminando: Boolean = false,
        val errorEliminacion: String? = null
    ) : DetalleFlushingRemotoUiState()

    object NoEncontrado : DetalleFlushingRemotoUiState()

    data class Eliminado(
        val mensaje: String
    ) : DetalleFlushingRemotoUiState()

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
        val visible = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
        if (visible?.edicion != null || visible?.eliminando == true) return
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

        if (detalle.foto is FotoDetalleFlushingUiState.Cargando || detalle.eliminando) return

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

    // El botón se habilitará solo para el operario. El backend también
    // verifica el permiso antes de aceptar cualquier modificación.
    fun iniciarEdicion() {
        val actual = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
            ?: return
        if (actual.edicion != null || actual.eliminando) return
        if (actual.desdeCopiaLocal) {
            _uiState.value = actual.copy(
                mensajeEdicion = "Actualiza el registro con conexión antes de editarlo."
            )
            return
        }
        val registro = actual.registro
        _uiState.value = actual.copy(
            mensajeEdicion = null,
            edicion = EdicionFlushingUiState(
                observacion = registro.observacion.orEmpty(),
                duracion = registro.duracionSegundos.toString(),
                temperaturaAntes = registro.temperaturaAntes?.toString().orEmpty(),
                temperaturaDespues = registro.temperaturaDespues?.toString().orEmpty()
            )
        )
    }

    fun editarObservacion(valor: String) = cambiarEdicion {
        it.copy(observacion = valor)
    }

    fun editarDuracion(valor: String) = cambiarEdicion {
        it.copy(duracion = valor)
    }

    fun editarTemperaturaAntes(valor: String) = cambiarEdicion {
        it.copy(temperaturaAntes = valor)
    }

    fun editarTemperaturaDespues(valor: String) = cambiarEdicion {
        it.copy(temperaturaDespues = valor)
    }

    private fun cambiarEdicion(cambio: (EdicionFlushingUiState) -> EdicionFlushingUiState) {
        val actual = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
            ?: return
        val edicion = actual.edicion ?: return
        if (edicion.guardando) return
        _uiState.value = actual.copy(edicion = cambio(edicion).copy(error = null))
    }

    fun cancelarEdicion() {
        val actual = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
            ?: return
        if (actual.edicion?.guardando == true) return
        _uiState.value = actual.copy(edicion = null)
    }

    fun guardarEdicion() {
        val actual = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
            ?: return
        val edicion = actual.edicion ?: return
        if (edicion.guardando) return

        val duracion = edicion.duracion.trim().toIntOrNull()
        val antes = temperaturaOpcional(edicion.temperaturaAntes)
        val despues = temperaturaOpcional(edicion.temperaturaDespues)
        val observacion = edicion.observacion.trim()
        val error = when {
            actual.desdeCopiaLocal -> "Necesitas conexión para guardar cambios."
            observacion.length > 500 -> "La observación admite hasta 500 caracteres."
            duracion == null || duracion <= 0 -> "La duración debe ser un entero mayor que cero."
            edicion.temperaturaAntes.isNotBlank() && antes == null ->
                "Revisa la temperatura anterior. Debe ser un número válido."
            edicion.temperaturaDespues.isNotBlank() && despues == null ->
                "Revisa la temperatura posterior. Debe ser un número válido."
            else -> null
        }
        if (error != null) {
            _uiState.value = actual.copy(edicion = edicion.copy(error = error))
            return
        }

        _uiState.value = actual.copy(
            edicion = edicion.copy(guardando = true, error = null),
            mensajeEdicion = null
        )
        viewModelScope.launch {
            try {
                val resultado = repository.actualizar(
                    registro = actual.registro,
                    observacion = observacion,
                    duracionSegundos = requireNotNull(duracion),
                    temperaturaAntes = antes,
                    temperaturaDespues = despues
                )
                val visible = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
                    ?: return@launch
                if (visible.registro.id != actual.registro.id) return@launch
                _uiState.value = visible.copy(
                    registro = resultado.registro,
                    desdeCopiaLocal = false,
                    edicion = null,
                    mensajeEdicion = if (resultado.copiaActualizada) {
                        "Cambios guardados en el servidor."
                    } else {
                        "Cambios guardados en el servidor, pero no se pudo renovar " +
                                "la copia local. Actualiza el detalle con conexión."
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val visible = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
                    ?: return@launch
                if (visible.registro.id != actual.registro.id) return@launch
                val mensaje = when (e) {
                    is HttpException -> when (e.code()) {
                        400 -> "El servidor rechazó los datos. Revisa los campos."
                        404 -> "El registro o su línea ya no están disponibles."
                        else -> describirErrorHttp(e)
                    }
                    is IOException ->
                        "No pudimos confirmar los cambios. Puedes reintentar o " +
                                "cancelar la edición y actualizar para comprobar el resultado."
                    else -> "No pudimos confirmar el resultado. Actualiza el detalle antes de continuar."
                }
                _uiState.value = visible.copy(
                    edicion = edicion.copy(guardando = false, error = mensaje)
                )
            }
        }
    }

    // Conservamos las temperaturas vacías como null para registros antiguos.
    private fun temperaturaOpcional(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

    // La pantalla solicitará confirmación antes de llamar a esta función.
    // El backend comprueba que el usuario tenga el rol de supervisor.
    fun eliminarRegistro() {
        val actual = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
            ?: return
        if (actual.eliminando || actual.edicion != null) return
        if (actual.desdeCopiaLocal) {
            _uiState.value = actual.copy(
                errorEliminacion = "Actualiza con conexión antes de eliminar el registro."
            )
            return
        }

        _uiState.value = actual.copy(eliminando = true, errorEliminacion = null)

        viewModelScope.launch {
            // Esperamos a que termine la cancelación de la descarga para
            // evitar que vuelva a asociar una foto durante el borrado.
            cargaFoto?.cancelAndJoin()

            val resultado = try {
                repository.eliminar(actual.registro)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val visible = _uiState.value as? DetalleFlushingRemotoUiState.Disponible
                    ?: return@launch
                val mensaje = when (e) {
                    is HttpException -> describirErrorHttp(e)
                    is IOException ->
                        "No pudimos confirmar la eliminación. Actualiza el detalle " +
                                "para comprobar si el registro sigue disponible."
                    else -> "No pudimos confirmar la eliminación. Actualiza antes de continuar."
                }
                _uiState.value = visible.copy(
                    eliminando = false,
                    errorEliminacion = mensaje,
                    foto = if (visible.foto is FotoDetalleFlushingUiState.Cargando) {
                        FotoDetalleFlushingUiState.Error(
                            "La consulta de la foto se interrumpió. Puedes reintentarla."
                        )
                    } else visible.foto
                )
                return@launch
            }

            // Desde aquí el registro ya no existe en el backend.
            // Un fallo de limpieza no debe inducir a repetir el borrado.
            val fotoMostrada =
                (actual.foto as? FotoDetalleFlushingUiState.Disponible)?.nombreArchivo
            val fotoLimpia = if (resultado.copiaEliminada) {
                try {
                    fotosRepository.limpiarTrasEliminacion(
                        registro = actual.registro,
                        fotoMostrada = fotoMostrada
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    false
                }
            } else false

            _uiState.value = DetalleFlushingRemotoUiState.Eliminado(
                mensaje = when {
                    !resultado.copiaEliminada ->
                        "Registro eliminado del servidor. No se pudo retirar su copia local; " +
                                "actualiza el historial con conexión."
                    !fotoLimpia ->
                        "Registro eliminado del servidor y de la lista local. " +
                                "No se pudo completar la limpieza de la foto descargada."
                    else -> "Flushing eliminado correctamente."
                }
            )
        }
    }

    private fun describirErrorHttp(error: HttpException): String {
        return when (error.code()) {
            401 ->
                "La sesión no es válida o venció. Inicia sesión nuevamente."

            403 ->
                "Tu usuario no tiene permiso para realizar esta operación."

            else ->
                "El servidor no pudo completar la operación (${error.code()})."
        }
    }
}