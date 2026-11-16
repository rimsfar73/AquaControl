package com.example.aquacontrol

import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaUiState
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleLineaViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun rechazaIdInvalidoYLlaveQueNoExiste() = runTest(mainDispatcherRule.dispatcher) {
        val vm = DetalleLineaViewModel(FakeTemperaturaRepository())

        vm.cargarResumen(0)
        assertEquals(
            "No se pudo identificar la línea seleccionada.",
            (vm.uiState.value as DetalleLineaUiState.Error).mensaje
        )

        vm.cargarResumen(99)
        runCurrent()
        assertEquals(
            "La línea seleccionada no existe.",
            (vm.uiState.value as DetalleLineaUiState.Error).mensaje
        )
    }

    @Test
    fun resumenIncluyeUbicacionYUltimaMedicion() = runTest(mainDispatcherRule.dispatcher) {
        val anterior = MedicionTemperatura(1, 1, 18.0, 100, OrigenMedicion.MANUAL)
        val reciente = MedicionTemperatura(2, 1, 22.0, 200, OrigenMedicion.SIMULADA)
        val vm = DetalleLineaViewModel(
            FakeTemperaturaRepository().apply { mediciones.value = listOf(anterior, reciente) }
        )

        vm.cargarResumen(1)
        runCurrent()

        val estado = vm.uiState.value as DetalleLineaUiState.Success
        assertEquals("Granja Norte", estado.nombreGranja)
        assertEquals("Galpón 1", estado.nombreGalpon)
        assertEquals("Línea 1", estado.linea.nombre)
        assertEquals(reciente, estado.linea.ultimaMedicion)
        assertEquals(listOf(reciente), estado.historial)
    }

    @Test
    fun historialDeLineaConservaTodasLasMediciones() = runTest(mainDispatcherRule.dispatcher) {
        val mediciones = listOf(
            MedicionTemperatura(1, 2, 21.0, 100, OrigenMedicion.MANUAL),
            MedicionTemperatura(2, 2, 22.0, 200, OrigenMedicion.SIMULADA)
        )
        val vm = DetalleLineaViewModel(
            FakeTemperaturaRepository().apply { this.mediciones.value = mediciones }
        )

        vm.cargarHistorial(2)
        runCurrent()

        val estado = vm.uiState.value as DetalleLineaUiState.Success
        assertEquals("Granja Norte", estado.nombreGranja)
        assertEquals("Galpón 1", estado.nombreGalpon)
        assertEquals("Línea 2", estado.linea.nombre)
        assertEquals(mediciones, estado.historial)
        assertEquals(mediciones.first(), estado.linea.ultimaMedicion)
    }
}
