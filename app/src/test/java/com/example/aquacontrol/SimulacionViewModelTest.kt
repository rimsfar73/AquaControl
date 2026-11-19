package com.example.aquacontrol

import com.example.aquacontrol.data.simulacion.GeneradorTemperatura
import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.simulacion.SimulacionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class SimulacionViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cargaCatalogoYSimulaTodasLasLineasPorDefecto() =
        runTest(mainDispatcherRule.dispatcher) {
            val ids = mutableListOf<Int>()
            val repo = FakeTemperaturaRepository().apply {
                onRegistrar = { lineaId, temperatura, origen ->
                    ids.add(lineaId)
                    assertTrue(temperatura.isFinite())
                    assertEquals(OrigenMedicion.SIMULADA, origen)
                    MedicionTemperatura(ids.size.toLong(), lineaId, temperatura,
                        ids.size.toLong(), origen)
                }
            }
            val vm = SimulacionViewModel(repo, api = FakeCatalogoApi())
            runCurrent()

            assertEquals(5, vm.uiState.value.lineas.size)
            vm.seleccionarLinea(999)
            assertNull(vm.uiState.value.lineaSeleccionadaId)
            vm.seleccionarLinea(1)
            vm.seleccionarEscenario(EscenarioSimulacion.CALENTAMIENTO)
            assertFalse(vm.uiState.value.puedeAplicarEscenario)

            vm.iniciar()
            runCurrent()
            assertTrue(vm.uiState.value.activa)
            assertEquals(5, vm.uiState.value.medicionesGeneradas)
            assertEquals(setOf(1, 2, 3, 4, 5), vm.lineasEnSimulacion.value)
            assertEquals(5, ids.distinct().size)

            vm.aplicarEscenario()
            assertEquals(EscenarioSimulacion.CALENTAMIENTO,
                vm.escenariosManuales.value[1]?.escenario)
            assertTrue(vm.uiState.value.mensajeEscenario.orEmpty().contains("Calentamiento"))

            vm.detener()
            runCurrent()
            assertFalse(vm.uiState.value.activa)
            assertTrue(vm.escenariosManuales.value.isEmpty())
            assertTrue(vm.lineasEnSimulacion.value.isEmpty())
        }

    @Test
    fun modoIndividualRegistraSoloLaLineaSeleccionada() =
        runTest(mainDispatcherRule.dispatcher) {
            val ids = mutableListOf<Int>()
            val repo = FakeTemperaturaRepository().apply {
                onRegistrar = { lineaId, temperatura, origen ->
                    ids.add(lineaId)
                    MedicionTemperatura(ids.size.toLong(), lineaId, temperatura,
                        ids.size.toLong(), origen)
                }
            }
            val vm = SimulacionViewModel(repo, api = FakeCatalogoApi())
            runCurrent()
            vm.seleccionarLinea(2)
            vm.establecerSoloLineaSeleccionada(true)
            vm.iniciar()
            runCurrent()

            assertTrue(vm.uiState.value.activa)
            assertEquals(setOf(2), vm.lineasEnSimulacion.value)
            assertEquals(listOf(2), ids)

            advanceTimeBy(15_000)
            runCurrent()
            assertTrue(ids.size >= 3)
            assertTrue(ids.all { it == 2 })
            vm.detener()
        }

    @Test
    fun modoIndividualExigeSeleccionarUnaLinea() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = SimulacionViewModel(
                FakeTemperaturaRepository(), api = FakeCatalogoApi()
            )
            runCurrent()
            vm.establecerSoloLineaSeleccionada(true)
            vm.iniciar()
            runCurrent()

            assertFalse(vm.uiState.value.activa)
            assertTrue(vm.uiState.value.error.orEmpty().contains("Selecciona una línea"))
            assertTrue(vm.lineasEnSimulacion.value.isEmpty())
        }

    @Test
    fun escenarioManualSeMantieneHastaLiberarElControl() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = SimulacionViewModel(
                FakeTemperaturaRepository(),
                generador = GeneradorTemperatura(Random(42)),
                api = FakeCatalogoApi()
            )
            runCurrent()
            vm.seleccionarLinea(2)
            vm.seleccionarEscenario(EscenarioSimulacion.ENFRIAMIENTO)
            vm.iniciar()
            runCurrent()
            vm.aplicarEscenario()

            assertEquals(EscenarioSimulacion.ENFRIAMIENTO,
                vm.escenariosManuales.value[2]?.escenario)
            assertTrue(vm.escenariosManuales.value[2]!!.objetivo in 8.0..16.0)
            assertTrue(vm.uiState.value.mensajeEscenario.orEmpty().contains("mantiene"))

            advanceTimeBy(60_000)
            runCurrent()
            assertEquals(EscenarioSimulacion.ENFRIAMIENTO,
                vm.escenariosManuales.value[2]?.escenario)

            vm.liberarEscenarioSeleccionado()
            assertFalse(vm.escenariosManuales.value.containsKey(2))
            assertTrue(vm.uiState.value.mensajeEscenario.orEmpty().contains("automático"))
            vm.detener()
        }

    @Test
    fun generadorEnfriamientoManualNoCambiaACalorCritico() {
        val generador = GeneradorTemperatura(Random(42))
        val inicial = generador.generarTemperatura(3)
        generador.seleccionarEscenario(3, EscenarioSimulacion.ENFRIAMIENTO)

        val lecturas = (1..120).map { generador.generarTemperatura(3) }
        assertTrue(lecturas.first() < inicial)
        assertTrue(lecturas.takeLast(60).all { it in 8.0..16.0 })
        assertEquals(EscenarioSimulacion.ENFRIAMIENTO,
            generador.escenarioManual(3))

        generador.liberarEscenario(3)
        assertNull(generador.escenarioManual(3))
        generador.reiniciar()
        assertNull(generador.objetivoDeLinea(3))
    }

    @Test
    fun generadorCriticoFrioManualPermaneceEnSuRango() {
        val generador = GeneradorTemperatura(Random(73))
        generador.generarTemperatura(1)
        generador.seleccionarEscenario(1, EscenarioSimulacion.CRITICO_FRIO)
        val lecturas = (1..120).map { generador.generarTemperatura(1) }
        assertTrue(lecturas.takeLast(60).all { it in 1.0..3.5 })
        assertEquals(EscenarioSimulacion.CRITICO_FRIO,
            generador.escenarioManual(1))
    }

    @Test
    fun catalogoRemotoIncluyeLineaSeis() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = SimulacionViewModel(
                FakeTemperaturaRepository(), api = FakeCatalogoApi(conLineaSeis = true)
            )
            runCurrent()
            assertEquals(6, vm.uiState.value.lineas.size)
            vm.seleccionarLinea(6)
            assertEquals(6, vm.uiState.value.lineaSeleccionadaId)
            vm.detener()
        }

    @Test
    fun errorAlRegistrarDetieneLaSimulacionConMensaje() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository().apply {
                onRegistrar = { _, _, _ -> throw IllegalStateException("fallo de prueba") }
            }
            val vm = SimulacionViewModel(repo, api = FakeCatalogoApi())
            vm.iniciar()
            advanceUntilIdle()

            assertFalse(vm.uiState.value.activa)
            assertEquals(
                "La simulación se detuvo por un error. Las mediciones ya guardadas se conservan.",
                vm.uiState.value.error
            )
            assertEquals(0, vm.uiState.value.medicionesGeneradas)
        }
}
