package com.example.aquacontrol

import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.temperatura.DetalleTemperaturaViewModel
import com.example.aquacontrol.viewmodel.temperatura.RegistrarTemperaturaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTemperaturaTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun registroValidaCamposYNoLlamaAlRepositorioSiFaltan() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            var llamadas = 0
            repo.onRegistrar = { linea, temp, origen ->
                llamadas++
                MedicionTemperatura(10, linea, temp, 100, origen)
            }
            val vm = RegistrarTemperaturaViewModel(repo)

            vm.registrarTemperatura()
            advanceUntilIdle()

            assertEquals(0, llamadas)
            assertNotNull(vm.uiState.value.errorLinea)
            assertNotNull(vm.uiState.value.errorTemperatura)

            vm.seleccionarLinea(1)
            vm.actualizarTemperatura("texto")
            vm.registrarTemperatura()
            advanceUntilIdle()
            assertEquals(0, llamadas)
            assertNotNull(vm.uiState.value.errorTemperatura)
        }

    @Test fun registroAceptaComaDecimalYActualizaEstadoExitoso() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            var parametros: Triple<Int, Double, OrigenMedicion>? = null
            repo.onRegistrar = { linea, temp, origen ->
                parametros = Triple(linea, temp, origen)
                MedicionTemperatura(12, linea, temp, 500, origen)
            }
            val vm = RegistrarTemperaturaViewModel(repo)
            vm.seleccionarLinea(2)
            vm.actualizarTemperatura(" 21,5 ")
            vm.registrarTemperatura()
            advanceUntilIdle()

            assertEquals(Triple(2, 21.5, OrigenMedicion.MANUAL), parametros)
            assertEquals(12L, vm.uiState.value.medicionGuardada?.id)
            assertFalse(vm.uiState.value.guardando)
            assertNull(vm.uiState.value.errorGuardado)
        }

    @Test fun registroMuestraErrorControladoDelRepositorio() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository().apply {
                onRegistrar = { _, _, _ -> throw IllegalArgumentException("Línea no válida") }
            }
            val vm = RegistrarTemperaturaViewModel(repo)
            vm.seleccionarLinea(1)
            vm.actualizarTemperatura("20")
            vm.registrarTemperatura()
            advanceUntilIdle()

            assertEquals("Línea no válida", vm.uiState.value.errorGuardado)
            assertFalse(vm.uiState.value.guardando)
        }

    @Test fun detalleCargaEdicionOperarioYCancelacionConservaRegistro() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            val original = MedicionTemperatura(8, 1, 21.0, 1000, OrigenMedicion.MANUAL)
            repo.onObtener = { _, _ -> original }
            val vm = DetalleTemperaturaViewModel(repo)

            vm.cargarMedicion(1, 8)
            advanceUntilIdle()
            assertEquals(original, vm.uiState.value.medicion)
            assertTrue(vm.uiState.value.puedeInteractuar)

            vm.iniciarEdicion(PerfilUsuario.SUPERVISOR)
            assertFalse(vm.uiState.value.editando)
            vm.iniciarEdicion(PerfilUsuario.OPERARIO)
            vm.actualizarTemperatura("22,5", PerfilUsuario.OPERARIO)
            assertEquals(21.0, vm.uiState.value.medicion?.temperatura ?: 0.0, 0.0)
            vm.cancelarEdicion()

            assertFalse(vm.uiState.value.editando)
            assertEquals("21.0", vm.uiState.value.temperatura)
            assertEquals(original, vm.uiState.value.medicion)
        }

    @Test fun detalleGuardaTemperaturaYRechazaValorInvalido() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            val original = MedicionTemperatura(8, 1, 21.0, 1000, OrigenMedicion.MANUAL)
            repo.onObtener = { _, _ -> original }
            var nuevoValor = 0.0
            repo.onActualizar = { linea, id, temperatura, perfil ->
                assertEquals(PerfilUsuario.OPERARIO, perfil)
                nuevoValor = temperatura
                original.copy(lineaId = linea, id = id, temperatura = temperatura)
            }
            val vm = DetalleTemperaturaViewModel(repo)
            vm.cargarMedicion(1, 8)
            advanceUntilIdle()
            vm.iniciarEdicion(PerfilUsuario.OPERARIO)
            vm.actualizarTemperatura("abc", PerfilUsuario.OPERARIO)
            vm.guardarCambios(PerfilUsuario.OPERARIO)
            assertNotNull(vm.uiState.value.errorTemperatura)
            assertEquals(0.0, nuevoValor, 0.0)

            vm.actualizarTemperatura("22,5", PerfilUsuario.OPERARIO)
            vm.guardarCambios(PerfilUsuario.OPERARIO)
            advanceUntilIdle()
            assertEquals(22.5, nuevoValor, 0.0)
            assertEquals(22.5, vm.uiState.value.medicion?.temperatura ?: 0.0, 0.0)
            assertFalse(vm.uiState.value.editando)
            assertEquals("Temperatura actualizada correctamente.", vm.uiState.value.mensaje)
        }

    @Test fun detalleEliminaSoloConSupervisorYActualizaEstado() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            repo.onObtener = { _, _ -> MedicionTemperatura(8, 1, 21.0, 1000, OrigenMedicion.MANUAL) }
            var eliminaciones = 0
            repo.onEliminar = { _, _, perfil ->
                assertEquals(PerfilUsuario.SUPERVISOR, perfil)
                eliminaciones++
            }
            val vm = DetalleTemperaturaViewModel(repo)
            vm.cargarMedicion(1, 8)
            advanceUntilIdle()
            vm.eliminarMedicion(PerfilUsuario.OPERARIO)
            assertEquals(0, eliminaciones)
            assertNotNull(vm.uiState.value.errorOperacion)

            vm.eliminarMedicion(PerfilUsuario.SUPERVISOR)
            advanceUntilIdle()
            assertEquals(1, eliminaciones)
            assertTrue(vm.uiState.value.eliminado)
            assertNull(vm.uiState.value.medicion)
        }
}
