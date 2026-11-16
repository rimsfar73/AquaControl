package com.example.aquacontrol

import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.repository.temperatura.TemperaturaRepository
import com.example.aquacontrol.viewmodel.supervisor.SupervisorUiState
import com.example.aquacontrol.viewmodel.supervisor.SupervisorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupervisorViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun resumeEstadosCuentaCadaLineaUnaVezYMarcaLasSinLectura() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FakeTemperaturaRepository().apply {
                mediciones.value = listOf(
                    MedicionTemperatura(1, 1, 20.0, 1, OrigenMedicion.MANUAL),
                    MedicionTemperatura(2, 2, 22.0, 2, OrigenMedicion.MANUAL),
                    MedicionTemperatura(3, 3, 31.0, 3, OrigenMedicion.MANUAL),
                    MedicionTemperatura(4, 4, 17.0, 4, OrigenMedicion.SIMULADA)
                )
            }
            val vm = SupervisorViewModel(repo)
            runCurrent()

            val estado = vm.uiState.value as SupervisorUiState.Success
            assertEquals(1, estado.lineasNormales)
            assertEquals(2, estado.lineasEnAdvertencia)
            assertEquals(1, estado.lineasCriticas)
            assertEquals(1, estado.lineasSinMediciones)
        }

    @Test
    fun unaFallaDeObservacionSeExponeComoErrorControlado() =
        runTest(mainDispatcherRule.dispatcher) {
            val repo = FailingSupervisorRepository()
            val vm = SupervisorViewModel(repo)
            runCurrent()

            assertEquals(
                "No se pudo cargar el resumen. Intenta nuevamente.",
                (vm.uiState.value as SupervisorUiState.Error).mensaje
            )
        }
}

private class FailingSupervisorRepository : TemperaturaRepository by FakeTemperaturaRepository() {
    override fun observarUltimasMediciones(): Flow<List<MedicionTemperatura>> = flow {
        throw IllegalStateException("fallo de prueba")
    }
}
