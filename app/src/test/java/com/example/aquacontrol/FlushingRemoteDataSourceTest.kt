package com.example.aquacontrol

import com.example.aquacontrol.data.flushing.remote.FlushingApiService
import com.example.aquacontrol.data.flushing.remote.FlushingRemoteDataSource
import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto
import com.example.aquacontrol.data.flushing.remote.FotoFlushingRemotaDto
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Test

class FlushingRemoteDataSourceTest {
    @Test
    fun delegaConsultasRegistroEdicionEliminacionYFotos() = runBlocking {
        val api = FakeFlushingRemoteApi()
        val source = FlushingRemoteDataSource(api)
        val dto = FlushingRemotoDto(
            id = 9,
            lineaId = 3,
            fechaHora = "2026-11-15T10:00:00",
            duracionSegundos = 90,
            observacion = "prueba",
            temperaturaAntes = 25.0,
            temperaturaDespues = 19.0
        )
        api.dto = dto

        assertEquals(listOf(dto), source.listarPorLinea(3))
        assertEquals(3L, api.lineaConsultada)
        assertEquals(dto, source.obtener(9))
        assertEquals(9L, api.idConsultado)
        assertEquals(dto, source.crear(dto))
        assertEquals(dto, api.dtoEnviado)
        assertEquals(dto, source.actualizar(9, dto))
        assertEquals(9L, api.idActualizado)
        source.eliminar(9)
        assertEquals(9L, api.idEliminado)

        val parteFoto = MultipartBody.Part.createFormData("foto", "evidencia.jpg", RequestBody.create(MediaType.parse("text/plain"), "jpg"))
        val confirmacion = FotoFlushingRemotaDto(9, "image/jpeg", 3)
        api.confirmacionFoto = confirmacion
        assertEquals(confirmacion, source.guardarFoto(9, parteFoto))
        assertEquals(9L, api.idFoto)
        assertSame(parteFoto, api.parteFoto)

        val bytes = byteArrayOf(1, 2, 3)
        api.bytesFoto = bytes
        assertArrayEquals(bytes, source.obtenerFoto(9))
        assertEquals(9L, api.idFotoSolicitada)
    }
}

private class FakeFlushingRemoteApi : FlushingApiService {
    var dto = FlushingRemotoDto(1, 1, "2026-11-15T10:00:00", 60)
    var dtoEnviado: FlushingRemotoDto? = null
    var confirmacionFoto = FotoFlushingRemotaDto(1, "image/jpeg", 1)
    var lineaConsultada: Long? = null
    var idConsultado: Long? = null
    var idActualizado: Long? = null
    var idEliminado: Long? = null
    var idFoto: Long? = null
    var idFotoSolicitada: Long? = null
    var parteFoto: MultipartBody.Part? = null
    var bytesFoto: ByteArray = byteArrayOf()

    override suspend fun listar(lineaId: Long?): List<FlushingRemotoDto> {
        lineaConsultada = lineaId
        return listOf(dto)
    }

    override suspend fun obtener(id: Long): FlushingRemotoDto {
        idConsultado = id
        return dto
    }

    override suspend fun crear(registro: FlushingRemotoDto): FlushingRemotoDto {
        dtoEnviado = registro
        return dto
    }

    override suspend fun actualizar(id: Long, registro: FlushingRemotoDto): FlushingRemotoDto {
        idActualizado = id
        dtoEnviado = registro
        return dto
    }

    override suspend fun eliminar(id: Long) {
        idEliminado = id
    }

    override suspend fun guardarFoto(
        id: Long,
        foto: MultipartBody.Part
    ): FotoFlushingRemotaDto {
        idFoto = id
        parteFoto = foto
        return confirmacionFoto
    }

    override suspend fun obtenerFoto(id: Long): ResponseBody {
        idFotoSolicitada = id
        return ResponseBody.create(MediaType.parse("image/jpeg"), bytesFoto)
    }
}
