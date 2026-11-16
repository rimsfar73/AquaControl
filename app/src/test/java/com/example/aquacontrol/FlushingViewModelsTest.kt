package com.example.aquacontrol

import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.flushing.FlushingDTO
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.repository.flushing.FlushingRepository
import com.example.aquacontrol.viewmodel.flushing.DetalleFlushingViewModel
import com.example.aquacontrol.viewmodel.flushing.FlushingUiState
import com.example.aquacontrol.viewmodel.flushing.FlushingViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlushingViewModelsTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun historialRechazaLineaInvalidaSinConsultarRepositorio() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeFlushingRepository()
            val vm = FlushingViewModel(repo)

            vm.cargarFlushing(0)

            val estado = vm.uiState.value as FlushingUiState.Error
            assertEquals(
                "Selecciona una línea válida para consultar su historial.",
                estado.mensaje
            )
            assertEquals(0, repo.consultas)
        }

    @Test
    fun historialEntregaEventosYRepresentaListaVacia() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeFlushingRepository()
            val vm = FlushingViewModel(repo)

            vm.cargarFlushing(2)
            advanceUntilIdle()
            val resultado = vm.uiState.value as FlushingUiState.Success
            assertEquals(listOf(eventoPrueba()), resultado.eventos)
            assertEquals(1, repo.consultas)

            repo.eventos = emptyList()
            vm.cargarFlushing(3)
            advanceUntilIdle()
            assertSame(FlushingUiState.Empty, vm.uiState.value)
        }

    @Test
    fun detalleCargaEventoYRespetaPermisoDeEdicion() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeFlushingRepository().apply { evento = eventoPrueba() }
            val vm = DetalleFlushingViewModel(repo) { true }

            vm.cargarEvento(2, 7)
            advanceUntilIdle()
            assertEquals(eventoPrueba(), vm.uiState.value.evento)
            assertFalse(vm.uiState.value.cargando)

            vm.iniciarEdicion(PerfilUsuario.SUPERVISOR)
            assertFalse(vm.uiState.value.editando)
            vm.iniciarEdicion(PerfilUsuario.OPERARIO)
            assertTrue(vm.uiState.value.editando)
            vm.actualizarObservacion("borrador", PerfilUsuario.SUPERVISOR)
            assertEquals("lavado inicial", vm.uiState.value.observacion)

            vm.actualizarObservacion("borrador", PerfilUsuario.OPERARIO)
            vm.cancelarEdicion()
            assertFalse(vm.uiState.value.editando)
            assertEquals("lavado inicial", vm.uiState.value.observacion)
        }

    @Test
    fun detalleInformaRegistroInvalidoYRegistroInexistente() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeFlushingRepository()
            val vm = DetalleFlushingViewModel(repo) { true }

            vm.cargarEvento(0, 7)
            assertEquals(
                "No se pudo identificar el registro de flushing.",
                vm.uiState.value.errorCarga
            )

            vm.cargarEvento(2, 8)
            advanceUntilIdle()
            assertEquals(
                "El registro ya no existe en esta línea.",
                vm.uiState.value.errorCarga
            )
        }

    @Test
    fun detalleValidaAntesDeGuardarYActualizaConComaDecimal() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeFlushingRepository().apply { evento = eventoPrueba() }
            val vm = DetalleFlushingViewModel(repo) { true }
            vm.cargarEvento(2, 7)
            advanceUntilIdle()
            vm.iniciarEdicion(PerfilUsuario.OPERARIO)
            vm.actualizarObservacion("", PerfilUsuario.OPERARIO)
            vm.guardarCambios(PerfilUsuario.OPERARIO)

            assertEquals(0, repo.actualizaciones)
            assertEquals("Ingresa una observación.", vm.uiState.value.errorObservacion)

            vm.actualizarObservacion("lavado corregido", PerfilUsuario.OPERARIO)
            vm.actualizarTemperaturaAntes("25,5", PerfilUsuario.OPERARIO)
            vm.actualizarTemperaturaDespues("19,5", PerfilUsuario.OPERARIO)
            vm.actualizarDuracion("120", PerfilUsuario.OPERARIO)
            vm.guardarCambios(PerfilUsuario.OPERARIO)
            advanceUntilIdle()

            assertEquals(1, repo.actualizaciones)
            assertEquals(25.5, repo.ultimaTemperaturaAntes ?: 0.0, 0.0)
            assertEquals(19.5, repo.ultimaTemperaturaDespues ?: 0.0, 0.0)
            assertEquals("Flushing actualizado correctamente.", vm.uiState.value.mensaje)
            assertFalse(vm.uiState.value.guardando)
        }

    @Test
    fun detallePermiteEliminarSoloAlSupervisor() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeFlushingRepository().apply { evento = eventoPrueba() }
            val vm = DetalleFlushingViewModel(repo) { true }
            vm.cargarEvento(2, 7)
            advanceUntilIdle()

            vm.eliminarEvento(PerfilUsuario.OPERARIO)
            assertEquals(0, repo.eliminaciones)
            assertEquals("Solo el supervisor puede eliminar flushing.", vm.uiState.value.errorOperacion)

            vm.eliminarEvento(PerfilUsuario.SUPERVISOR)
            advanceUntilIdle()
            assertEquals(1, repo.eliminaciones)
            assertTrue(vm.uiState.value.eliminado)
            assertEquals("Flushing eliminado correctamente.", vm.uiState.value.mensaje)
        }

    @Test
    fun detalleTraduceErroresDeCargaYActualizacion() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeFlushingRepository().apply {
                evento = eventoPrueba()
                errorObtener = IllegalStateException("detalle")
                errorActualizar = IllegalArgumentException("actualización rechazada")
            }
            val vm = DetalleFlushingViewModel(repo) { true }

            vm.cargarEvento(2, 7)
            advanceUntilIdle()
            assertEquals(
                "No se pudo cargar el registro. Inténtalo nuevamente.",
                vm.uiState.value.errorCarga
            )

            repo.errorObtener = null
            vm.cargarEvento(2, 7, forzar = true)
            advanceUntilIdle()
            vm.iniciarEdicion(PerfilUsuario.OPERARIO)
            vm.actualizarObservacion("edición", PerfilUsuario.OPERARIO)
            vm.guardarCambios(PerfilUsuario.OPERARIO)
            advanceUntilIdle()
            assertEquals("actualización rechazada", vm.uiState.value.errorOperacion)
            assertFalse(vm.uiState.value.guardando)
        }

    @Test
    fun detalleInformaSiNoPuedeLimpiarLaFotoTrasEliminar() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeFlushingRepository().apply {
                evento = eventoPrueba().copy(fotoRuta = "foto.jpg")
                eventoEliminado = evento
            }
            val limpiezaFoto = CompletableDeferred<Boolean>()
            val vm = DetalleFlushingViewModel(repo) {
                limpiezaFoto.complete(false)
                false
            }

            vm.cargarEvento(2, 7)
            advanceUntilIdle()
            vm.eliminarEvento(PerfilUsuario.SUPERVISOR)
            advanceUntilIdle()
            assertFalse(limpiezaFoto.await())
            advanceUntilIdle()

            assertTrue(vm.uiState.value.eliminado)
            assertEquals(
                "Flushing eliminado. No se pudo borrar su archivo de fotografía.",
                vm.uiState.value.mensaje
            )
        }

    private fun eventoPrueba() = EventoFlushing(
        id = 7,
        lineaId = 2,
        fechaHora = "2026-11-15T10:00:00",
        temperaturaAntes = 25.0,
        temperaturaDespues = 19.0,
        duracionSegundos = 120,
        observacion = "lavado inicial"
    )
}

private class FakeFlushingRepository : FlushingRepository {
    var eventos: List<EventoFlushing> = listOf(eventoPruebaStatic())
    var evento: EventoFlushing? = null
    var consultas = 0
    var actualizaciones = 0
    var eliminaciones = 0
    var ultimaTemperaturaAntes: Double? = null
    var ultimaTemperaturaDespues: Double? = null
    var errorObtener: Throwable? = null
    var errorActualizar: Throwable? = null
    var eventoEliminado: EventoFlushing? = null

    override suspend fun registrarFlushing(dto: FlushingDTO): EventoFlushing =
        evento ?: eventoPruebaStatic()

    override suspend fun obtenerFlushingPorLinea(lineaId: Int): List<EventoFlushing> {
        consultas++
        return eventos
    }

    override suspend fun obtenerEvento(lineaId: Int, eventoId: Int): EventoFlushing? {
        errorObtener?.let { throw it }
        return evento?.takeIf { it.lineaId == lineaId && it.id == eventoId }
    }

    override suspend fun actualizarFlushing(
        lineaId: Int,
        eventoId: Int,
        observacion: String,
        temperaturaAntes: Double,
        temperaturaDespues: Double,
        duracionSegundos: Int,
        perfil: PerfilUsuario
    ): EventoFlushing {
        actualizaciones++
        errorActualizar?.let { throw it }
        ultimaTemperaturaAntes = temperaturaAntes
        ultimaTemperaturaDespues = temperaturaDespues
        return requireNotNull(evento).copy(
            observacion = observacion,
            temperaturaAntes = temperaturaAntes,
            temperaturaDespues = temperaturaDespues,
            duracionSegundos = duracionSegundos
        ).also { evento = it }
    }

    override suspend fun eliminarFlushing(
        lineaId: Int,
        eventoId: Int,
        perfil: PerfilUsuario
    ): EventoFlushing {
        eliminaciones++
        return eventoEliminado ?: requireNotNull(evento)
    }
}

private fun eventoPruebaStatic() = EventoFlushing(
    id = 7,
    lineaId = 2,
    fechaHora = "2026-11-15T10:00:00",
    temperaturaAntes = 25.0,
    temperaturaDespues = 19.0,
    duracionSegundos = 120,
    observacion = "lavado inicial"
)
