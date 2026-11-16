package com.example.aquacontrol

import com.example.aquacontrol.data.flushing.remote.FlushingApiService
import com.example.aquacontrol.data.flushing.remote.FlushingRemoteDataSource
import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto
import com.example.aquacontrol.data.flushing.remote.FotoFlushingRemotaDto
import com.example.aquacontrol.repository.data.FlushingRemoteRepository
import com.example.aquacontrol.viewmodel.flushing.RegistrarFlushingViewModel
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrarFlushingRecoveryTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun reintentoDeFotoNoDuplicaElFlushingYaConfirmado() =
        runTest(mainDispatcherRule.dispatcher) {
            val api = RecoveryFlushingApi().apply { fallarPrimeraFoto = true }
            val parte = MultipartBody.Part.createFormData(
                "foto", "evidencia.jpg", RequestBody.create(okhttp3.MediaType.parse("text/plain"), "jpg")
            )
            val vm = RegistrarFlushingViewModel(
                FlushingRemoteRepository(FlushingRemoteDataSource(api))
            ) { parte }
            llenarFormulario(vm)
            vm.actualizarFoto("C:/tmp/evidencia.jpg")

            vm.registrarFlushing()
            advanceUntilIdle()

            assertEquals(1, api.creaciones)
            assertEquals(1, api.intentosFoto)
            assertEquals(77L, vm.uiState.value.registroConfirmado?.id)
            assertFalse(vm.uiState.value.guardadoRemotoCompleto)
            assertTrue(vm.uiState.value.errorGuardado.orEmpty().contains("ya está guardado"))

            vm.registrarFlushing()
            advanceUntilIdle()

            assertEquals(1, api.creaciones)
            assertEquals(2, api.intentosFoto)
            assertTrue(vm.uiState.value.guardadoRemotoCompleto)
            assertFalse(vm.uiState.value.guardando)
        }

    @Test
    fun errorAlPrepararFotoImpideCrearRegistroEnBackend() =
        runTest(mainDispatcherRule.dispatcher) {
            val api = RecoveryFlushingApi()
            val vm = RegistrarFlushingViewModel(
                FlushingRemoteRepository(FlushingRemoteDataSource(api))
            ) { throw IOException("archivo no disponible") }
            llenarFormulario(vm)
            vm.actualizarFoto("C:/tmp/no-existe.jpg")

            vm.registrarFlushing()
            advanceUntilIdle()

            assertEquals(0, api.creaciones)
            assertFalse(vm.uiState.value.guardadoRemotoCompleto)
            assertFalse(vm.uiState.value.guardando)
            assertNotNull(vm.uiState.value.errorGuardado)
        }

    private fun llenarFormulario(vm: RegistrarFlushingViewModel) {
        vm.seleccionarLinea(2)
        vm.actualizarObservacion("prueba de recuperación")
        vm.actualizarTemperaturaAntes("25")
        vm.actualizarTemperaturaDespues("19")
        vm.actualizarDuracion("120")
    }
}

private class RecoveryFlushingApi : FlushingApiService {
    var creaciones = 0
    var intentosFoto = 0
    var fallarPrimeraFoto = false

    override suspend fun listar(lineaId: Long?): List<FlushingRemotoDto> =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun obtener(id: Long): FlushingRemotoDto =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun crear(registro: FlushingRemotoDto): FlushingRemotoDto {
        creaciones++
        return registro.copy(id = 77L)
    }

    override suspend fun actualizar(id: Long, registro: FlushingRemotoDto): FlushingRemotoDto =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun eliminar(id: Long) =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun guardarFoto(
        id: Long,
        foto: MultipartBody.Part
    ): FotoFlushingRemotaDto {
        intentosFoto++
        if (fallarPrimeraFoto && intentosFoto == 1) throw IOException("fallo temporal")
        return FotoFlushingRemotaDto(id, "image/jpeg", 3)
    }

    override suspend fun obtenerFoto(id: Long): ResponseBody =
        throw UnsupportedOperationException("No se usa en esta prueba")
}
