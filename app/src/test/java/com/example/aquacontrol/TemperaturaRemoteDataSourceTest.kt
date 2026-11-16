package com.example.aquacontrol

import com.example.aquacontrol.data.remote.AquaControlApiService
import com.example.aquacontrol.data.remote.GalponRemotoDto
import com.example.aquacontrol.data.remote.GranjaRemotaDto
import com.example.aquacontrol.data.remote.LineaRemotaDto
import com.example.aquacontrol.data.remote.TemperaturaRemotaDto
import com.example.aquacontrol.data.temperatura.remote.TemperaturaRemoteDataSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class TemperaturaRemoteDataSourceTest {
    @Test
    fun listarValidaLineaYDelegaLaConsulta() = runBlocking {
        val api = FakeTemperaturaApi()
        val source = TemperaturaRemoteDataSource(api)
        val dto = temperaturaDto()
        api.respuestaLista = listOf(dto)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { source.listar(0) }
        }
        assertNull(api.lineaSolicitada)

        assertEquals(listOf(dto), source.listar(2))
        assertEquals(2L, api.lineaSolicitada)

        api.respuestaLista = emptyList()
        assertEquals(emptyList<TemperaturaRemotaDto>(), source.listar())
        assertNull(api.lineaSolicitada)
    }

    @Test
    fun crearActualizarYEliminarDeleganYValidanIdServidor() = runBlocking {
        val api = FakeTemperaturaApi()
        val source = TemperaturaRemoteDataSource(api)
        val dto = temperaturaDto()
        api.respuestaTemperatura = dto

        assertEquals(dto, source.crear(dto))
        assertEquals(dto, api.temperaturaEnviada)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { source.actualizar(0, dto) }
        }
        assertEquals(dto, source.actualizar(50, dto))
        assertEquals(50L, api.idActualizado)
        assertEquals(dto, source.crear(dto))

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { source.eliminar(-1) }
        }
        source.eliminar(50)
        assertEquals(50L, api.idEliminado)
    }

    private fun temperaturaDto() = TemperaturaRemotaDto(
        id = 10,
        lineaId = 2,
        temperatura = 22.5,
        fechaHora = "2026-11-15T10:00:00",
        observacion = "prueba"
    )
}

private class FakeTemperaturaApi : AquaControlApiService {
    var lineaSolicitada: Long? = null
    var respuestaLista: List<TemperaturaRemotaDto> = emptyList()
    var respuestaTemperatura: TemperaturaRemotaDto? = null
    var temperaturaEnviada: TemperaturaRemotaDto? = null
    var idActualizado: Long? = null
    var idEliminado: Long? = null

    override suspend fun listarGranjas(): List<GranjaRemotaDto> = unsupported()
    override suspend fun listarGalpones(granjaId: Long): List<GalponRemotoDto> = unsupported()
    override suspend fun listarLineas(galponId: Long): List<LineaRemotaDto> = unsupported()

    override suspend fun listarTemperaturas(lineaId: Long?): List<TemperaturaRemotaDto> {
        lineaSolicitada = lineaId
        return respuestaLista
    }

    override suspend fun obtenerTemperatura(id: Long): TemperaturaRemotaDto = unsupported()

    override suspend fun crearTemperatura(temperatura: TemperaturaRemotaDto): TemperaturaRemotaDto {
        temperaturaEnviada = temperatura
        return respuestaTemperatura ?: temperatura
    }

    override suspend fun actualizarTemperatura(
        id: Long,
        temperatura: TemperaturaRemotaDto
    ): TemperaturaRemotaDto {
        idActualizado = id
        temperaturaEnviada = temperatura
        return respuestaTemperatura ?: temperatura
    }

    override suspend fun eliminarTemperatura(id: Long) {
        idEliminado = id
    }

    private fun <T> unsupported(): T = throw UnsupportedOperationException("No se usa en esta prueba")
}
