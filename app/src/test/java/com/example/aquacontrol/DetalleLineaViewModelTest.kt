package com.example.aquacontrol

import com.example.aquacontrol.data.remote.AquaControlApiService
import com.example.aquacontrol.data.remote.GalponRemotoDto
import com.example.aquacontrol.data.remote.GranjaRemotaDto
import com.example.aquacontrol.data.remote.LineaRemotaDto
import com.example.aquacontrol.data.remote.TemperaturaRemotaDto
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaUiState
import com.example.aquacontrol.viewmodel.detalle.DetalleLineaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleLineaViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun rechazaIdInvalidoYLineaInexistente() = runTest(mainDispatcherRule.dispatcher) {
        val vm = DetalleLineaViewModel(
            FakeTemperaturaRepository(),
            apiInyectada = FakeDetalleLineaApi()
        )

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
    fun resumenLinea6IncluyeUbicacionYUltimaMedicion() = runTest(mainDispatcherRule.dispatcher) {
        val anterior = MedicionTemperatura(1, 6, 18.0, 100, OrigenMedicion.MANUAL)
        val reciente = MedicionTemperatura(2, 6, 22.0, 200, OrigenMedicion.SIMULADA)
        val repository = FakeTemperaturaRepository().apply {
            mediciones.value = listOf(anterior, reciente)
        }
        val vm = DetalleLineaViewModel(repository, FakeDetalleLineaApi())

        vm.cargarResumen(6)
        runCurrent()

        val estado = vm.uiState.value as DetalleLineaUiState.Success
        assertEquals("Granja Sur", estado.nombreGranja)
        assertEquals("Galpón 4", estado.nombreGalpon)
        assertEquals("Línea 1", estado.linea.nombre)
        assertEquals(6, estado.linea.id)
        assertEquals(5, estado.linea.galponId)
        assertEquals(reciente, estado.linea.ultimaMedicion)
        assertEquals(listOf(reciente), estado.historial)
    }

    @Test
    fun historialDeLinea3ConservaTodasLasMediciones() = runTest(mainDispatcherRule.dispatcher) {
        val mediciones = listOf(
            MedicionTemperatura(1, 3, 21.0, 100, OrigenMedicion.MANUAL),
            MedicionTemperatura(2, 3, 22.0, 200, OrigenMedicion.SIMULADA)
        )
        val repository = FakeTemperaturaRepository().apply {
            this.mediciones.value = mediciones
        }
        val vm = DetalleLineaViewModel(repository, FakeDetalleLineaApi())

        vm.cargarHistorial(3)
        runCurrent()

        val estado = vm.uiState.value as DetalleLineaUiState.Success
        assertEquals("Granja Norte", estado.nombreGranja)
        assertEquals("Galpón 1", estado.nombreGalpon)
        assertEquals("Línea 2", estado.linea.nombre)
        assertEquals(mediciones, estado.historial)
        assertEquals(mediciones.first(), estado.linea.ultimaMedicion)
    }

    @Test
    fun lineaExistenteSinMedicionesPresentaDetalleVacio() = runTest(mainDispatcherRule.dispatcher) {
        val vm = DetalleLineaViewModel(
            FakeTemperaturaRepository(),
            FakeDetalleLineaApi()
        )
        vm.cargarResumen(6)
        runCurrent()

        val estado = vm.uiState.value as DetalleLineaUiState.Success
        assertEquals(6, estado.linea.id)
        assertNull(estado.linea.ultimaMedicion)
        assertTrue(estado.historial.isEmpty())
    }
}

private class FakeDetalleLineaApi : AquaControlApiService {
    private val granjas = listOf(
        GranjaRemotaDto(1L, "Granja Norte"),
        GranjaRemotaDto(3L, "Granja Sur")
    )
    private val galpones = listOf(
        GalponRemotoDto(1L, "Galpón 1", 1L),
        GalponRemotoDto(5L, "Galpón 4", 3L)
    )
    private val lineas = mapOf(
        3L to LineaRemotaDto(3L, "Línea 2", 1L),
        6L to LineaRemotaDto(6L, "Línea 1", 5L)
    )

    override suspend fun listarGranjas(): List<GranjaRemotaDto> = granjas
    override suspend fun listarGalpones(granjaId: Long): List<GalponRemotoDto> =
        galpones.filter { it.granjaId == granjaId }

    override suspend fun listarLineas(galponId: Long): List<LineaRemotaDto> =
        lineas.values.filter { it.galponId == galponId }

    override suspend fun obtenerLineaPorId(id: Long): LineaRemotaDto =
        lineas[id] ?: throw HttpException(
            Response.error<LineaRemotaDto>(404, ResponseBody.create(null, "No existe"))
        )

    override suspend fun listarTemperaturas(lineaId: Long?): List<TemperaturaRemotaDto> =
        unsupported()
    override suspend fun obtenerTemperatura(id: Long): TemperaturaRemotaDto = unsupported()
    override suspend fun crearTemperatura(
        temperatura: TemperaturaRemotaDto
    ): TemperaturaRemotaDto = unsupported()
    override suspend fun actualizarTemperatura(
        id: Long,
        temperatura: TemperaturaRemotaDto
    ): TemperaturaRemotaDto = unsupported()
    override suspend fun eliminarTemperatura(id: Long) = unsupported<Unit>()

    private fun <T> unsupported(): T =
        throw UnsupportedOperationException("No se usa en esta prueba")
}
