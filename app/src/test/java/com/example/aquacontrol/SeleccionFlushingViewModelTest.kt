package com.example.aquacontrol

import com.example.aquacontrol.viewmodel.flushing.SeleccionFlushingViewModel
import org.junit.Assert.*
import org.junit.Test

class SeleccionFlushingViewModelTest {
    @Test
    fun seleccionEnCascadaValidaRelacionesYPermiteContinuar() {
        val vm = SeleccionFlushingViewModel()

        assertEquals(2, vm.uiState.value.granjas.size)
        assertFalse(vm.uiState.value.puedeContinuar)

        vm.seleccionarGranja(999)
        assertNull(vm.uiState.value.granjaId)

        vm.seleccionarGranja(1)
        assertEquals(2, vm.uiState.value.galpones.size)
        assertNull(vm.uiState.value.galponId)

        vm.seleccionarGalpon(3) // Galpón 3 pertenece a la Granja Sur.
        assertNull(vm.uiState.value.galponId)
        assertTrue(vm.uiState.value.lineas.isEmpty())

        vm.seleccionarGalpon(2)
        assertEquals(2, vm.uiState.value.galponId)
        assertEquals(1, vm.uiState.value.lineas.size)
        assertNull(vm.uiState.value.lineaId)

        vm.seleccionarLinea(1) // La línea 1 pertenece al galpón 1.
        assertNull(vm.uiState.value.lineaId)
        assertFalse(vm.uiState.value.puedeContinuar)

        vm.seleccionarLinea(3)
        assertEquals(3, vm.uiState.value.lineaId)
        assertTrue(vm.uiState.value.puedeContinuar)
    }

    @Test
    fun cambiarGranjaLimpiaGalponLineaYListasDependientes() {
        val vm = SeleccionFlushingViewModel()
        vm.seleccionarGranja(1)
        vm.seleccionarGalpon(1)
        vm.seleccionarLinea(1)
        assertTrue(vm.uiState.value.puedeContinuar)

        vm.seleccionarGranja(2)

        assertEquals(2, vm.uiState.value.granjaId)
        assertEquals(2, vm.uiState.value.galpones.size)
        assertNull(vm.uiState.value.galponId)
        assertNull(vm.uiState.value.lineaId)
        assertTrue(vm.uiState.value.lineas.isEmpty())
        assertFalse(vm.uiState.value.puedeContinuar)
    }
}
