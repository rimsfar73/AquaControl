package com.example.aquacontrol

import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.flushing.FlushingDTO
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.repository.flushing.FlushingRepository
import com.example.aquacontrol.viewmodel.flushing.FlushingUiState
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import retrofit2.HttpException
import retrofit2.Response
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlushingViewModelBranchesTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun traduceErroresHttpSegunSesionPermisoYRecurso() =
        runTest(mainDispatcherRule.dispatcher) {
            val casos = mapOf(
                401 to "La sesión no es válida o venció. Inicia sesión nuevamente.",
                403 to "Tu usuario no tiene permiso para consultar este historial.",
                404 to "No se encontró el recurso solicitado en el servidor.",
                503 to "El servidor no pudo entregar el historial (503)."
            )

            casos.forEach { (codigo, esperado) ->
                val repo = ErrorFlushingRepository().apply {
                    error = HttpException(
                        Response.error<Any>(codigo, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "error"))
                    )
                }
                val vm = FlushingViewModel(repo)
                vm.cargarFlushing(1)
                advanceUntilIdle()

                assertEquals(esperado, (vm.uiState.value as FlushingUiState.Error).mensaje)
            }
        }

    @Test
    fun conservaMensajesDeRedYControlaErroresInesperados() =
        runTest(mainDispatcherRule.dispatcher) {
            val mensajes = listOf(
                IOException("sin conexión") to "sin conexión",
                IllegalStateException("detalle interno") to
                        "No se pudo cargar o actualizar la copia del historial. Intenta nuevamente."
            )

            mensajes.forEach { (error, esperado) ->
                val vm = FlushingViewModel(ErrorFlushingRepository().apply { this.error = error })
                vm.cargarFlushing(1)
                advanceUntilIdle()

                assertEquals(esperado, (vm.uiState.value as FlushingUiState.Error).mensaje)
            }
        }
}

private class ErrorFlushingRepository : FlushingRepository {
    var error: Throwable? = null

    override suspend fun registrarFlushing(dto: FlushingDTO): EventoFlushing = unused()

    override suspend fun obtenerFlushingPorLinea(lineaId: Int): List<EventoFlushing> {
        error?.let { throw it }
        return emptyList()
    }

    override suspend fun obtenerEvento(lineaId: Int, eventoId: Int): EventoFlushing? = unused()

    override suspend fun actualizarFlushing(
        lineaId: Int,
        eventoId: Int,
        observacion: String,
        temperaturaAntes: Double,
        temperaturaDespues: Double,
        duracionSegundos: Int,
        perfil: PerfilUsuario
    ): EventoFlushing = unused()

    override suspend fun eliminarFlushing(
        lineaId: Int,
        eventoId: Int,
        perfil: PerfilUsuario
    ): EventoFlushing = unused()

    private fun <T> unused(): T = throw UnsupportedOperationException("No se usa en esta prueba")
}
