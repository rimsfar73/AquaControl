package com.example.aquacontrol

import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.alertas.AlertasUiState
import com.example.aquacontrol.viewmodel.alertas.AlertasViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertasViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun separaNormalesSinMedicionYOrdenaCriticasAntesDeAdvertencias() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository().apply {
                mediciones.value = listOf(
                    MedicionTemperatura(1, 1, 22.0, 1, OrigenMedicion.MANUAL),
                    MedicionTemperatura(2, 2, 19.0, 2, OrigenMedicion.MANUAL),
                    MedicionTemperatura(3, 3, 31.0, 3, OrigenMedicion.MANUAL)
                )
            }
            val vm = AlertasViewModel(repo, api = FakeCatalogoApi())
            runCurrent()

            val estado = vm.uiState.value as AlertasUiState.Success
            assertEquals(5, estado.totalLineas)
            assertEquals(2, estado.lineasSinMediciones)
            assertEquals(2, estado.alertas.size)
            assertEquals(31.0, estado.alertas[0].medicion.temperatura, 0.0)
            assertEquals(22.0, estado.alertas[1].medicion.temperatura, 0.0)
            assertEquals("Granja Norte", estado.alertas[0].nombreGranja)
        }

    @Test fun catalogoRemotoIncluyeLineaSeisYActualizaAlerta() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository()
            val vm = AlertasViewModel(repo, api = FakeCatalogoApi(conLineaSeis = true))
            runCurrent()
            repo.mediciones.value = listOf(
                MedicionTemperatura(100, 6, 1.3, 100L, OrigenMedicion.MANUAL)
            )
            runCurrent()
            val estado = vm.uiState.value as AlertasUiState.Success
            assertEquals(6, estado.totalLineas)
            assertEquals(1, estado.alertas.size)
            assertEquals(6, estado.alertas[0].medicion.lineaId)
            assertEquals(1.3, estado.alertas[0].medicion.temperatura, 0.0)
            repo.mediciones.value = listOf(
                MedicionTemperatura(101, 6, 56.0, 101L, OrigenMedicion.MANUAL)
            )
            runCurrent()
            val actual = vm.uiState.value as AlertasUiState.Success
            assertEquals(56.0, actual.alertas.single().medicion.temperatura, 0.0)
        }

    @Test fun sinMedicionesMuestraEstadoExitosoSinAlertas() =
        runTest(mainDispatcherRule.dispatcher) {
            val vm = AlertasViewModel(FakeTemperaturaRepository(), api = FakeCatalogoApi())
            runCurrent()

            val estado = vm.uiState.value as AlertasUiState.Success
            assertTrue(estado.alertas.isEmpty())
            assertEquals(5, estado.lineasSinMediciones)
        }
}
