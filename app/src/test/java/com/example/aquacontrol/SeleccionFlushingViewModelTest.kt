package com.example.aquacontrol

import com.example.aquacontrol.viewmodel.flushing.SeleccionFlushingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SeleccionFlushingViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun seleccionEnCascadaValidaRelacionesYPermiteLineaRemotaSeis() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = SeleccionFlushingViewModel(FakeCatalogoApi(conLineaSeis = true))
            runCurrent()
            assertEquals(2, vm.uiState.value.granjas.size)
            vm.seleccionarGranja(3)
            runCurrent()
            assertEquals(2, vm.uiState.value.galpones.size)
            vm.seleccionarGalpon(5)
            runCurrent()
            assertEquals(2, vm.uiState.value.lineas.size)
            vm.seleccionarLinea(6)
            assertEquals(6, vm.uiState.value.lineaId)
            assertTrue(vm.uiState.value.puedeContinuar)
        }

    @Test fun cambioDeGranjaLimpiaLaSeleccion() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = SeleccionFlushingViewModel(FakeCatalogoApi())
            runCurrent()
            vm.seleccionarGranja(1)
            runCurrent()
            vm.seleccionarGalpon(1)
            runCurrent()
            vm.seleccionarLinea(1)
            assertTrue(vm.uiState.value.puedeContinuar)
            vm.seleccionarGranja(3)
            runCurrent()
            assertNull(vm.uiState.value.galponId)
            assertNull(vm.uiState.value.lineaId)
            assertTrue(vm.uiState.value.lineas.isEmpty())
            assertFalse(vm.uiState.value.puedeContinuar)
        }
}
