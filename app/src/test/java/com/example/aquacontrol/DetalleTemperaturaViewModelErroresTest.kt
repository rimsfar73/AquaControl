package com.example.aquacontrol

import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.temperatura.DetalleTemperaturaViewModel
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleTemperaturaViewModelErroresTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun detalleMuestraErroresDeIdentidadRegistroAusenteYConsulta() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            val vm = DetalleTemperaturaViewModel(repo)

            vm.cargarMedicion(0, 1)
            assertEquals("No se pudo identificar la medición.", vm.uiState.value.errorCarga)

            vm.cargarMedicion(1, 1)
            advanceUntilIdle()
            assertEquals("La medición ya no existe en esta línea.", vm.uiState.value.errorCarga)

            repo.onObtener = { _, _ -> throw IOException("consulta fallida") }
            vm.cargarMedicion(1, 2)
            advanceUntilIdle()
            assertEquals(
                "No se pudo cargar la medición. Inténtalo nuevamente.",
                vm.uiState.value.errorCarga
            )
        }

    @Test
    fun mantieneBorradorYEstadoRecuperableCuandoFallaEditarOEliminar() =
        runTest(mainDispatcherRule.dispatcher) {
            val original = MedicionTemperatura(5, 1, 21.0, 100, OrigenMedicion.MANUAL)
            val repo = FakeTemperaturaRepository().apply {
                onObtener = { _, _ -> original }
                onActualizar = { _, _, _, _ -> throw IllegalStateException("servidor rechazó") }
                onEliminar = { _, _, _ -> throw IOException("sin conexión") }
            }
            val vm = DetalleTemperaturaViewModel(repo)
            vm.cargarMedicion(1, 5)
            advanceUntilIdle()
            vm.iniciarEdicion(PerfilUsuario.OPERARIO)
            vm.actualizarTemperatura("23,5", PerfilUsuario.OPERARIO)
            vm.guardarCambios(PerfilUsuario.OPERARIO)
            advanceUntilIdle()

            assertEquals("servidor rechazó", vm.uiState.value.errorOperacion)
            assertTrue(vm.uiState.value.editando)
            assertFalse(vm.uiState.value.guardando)

            vm.eliminarMedicion(PerfilUsuario.SUPERVISOR)
            advanceUntilIdle()
            assertEquals(
                "No se pudo eliminar la medición. Inténtalo nuevamente.",
                vm.uiState.value.errorOperacion
            )
            assertFalse(vm.uiState.value.eliminando)
            assertEquals(original, vm.uiState.value.medicion)
        }
}
