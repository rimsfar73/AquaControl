package com.example.aquacontrol

import com.example.aquacontrol.data.flushing.remote.FlushingApiService
import com.example.aquacontrol.data.flushing.remote.FlushingRemoteDataSource
import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto
import com.example.aquacontrol.data.flushing.remote.FotoFlushingRemotaDto
import com.example.aquacontrol.repository.data.FlushingRemoteRepository
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrarFlushingViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun formularioInvalidoMuestraErroresYNoLlamaAlBackend() =
        runTest(mainDispatcherRule.dispatcher) {
            val api = FakeFlushingApiService()
            val vm = crearViewModel(api)

            vm.registrarFlushing()

            assertEquals(0, api.creaciones)
            assertEquals("Selecciona una línea.", vm.uiState.value.errorLinea)
            assertEquals("Ingresa una observación.", vm.uiState.value.errorObservacion)
            assertEquals("Ingresa la temperatura.", vm.uiState.value.errorTemperaturaAntes)
            assertEquals("Ingresa la temperatura.", vm.uiState.value.errorTemperaturaDespues)
            assertEquals(
                "Ingresa una duración entera mayor que cero.",
                vm.uiState.value.errorDuracion
            )
        }

    @Test
    fun registroValidoNormalizaComasYConservaConfirmacionRemota() =
        runTest(mainDispatcherRule.dispatcher) {
            val api = FakeFlushingApiService()
            val vm = crearViewModel(api)
            vm.seleccionarLinea(2)
            vm.actualizarObservacion("  limpieza de prueba  ")
            vm.actualizarTemperaturaAntes(" 25,5 ")
            vm.actualizarTemperaturaDespues("19,5")
            vm.actualizarDuracion("120")

            vm.registrarFlushing()
            advanceUntilIdle()

            assertEquals(1, api.creaciones)
            assertEquals(2L, api.registroRecibido?.lineaId)
            assertEquals("limpieza de prueba", api.registroRecibido?.observacion)
            assertEquals(25.5, api.registroRecibido?.temperaturaAntes ?: 0.0, 0.0)
            assertEquals(19.5, api.registroRecibido?.temperaturaDespues ?: 0.0, 0.0)
            assertEquals(120, api.registroRecibido?.duracionSegundos)
            assertEquals(44L, vm.uiState.value.registroConfirmado?.id)
            assertTrue(vm.uiState.value.guardadoRemotoCompleto)
            assertFalse(vm.uiState.value.guardando)
        }

    @Test
    fun rechazaRespuestaDelServidorQueAsociaOtraLinea() =
        runTest(mainDispatcherRule.dispatcher) {
            val api = FakeFlushingApiService().apply { lineaRespuesta = 99L }
            val vm = crearViewModel(api)
            completarFormulario(vm)

            vm.registrarFlushing()
            advanceUntilIdle()

            assertEquals(1, api.creaciones)
            assertFalse(vm.uiState.value.guardadoRemotoCompleto)
            assertEquals(
                "No pudimos confirmar si el flushing se guardó. " +
                        "Revisa el historial antes de registrar otro.",
                vm.uiState.value.errorGuardado
            )
            assertFalse(vm.uiState.value.guardando)
        }

    @Test
    fun precargaUltimaTemperaturaSinSobrescribirValorDelOperario() =
        runTest(mainDispatcherRule.dispatcher) {
            val api = FakeFlushingApiService()
            val vm = RegistrarFlushingViewModel(
                repository = FlushingRemoteRepository(FlushingRemoteDataSource(api)),
                consultarUltimaTemperatura = { linea -> if (linea == 6) 21.7 else null },
                prepararFoto = { error("Sin foto") }
            )
            vm.seleccionarLinea(6)
            runCurrent()
            assertEquals("21.7", vm.uiState.value.temperaturaAntes)
            vm.actualizarTemperaturaAntes("22,4")
            assertEquals("22,4", vm.uiState.value.temperaturaAntes)
        }

    private fun crearViewModel(api: FakeFlushingApiService) =
        RegistrarFlushingViewModel(
            repository = FlushingRemoteRepository(FlushingRemoteDataSource(api)),
            prepararFoto = { error("La prueba no selecciona una foto") }
        )

    private fun completarFormulario(vm: RegistrarFlushingViewModel) {
        vm.seleccionarLinea(2)
        vm.actualizarObservacion("prueba de flushing")
        vm.actualizarTemperaturaAntes("25")
        vm.actualizarTemperaturaDespues("19")
        vm.actualizarDuracion("120")
    }
}

private class FakeFlushingApiService : FlushingApiService {
    var creaciones = 0
    var registroRecibido: FlushingRemotoDto? = null
    var lineaRespuesta = 2L

    override suspend fun listar(lineaId: Long?): List<FlushingRemotoDto> = emptyList()

    override suspend fun obtener(id: Long): FlushingRemotoDto =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun crear(registro: FlushingRemotoDto): FlushingRemotoDto {
        creaciones++
        registroRecibido = registro
        return registro.copy(id = 44L, lineaId = lineaRespuesta)
    }

    override suspend fun actualizar(
        id: Long,
        registro: FlushingRemotoDto
    ): FlushingRemotoDto = throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun eliminar(id: Long) =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun guardarFoto(
        id: Long,
        foto: MultipartBody.Part
    ): FotoFlushingRemotaDto =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun obtenerFoto(id: Long): ResponseBody =
        throw UnsupportedOperationException("No se usa en esta prueba")
}
