package com.example.aquacontrol

import com.example.aquacontrol.data.flushing.local.FlushingRemotoDao
import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity
import com.example.aquacontrol.data.flushing.remote.FlushingApiService
import com.example.aquacontrol.data.flushing.remote.FlushingRemoteDataSource
import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto
import com.example.aquacontrol.data.flushing.remote.FotoFlushingRemotaDto
import com.example.aquacontrol.repository.data.FlushingConsultaRepository
import com.example.aquacontrol.repository.data.FlushingRemoteRepository
import java.io.IOException
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import org.junit.Assert.*
import org.junit.Test

class FlushingConsultaRepositoryTest {
    @Test
    fun sincronizacionRemotaReemplazaLineaYConservaRutaFotoAnterior() = runBlocking {
        val servidor = "http://localhost:8080/"
        val dao = InMemoryFlushingRemotoDao()
        dao.guardar(registroLocal(servidor, id = 9, foto = "foto-descargada.jpg"))
        val api = FakeConsultaFlushingApi().apply {
            respuestaLista = listOf(dto(id = 9, lineaId = 2, observacion = "actualizado"))
        }
        val repository = crearRepository(dao, api, servidor)

        val resultado = repository.obtenerPorLinea(2)

        assertFalse(resultado.desdeCopiaLocal)
        assertEquals(1, resultado.registros.size)
        assertEquals("actualizado", resultado.registros.single().observacion)
        assertEquals("foto-descargada.jpg", resultado.registros.single().fotoRuta)
        assertEquals(2L, api.lineaConsultada)
    }

    @Test
    fun errorDeRedPermiteUsarLaCopiaDeLaLinea() = runBlocking {
        val servidor = "http://localhost:8080/"
        val dao = InMemoryFlushingRemotoDao()
        val local = registroLocal(servidor, id = 12, foto = "foto-local.jpg")
        dao.guardar(local)
        val api = FakeConsultaFlushingApi().apply {
            errorLista = IOException("sin conexión")
        }
        val repository = crearRepository(dao, api, servidor)

        val resultado = repository.obtenerPorLinea(2)

        assertTrue(resultado.desdeCopiaLocal)
        assertEquals(listOf(local), resultado.registros)
    }

    @Test
    fun detalleOfflineSoloUsaCopiaDelMismoRegistroYLinea() = runBlocking {
        val servidor = "http://localhost:8080/"
        val dao = InMemoryFlushingRemotoDao()
        val local = registroLocal(servidor, id = 15)
        dao.guardar(local)
        val api = FakeConsultaFlushingApi().apply {
            errorDetalle = IOException("sin conexión")
        }
        val repository = crearRepository(dao, api, servidor)

        val encontrado = repository.obtenerDetalle(lineaId = 2, id = 15)
        assertTrue(encontrado.desdeCopiaLocal)
        assertEquals(local, encontrado.registro)

        try {
            repository.obtenerDetalle(lineaId = 3, id = 15)
            fail("No debe servir una copia de otra línea")
        } catch (expected: IOException) {
            assertTrue(expected.message.orEmpty().contains("no tiene"))
        }
    }

    @Test
    fun validacionesEvitanConsultaDeLineaInvalidaYServidorVacio() = runBlocking {
        try {
            FlushingConsultaRepository(
                dao = InMemoryFlushingRemotoDao(),
                remoto = FlushingRemoteRepository(FlushingRemoteDataSource(FakeConsultaFlushingApi())),
                servidor = "  "
            )
            fail("Debe rechazar un servidor vacío")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("servidor"))
        }

        val repository = crearRepository(
            InMemoryFlushingRemotoDao(),
            FakeConsultaFlushingApi(),
            "http://localhost:8080/"
        )
        try {
            repository.obtenerPorLinea(0)
            fail("Debe rechazar una línea inválida")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("línea"))
        }
    }

    private fun crearRepository(
        dao: InMemoryFlushingRemotoDao,
        api: FakeConsultaFlushingApi,
        servidor: String
    ) = FlushingConsultaRepository(
        dao = dao,
        remoto = FlushingRemoteRepository(FlushingRemoteDataSource(api)),
        servidor = servidor
    )

    private fun dto(id: Long, lineaId: Long, observacion: String = "prueba") =
        FlushingRemotoDto(
            id = id,
            lineaId = lineaId,
            fechaHora = "2026-11-15T10:00:00",
            duracionSegundos = 120,
            observacion = observacion,
            temperaturaAntes = 25.0,
            temperaturaDespues = 19.0
        )

    private fun registroLocal(
        servidor: String,
        id: Long,
        foto: String? = null
    ) = FlushingRemotoEntity(
        servidor = servidor,
        id = id,
        lineaId = 2,
        fechaHora = "2026-11-15T10:00:00",
        duracionSegundos = 120,
        observacion = "local",
        temperaturaAntes = 25.0,
        temperaturaDespues = 19.0,
        fotoRuta = foto,
        descargadoEn = 1L
    )
}

private class InMemoryFlushingRemotoDao : FlushingRemotoDao() {
    private val registros = linkedMapOf<Pair<String, Long>, FlushingRemotoEntity>()

    override suspend fun obtenerPorLinea(
        servidor: String,
        lineaId: Long
    ) = registros.values.filter { it.servidor == servidor && it.lineaId == lineaId }

    override suspend fun obtener(servidor: String, id: Long) = registros[servidor to id]

    override suspend fun guardar(registro: FlushingRemotoEntity) {
        registros[registro.servidor to registro.id] = registro
    }

    override suspend fun guardarTodos(registros: List<FlushingRemotoEntity>) {
        registros.forEach { guardar(it) }
    }

    override suspend fun eliminar(servidor: String, id: Long) {
        registros.remove(servidor to id)
    }

    override suspend fun eliminarPorLinea(servidor: String, lineaId: Long) {
        registros.keys.removeAll { key ->
            registros[key]?.let { it.servidor == servidor && it.lineaId == lineaId } == true
        }
    }

    override suspend fun actualizarFoto(servidor: String, id: Long, fotoRuta: String?) {
        registros[servidor to id]?.let { registros[servidor to id] = it.copy(fotoRuta = fotoRuta) }
    }
}

private class FakeConsultaFlushingApi : FlushingApiService {
    var respuestaLista: List<FlushingRemotoDto> = emptyList()
    var errorLista: Throwable? = null
    var errorDetalle: Throwable? = null
    var lineaConsultada: Long? = null

    override suspend fun listar(lineaId: Long?): List<FlushingRemotoDto> {
        lineaConsultada = lineaId
        errorLista?.let { throw it }
        return respuestaLista
    }

    override suspend fun obtener(id: Long): FlushingRemotoDto {
        errorDetalle?.let { throw it }
        return respuestaLista.first { it.id == id }
    }

    override suspend fun crear(registro: FlushingRemotoDto) =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun actualizar(id: Long, registro: FlushingRemotoDto) =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun eliminar(id: Long) =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun guardarFoto(id: Long, foto: MultipartBody.Part) =
        throw UnsupportedOperationException("No se usa en esta prueba")

    override suspend fun obtenerFoto(id: Long): ResponseBody =
        throw UnsupportedOperationException("No se usa en esta prueba")
}
