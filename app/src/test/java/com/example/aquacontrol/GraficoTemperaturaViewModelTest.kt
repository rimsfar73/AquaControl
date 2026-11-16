package com.example.aquacontrol

import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.temperatura.GraficoTemperaturaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GraficoTemperaturaViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun graficoObservaMedicionesYReintentaLaMismaLinea() =
        runTest(mainDispatcherRule.dispatcher) {
            val medicion = MedicionTemperatura(
                3, 1, 22.5, 1_000L, OrigenMedicion.MANUAL
            )
            val repo = FakeTemperaturaRepository().apply {
                mediciones.value = listOf(medicion)
            }
            val vm = GraficoTemperaturaViewModel(repo)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vm.uiState.collect { }
            }

            vm.cargar(1)
            runCurrent()

            val estado = vm.uiState.value
            assertFalse(estado.cargando)
            assertNull(estado.error)
            assertEquals(listOf(medicion), estado.mediciones)
            assertTrue(estado.hastaInclusive > estado.desdeInclusive)
            assertEquals(24L * 60L * 60L * 1_000L, estado.hastaInclusive - estado.desdeInclusive)

            vm.reintentar()
            runCurrent()
            assertFalse(vm.uiState.value.cargando)
            assertEquals(listOf(medicion), vm.uiState.value.mediciones)
        }

    @Test
    fun graficoInformaLineaInvalida() = runTest(mainDispatcherRule.dispatcher) {
        val vm = GraficoTemperaturaViewModel(FakeTemperaturaRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.uiState.collect { }
        }

        vm.cargar(0)
        runCurrent()

        assertFalse(vm.uiState.value.cargando)
        assertEquals(
            "No se pudo identificar la línea seleccionada.",
            vm.uiState.value.error
        )
    }
}
