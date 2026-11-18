package com.example.aquacontrol.viewmodel.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.data.remote.AquaControlApiClient
import com.example.aquacontrol.data.remote.AquaControlApiService
import com.example.aquacontrol.model.linea.LineaMonitoreada
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class DetalleLineaViewModel(
    private val temperaturaRepository: TemperaturaRepository,
    // Inyección opcional para tests; la app utiliza Retrofit con JWT por defecto.
    private val apiInyectada: AquaControlApiService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetalleLineaUiState>(
        DetalleLineaUiState.Loading
    )
    val uiState: StateFlow<DetalleLineaUiState> = _uiState.asStateFlow()
    private var observacionActual: Job? = null

    fun cargarResumen(lineaId: Int) = cargarDatos(lineaId, soloUltimaMedicion = true)

    fun cargarHistorial(lineaId: Int) = cargarDatos(lineaId, soloUltimaMedicion = false)

    private fun cargarDatos(lineaId: Int, soloUltimaMedicion: Boolean) {
        observacionActual?.cancel()
        if (lineaId <= 0) {
            _uiState.value = DetalleLineaUiState.Error(
                "No se pudo identificar la línea seleccionada."
            )
            return
        }
        _uiState.value = DetalleLineaUiState.Loading

        observacionActual = viewModelScope.launch {
            try {
                // Los tests inyectan un fake. En producción se conserva JWT RS256.
                val api = apiInyectada ?: AquaControlApiClient.service

                // No usar BebederoRepository: sus IDs de demostración no son
                // los IDs reales de PostgreSQL.
                val lineaRemota = try {
                    api.obtenerLineaPorId(lineaId.toLong())
                } catch (e: HttpException) {
                    if (e.code() == 404) {
                        _uiState.value = DetalleLineaUiState.Error(
                            "La línea seleccionada no existe."
                        )
                        return@launch
                    }
                    throw e
                }

                if (lineaRemota.id != lineaId.toLong()) {
                    throw IllegalStateException(
                        "El servidor devolvió una línea con identificador distinto."
                    )
                }

                val galponId = Math.toIntExact(lineaRemota.galponId)

                // La ubicación no debe impedir ver una línea que sí existe.
                // Si falla una consulta secundaria, se muestra el detalle igual.
                var nombreGranja = "Granja no identificada"
                var nombreGalpon = "Galpón no identificado"
                try {
                    for (granja in api.listarGranjas()) {
                        val galpon = api.listarGalpones(granja.id)
                            .firstOrNull { it.id == lineaRemota.galponId }
                        if (galpon != null) {
                            nombreGranja = granja.nombre
                            nombreGalpon = galpon.nombre
                            break
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Se mantiene el detalle aun si la ubicación no se resuelve.
                }

                // El repositorio sincroniza desde Spring Boot y observa Room.
                val mediciones = if (soloUltimaMedicion) {
                    temperaturaRepository.observarUltimaMedicionPorLinea(lineaId)
                        .map { listOfNotNull(it) }
                } else {
                    temperaturaRepository.observarHistorialPorLinea(lineaId)
                }

                mediciones.collect { historial ->
                    val linea = LineaMonitoreada(
                        id = Math.toIntExact(lineaRemota.id),
                        nombre = lineaRemota.nombre,
                        galponId = galponId,
                        ultimaMedicion = historial.firstOrNull()
                    )
                    _uiState.value = DetalleLineaUiState.Success(
                        nombreGranja = nombreGranja,
                        nombreGalpon = nombreGalpon,
                        linea = linea,
                        historial = historial
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val mensaje = when (e.code()) {
                    401 -> "Sesión no autorizada. Inicia sesión nuevamente."
                    403 -> "No tienes permisos para consultar esta información."
                    404 -> "El servidor no encontró uno de los recursos consultados."
                    else -> "Error HTTP ${e.code()} al cargar la línea."
                }
                _uiState.value = DetalleLineaUiState.Error(mensaje)
            } catch (e: IOException) {
                _uiState.value = DetalleLineaUiState.Error(
                    "No se pudo conectar con el backend. Verifica la conexión."
                )
            } catch (e: Exception) {
                _uiState.value = DetalleLineaUiState.Error(
                    "No se pudieron cargar los datos de la línea: ${e.message ?: "error desconocido"}"
                )
            }
        }
    }
}
