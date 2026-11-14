package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.repository.data.FlushingConsultaRepository
import com.example.aquacontrol.repository.flushing.FlushingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class FlushingViewModel(
    private val repository: FlushingRepository,

    // Recibiremos esta dependencia desde AppNavHost al conectar
    // la pantalla con el servidor y su copia local.
    private val consultaRemota: FlushingConsultaRepository? = null
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<FlushingUiState>(FlushingUiState.Loading)

    val uiState: StateFlow<FlushingUiState> =
        _uiState.asStateFlow()

    private var cargaActual: Job? = null

    fun cargarFlushing(lineaId: Int) {
        // Cancelamos una consulta anterior para no mostrar sus resultados
        // después de que el usuario haya seleccionado otra línea.
        cargaActual?.cancel()

        if (lineaId <= 0) {
            _uiState.value = FlushingUiState.Error(
                "Selecciona una línea válida para consultar su historial."
            )
            return
        }

        _uiState.value = FlushingUiState.Loading

        cargaActual = viewModelScope.launch {
            try {
                val consulta = consultaRemota

                if (consulta != null) {
                    // El repositorio consulta el backend y recurre a Room
                    // únicamente cuando falla la comunicación.
                    val resultado = consulta.obtenerPorLinea(
                        lineaId.toLong()
                    )

                    _uiState.value = if (resultado.registros.isEmpty()) {
                        FlushingUiState.Empty
                    } else {
                        FlushingUiState.Success(
                            registrosRemotos = resultado.registros,
                            desdeCopiaLocal = resultado.desdeCopiaLocal
                        )
                    }
                } else {
                    // Conservamos temporalmente el funcionamiento anterior
                    // hasta actualizar la pantalla y su fábrica en AppNavHost.
                    // No es una alternativa automática ante errores del servidor.
                    val eventos = repository.obtenerFlushingPorLinea(lineaId)

                    _uiState.value = if (eventos.isEmpty()) {
                        FlushingUiState.Empty
                    } else {
                        FlushingUiState.Success(eventos = eventos)
                    }
                }
            } catch (e: CancellationException) {
                // Respetamos la cancelación cuando cambia la consulta.
                throw e
            } catch (e: HttpException) {
                // Una sesión vencida o un permiso insuficiente no significa
                // que el dispositivo esté sin conexión.
                val mensaje = when (e.code()) {
                    401 -> "La sesión no es válida o venció. Inicia sesión nuevamente."
                    403 -> "Tu usuario no tiene permiso para consultar este historial."
                    404 -> "No se encontró el recurso solicitado en el servidor."
                    else -> "El servidor no pudo entregar el historial (${e.code()})."
                }

                _uiState.value = FlushingUiState.Error(mensaje)
            } catch (e: IOException) {
                // Si existían registros descargados, el repositorio ya los
                // devolvió. Este error indica que no pudo ofrecer esa copia.
                _uiState.value = FlushingUiState.Error(
                    e.message ?: "No se pudo conectar ni recuperar el historial."
                )
            } catch (e: Exception) {
                _uiState.value = FlushingUiState.Error(
                    "No se pudo cargar o actualizar la copia del historial. " +
                            "Intenta nuevamente."
                )
            }
        }
    }
}