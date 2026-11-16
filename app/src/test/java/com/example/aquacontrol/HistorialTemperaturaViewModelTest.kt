package com.example.aquacontrol

import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.historial.HistorialTemperaturaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialTemperaturaViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cargaHistorialConUbicacionYControlaPaginacion() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository().apply {
                conteo.value = 61
                pagina.value = List(30) { index ->
                    MedicionTemperatura(
                        id = index.toLong() + 1,
                        lineaId = 1,
                        temperatura = 20.0,
                        fechaHora = index.toLong(),
                        origen = OrigenMedicion.MANUAL
                    )
                }
            }
            val vm = HistorialTemperaturaViewModel(repo)

            vm.cargarHistorial(1)
            runCurrent()

            assertEquals(1, vm.uiState.value.lineaId)
            assertEquals("Granja Norte · Galpón 1 · Línea 1", vm.uiState.value.ubicacion)
            assertEquals(3, vm.uiState.value.totalPaginas)
            assertTrue(vm.uiState.value.puedeIrSiguiente)
            assertFalse(vm.uiState.value.puedeIrAnterior)

            vm.paginaSiguiente()
            runCurrent()
            assertEquals(1, vm.uiState.value.paginaActual)
            assertEquals(31, vm.uiState.value.primerResultadoVisible)
            assertEquals(60, vm.uiState.value.ultimoResultadoVisible)

            vm.paginaAnterior()
            runCurrent()
            assertEquals(0, vm.uiState.value.paginaActual)
        }

    @Test
    fun filtrosInvalidosSeInformanYFiltrosValidosReinicianPagina() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository().apply {
                conteo.value = 61
                pagina.value = List(30) { index ->
                    MedicionTemperatura(
                        id = index.toLong() + 1,
                        lineaId = 1,
                        temperatura = 31.0,
                        fechaHora = index.toLong(),
                        origen = OrigenMedicion.MANUAL
                    )
                }
            }
            val vm = HistorialTemperaturaViewModel(repo)
            vm.cargarHistorial(1)
            runCurrent()
            vm.paginaSiguiente()
            runCurrent()

            val invalido = FiltroHistorialTemperatura(
                desdeInclusive = 20L,
                hastaExclusivo = 10L
            )
            vm.aplicarFiltros(invalido)
            assertEquals(
                "La fecha inicial no puede ser posterior a la fecha final.",
                vm.uiState.value.errorFiltros
            )
            assertEquals(1, vm.uiState.value.paginaActual)

            val valido = FiltroHistorialTemperatura(estado = EstadoLinea.CRITICO)
            vm.aplicarFiltros(valido)
            runCurrent()
            assertEquals(valido, vm.uiState.value.filtro)
            assertEquals(0, vm.uiState.value.paginaActual)
            assertNull(vm.uiState.value.errorFiltros)

            vm.limpiarFiltros()
            runCurrent()
            assertFalse(vm.uiState.value.filtro.tieneFiltros)
        }

    @Test
    fun lineaInvalidaYErroresDeConsultaQuedanEnEstadoRecuperable() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            val vm = HistorialTemperaturaViewModel(repo)

            vm.cargarHistorial(-1)
            assertEquals("No se pudo identificar la línea.", vm.uiState.value.error)

            repo.errorHistorial = IllegalStateException("fallo de prueba")
            vm.cargarHistorial(1)
            advanceUntilIdle()
            assertEquals(
                "No se pudo cargar el historial. Inténtalo nuevamente.",
                vm.uiState.value.error
            )
            assertFalse(vm.uiState.value.cargando)

            repo.errorHistorial = null
            repo.conteo.value = 1
            vm.reintentar()
            runCurrent()
            assertNull(vm.uiState.value.error)
            assertFalse(vm.uiState.value.cargando)
            assertEquals(1, vm.uiState.value.totalResultados)
        }
}
