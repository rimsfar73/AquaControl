package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto
import com.example.aquacontrol.repository.data.FlushingRemoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrarFlushingViewModel(
    private val repository: FlushingRemoteRepository,
    private val prepararFoto: suspend (String) -> MultipartBody.Part
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrarFlushingUiState())
    val uiState: StateFlow<RegistrarFlushingUiState> = _uiState.asStateFlow()

    // Una respuesta perdida puede dejar incierto el resultado del POST.
    // Bloqueamos otro intento automático para evitar duplicar el registro.
    private var creacionIncierta = false

    fun seleccionarLinea(lineaId: Int) {
        editar { it.copy(lineaId = lineaId, errorLinea = null) }
    }

    fun actualizarObservacion(valor: String) {
        editar { it.copy(observacion = valor, errorObservacion = null) }
    }

    fun actualizarTemperaturaAntes(valor: String) {
        editar {
            it.copy(temperaturaAntes = valor, errorTemperaturaAntes = null)
        }
    }

    fun actualizarTemperaturaDespues(valor: String) {
        editar {
            it.copy(temperaturaDespues = valor, errorTemperaturaDespues = null)
        }
    }

    fun actualizarDuracion(valor: String) {
        editar { it.copy(duracionSegundos = valor, errorDuracion = null) }
    }

    fun actualizarFoto(ruta: String?) {
        val actual = _uiState.value
        if (actual.guardando || actual.guardadoRemotoCompleto || creacionIncierta) {
            return
        }

        // Si el registro ya existe, permitimos reemplazar la foto fallida,
        // pero no quitarla y presentar su subida como completada.
        val nombre = ruta?.takeIf { it.isNotBlank() }
        if (actual.registroConfirmado != null && nombre == null) return

        _uiState.value = actual.copy(
            fotoRuta = nombre,
            errorFoto = null,
            errorGuardado = null
        )
    }

    fun informarErrorFoto(mensaje: String?) {
        val actual = _uiState.value
        if (actual.guardando || actual.guardadoRemotoCompleto) return
        _uiState.value = actual.copy(errorFoto = mensaje)
    }

    private fun editar(
        cambio: (RegistrarFlushingUiState) -> RegistrarFlushingUiState
    ) {
        val actual = _uiState.value
        if (
            actual.guardando ||
            actual.guardadoRemotoCompleto ||
            actual.registroConfirmado != null ||
            creacionIncierta
        ) return

        _uiState.value = cambio(actual).copy(errorGuardado = null)
    }

    fun registrarFlushing() {
        val actual = _uiState.value
        if (actual.guardando || actual.guardadoRemotoCompleto || creacionIncierta) {
            return
        }

        // Cuando ya recibimos un ID, el siguiente intento solo sube la foto.
        val pendiente = actual.registroConfirmado
        val nuevo = if (pendiente == null) validarRegistro(actual) else null
        if (pendiente == null && nuevo == null) return

        _uiState.value = _uiState.value.copy(
            guardando = true,
            errorGuardado = null,
            errorFoto = null
        )

        viewModelScope.launch {
            var creandoRegistro = false

            try {
                // Revisamos el archivo antes de crear el flushing.
                val foto = actual.fotoRuta?.let { prepararFoto(it) }

                val confirmado = if (pendiente != null) {
                    pendiente
                } else {
                    creandoRegistro = true
                    val respuesta = repository.registrar(requireNotNull(nuevo))

                    require(respuesta.id != null && respuesta.id > 0) {
                        "El servidor no devolvió un identificador válido."
                    }
                    require(respuesta.lineaId == nuevo.lineaId) {
                        "El servidor devolvió una línea diferente."
                    }

                    // Conservamos la confirmación antes de intentar la foto.
                    _uiState.value = _uiState.value.copy(
                        registroConfirmado = respuesta
                    )
                    creandoRegistro = false
                    respuesta
                }

                if (foto != null) {
                    repository.guardarFoto(
                        id = requireNotNull(confirmado.id),
                        foto = foto
                    )
                }

                _uiState.value = _uiState.value.copy(
                    guardadoRemotoCompleto = true,
                    errorGuardado = null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val confirmado = _uiState.value.registroConfirmado

                // Un fallo de red o del servidor durante la creación
                // no garantiza que PostgreSQL haya rechazado el registro.
                if (
                    creandoRegistro &&
                    (e !is HttpException || e.code() >= 500)
                ) {
                    creacionIncierta = true
                }

                val mensaje = when {
                    creacionIncierta ->
                        "No pudimos confirmar si el flushing se guardó. " +
                                "Revisa el historial antes de registrar otro."

                    confirmado != null ->
                        "El flushing ya está guardado, pero su foto no quedó " +
                                "confirmada. ${describirError(e)} " +
                                "Puedes reintentar la foto en este formulario."

                    else -> describirError(e)
                }

                _uiState.value = _uiState.value.copy(
                    errorGuardado = mensaje,
                    guardadoRemotoCompleto = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(guardando = false)
            }
        }
    }

    private fun validarRegistro(
        actual: RegistrarFlushingUiState
    ): FlushingRemotoDto? {
        val linea = actual.lineaId
        val antes = convertirTemperatura(actual.temperaturaAntes)
        val despues = convertirTemperatura(actual.temperaturaDespues)
        val duracion = actual.duracionSegundos.trim().toIntOrNull()
        val observacion = actual.observacion.trim()

        val errorLinea = if (linea == null || linea <= 0) {
            "Selecciona una línea."
        } else null

        val errorObservacion = when {
            observacion.isBlank() -> "Ingresa una observación."
            observacion.length > 500 -> "Usa un máximo de 500 caracteres."
            else -> null
        }

        val errorAntes = validarTemperatura(actual.temperaturaAntes, antes)
        val errorDespues = validarTemperatura(actual.temperaturaDespues, despues)

        val errorDuracion = if (duracion == null || duracion <= 0) {
            "Ingresa una duración entera mayor que cero."
        } else null

        _uiState.value = actual.copy(
            errorLinea = errorLinea,
            errorObservacion = errorObservacion,
            errorTemperaturaAntes = errorAntes,
            errorTemperaturaDespues = errorDespues,
            errorDuracion = errorDuracion,
            errorGuardado = null
        )

        if (
            listOf(
                errorLinea,
                errorObservacion,
                errorAntes,
                errorDespues,
                errorDuracion
            ).any { it != null }
        ) return null

        return FlushingRemotoDto(
            lineaId = requireNotNull(linea).toLong(),
            fechaHora = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.US
            ).format(Date()),
            duracionSegundos = requireNotNull(duracion),
            observacion = observacion,
            temperaturaAntes = requireNotNull(antes),
            temperaturaDespues = requireNotNull(despues)
        )
    }

    private fun convertirTemperatura(texto: String): Double? {
        return texto.trim()
            .replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf { it.isFinite() }
    }

    private fun validarTemperatura(
        texto: String,
        valor: Double?
    ): String? {
        return when {
            texto.isBlank() -> "Ingresa la temperatura."
            valor == null -> "Ingresa una temperatura numérica válida."
            else -> null
        }
    }

    private fun describirError(error: Exception): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                401 -> "La sesión venció. Inicia sesión nuevamente."
                403 -> "Tu usuario no tiene permiso para esta operación."
                404 -> "No se encontró la línea o el registro en el servidor."
                413 -> "La foto supera el tamaño permitido."
                400, 415 -> "El servidor rechazó los datos o el formato de la foto."
                else -> "El servidor respondió con el error ${error.code()}."
            }

            is IOException ->
                "No pudimos comunicarnos con el backend."

            is IllegalArgumentException ->
                error.message ?: "Revisa los datos y la foto seleccionada."

            else ->
                "No se pudo completar la operación."
        }
    }
}