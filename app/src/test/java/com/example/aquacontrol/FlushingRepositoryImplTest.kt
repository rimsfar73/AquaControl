package com.example.aquacontrol

import com.example.aquacontrol.data.flushing.local.FlushingDao
import com.example.aquacontrol.data.flushing.local.FlushingEntity
import com.example.aquacontrol.data.flushing.local.FlushingLocalDataSource
import com.example.aquacontrol.model.flushing.FlushingDTO
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.repository.flushing.FlushingRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FlushingRepositoryImplTest {
    @Test
    fun registrarValidaNormalizaObservacionYConvierteEntidad() = runBlocking {
        val dao = InMemoryFlushingDao()
        val repository = FlushingRepositoryImpl(FlushingLocalDataSource(dao))
        val dto = dto().copy(observacion = "  lavado completo  ", fotoRuta = " ")

        val evento = repository.registrarFlushing(dto)

        assertEquals(1, evento.id)
        assertEquals(2, evento.lineaId)
        assertEquals("lavado completo", evento.observacion)
        assertNull(evento.fotoRuta)
        assertEquals(dto.fechaHora, evento.fechaHora)
    }

    @Test
    fun registrarRechazaDatosInvalidosAntesDeInsertar() = runBlocking {
        val dao = InMemoryFlushingDao()
        val repository = FlushingRepositoryImpl(FlushingLocalDataSource(dao))

        val inválidos = listOf(
            dto().copy(duracionSegundos = null),
            dto().copy(duracionSegundos = 0),
            dto().copy(lineaId = 0),
            dto().copy(observacion = "  "),
            dto().copy(temperaturaAntes = Double.NaN)
        )
        inválidos.forEach { inválido ->
            try {
                repository.registrarFlushing(inválido)
                fail("El registro inválido debe rechazarse")
            } catch (_: IllegalArgumentException) {
                // El DAO no debe recibir ninguno de estos registros.
            }
        }
        assertTrue(dao.filas.isEmpty())
    }

    @Test
    fun editarOperarioConservaFechaEvidenciaEIdentidad() = runBlocking {
        val dao = InMemoryFlushingDao().apply { guardarEvento(eventoExistente()) }
        val repository = FlushingRepositoryImpl(FlushingLocalDataSource(dao))

        try {
            repository.actualizarFlushing(2, 8, "x", 26.0, 19.0, 90, PerfilUsuario.SUPERVISOR)
            fail("Supervisor no debe editar el registro")
        } catch (_: IllegalArgumentException) {
            // Se valida el rol antes de delegar la actualización.
        }

        val editado = repository.actualizarFlushing(
            lineaId = 2,
            eventoId = 8,
            observacion = "  corregido  ",
            temperaturaAntes = 26.0,
            temperaturaDespues = 19.0,
            duracionSegundos = 90,
            perfil = PerfilUsuario.OPERARIO
        )
        assertEquals(8, editado.id)
        assertEquals("2026-11-15T10:00:00", editado.fechaHora)
        assertEquals("foto.jpg", editado.fotoRuta)
        assertEquals("corregido", editado.observacion)
        assertEquals(26.0, editado.temperaturaAntes, 0.0)
    }

    @Test
    fun eliminarExigeSupervisorYDevuelveEventoAntesDeBorrarlo() = runBlocking {
        val dao = InMemoryFlushingDao().apply { guardarEvento(eventoExistente()) }
        val repository = FlushingRepositoryImpl(FlushingLocalDataSource(dao))

        try {
            repository.eliminarFlushing(2, 8, PerfilUsuario.OPERARIO)
            fail("Operario no debe eliminar flushing")
        } catch (_: IllegalArgumentException) {
            assertEquals(1, dao.filas.size)
        }

        val eliminado = repository.eliminarFlushing(2, 8, PerfilUsuario.SUPERVISOR)
        assertEquals(8, eliminado.id)
        assertEquals("foto.jpg", eliminado.fotoRuta)
        assertTrue(dao.filas.isEmpty())
    }

    private fun dto() = FlushingDTO(
        fechaHora = "2026-11-15T10:00:00",
        lineaId = 2,
        observacion = "prueba",
        temperaturaAntes = 25.0,
        temperaturaDespues = 19.0,
        duracionSegundos = 120
    )

    private fun eventoExistente() = FlushingEntity(
        id = 8,
        lineaId = 2,
        fechaHora = "2026-11-15T10:00:00",
        observacion = "original",
        temperaturaAntes = 25.0,
        temperaturaDespues = 19.0,
        duracionSegundos = 120,
        fotoRuta = "foto.jpg"
    )
}

private class InMemoryFlushingDao : FlushingDao {
    val filas = linkedMapOf<Int, FlushingEntity>()
    private var siguienteId = 1

    fun guardarEvento(evento: FlushingEntity) {
        filas[evento.id] = evento
        siguienteId = maxOf(siguienteId, evento.id + 1)
    }

    override suspend fun insertarFlushing(evento: FlushingEntity): Long {
        val id = if (evento.id > 0) evento.id else siguienteId++
        filas[id] = evento.copy(id = id)
        return id.toLong()
    }

    override suspend fun obtenerFlushingPorLinea(lineaId: Int) =
        filas.values.filter { it.lineaId == lineaId }

    override suspend fun obtenerEvento(lineaId: Int, eventoId: Int) =
        filas[eventoId]?.takeIf { it.lineaId == lineaId }

    override suspend fun actualizarFlushing(
        lineaId: Int,
        eventoId: Int,
        observacion: String,
        temperaturaAntes: Double,
        temperaturaDespues: Double,
        duracionSegundos: Int
    ): Int {
        val actual = obtenerEvento(lineaId, eventoId) ?: return 0
        filas[eventoId] = actual.copy(
            observacion = observacion,
            temperaturaAntes = temperaturaAntes,
            temperaturaDespues = temperaturaDespues,
            duracionSegundos = duracionSegundos
        )
        return 1
    }

    override suspend fun eliminarFlushing(lineaId: Int, eventoId: Int): Int {
        val actual = obtenerEvento(lineaId, eventoId) ?: return 0
        filas.remove(actual.id)
        return 1
    }
}
