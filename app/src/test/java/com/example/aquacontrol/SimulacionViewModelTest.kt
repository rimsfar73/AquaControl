package com.example.aquacontrol

import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.simulacion.SimulacionViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulacionViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun cargaLineasIgnoraSeleccionInvalidaYAplicaEscenarioDuranteLaSimulacion() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            var registros = 0
            repo.onRegistrar = { lineaId, temperatura, origen ->
                registros++
                assertTrue(lineaId > 0)
                assertTrue(temperatura.isFinite())
                assertEquals(OrigenMedicion.SIMULADA, origen)
                MedicionTemperatura(registros.toLong(), lineaId, temperatura, registros.toLong(), origen)
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
            assertEquals(5, registros)

            vm.aplicarEscenario()
            assertTrue(vm.uiState.value.mensajeEscenario.orEmpty().contains("Calentamiento"))
            assertTrue(vm.uiState.value.puedeAplicarEscenario)

            vm.detener()
            runCurrent()
            assertFalse(vm.uiState.value.activa)
            assertNull(vm.uiState.value.mensajeEscenario)
        }

    @Test
    fun simulacionRemotaPermiteSeleccionarLineaSeis() =
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
