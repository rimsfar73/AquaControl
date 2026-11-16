package com.example.aquacontrol

import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaDao
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaEntity
import com.example.aquacontrol.data.temperatura.local.TemperaturaLocalDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class TemperaturaLocalDataSourceTest {
    @Test
    fun guardarRemotaInsertaYActualizaConservandoIdLocal() = runBlocking {
        val dao = InMemoryMedicionTemperaturaDao()
        val source = TemperaturaLocalDataSource(dao)

        val primero = source.guardarMedicionRemota(entidad(idServidor = 40, temperatura = 20.0))
        assertEquals(1L, primero)

        val actualizado = source.guardarMedicionRemota(
            entidad(idServidor = 40, temperatura = 22.5, id = 100)
        )

        assertEquals(primero, actualizado)
        assertEquals(1, dao.filas.size)
        assertEquals(primero, dao.filas.single().id)
        assertEquals(22.5, dao.filas.single().temperatura, 0.0)
    }

    @Test
    fun sincronizarLineaRetiraRemotasObsoletasSinBorrarSimuladas() = runBlocking {
        val dao = InMemoryMedicionTemperaturaDao()
        dao.insertarMedicion(entidad(id = 1, lineaId = 1, idServidor = null, origen = "SIMULADA"))
        dao.insertarMedicion(entidad(id = 2, lineaId = 1, idServidor = 70))
        dao.insertarMedicion(entidad(id = 3, lineaId = 2, idServidor = 80))
        val source = TemperaturaLocalDataSource(dao)

        source.sincronizarMedicionesRemotasPorLinea(
            lineaId = 1,
            mediciones = listOf(entidad(lineaId = 1, idServidor = 71, temperatura = 23.0))
        )

        assertTrue(dao.filas.any { it.idServidor == null && it.origen == "SIMULADA" })
        assertFalse(dao.filas.any { it.idServidor == 70L })
        assertTrue(dao.filas.any { it.idServidor == 71L && it.lineaId == 1 })
        assertTrue(dao.filas.any { it.idServidor == 80L && it.lineaId == 2 })
    }

    @Test
    fun rechazaIdentificadoresDuplicadosYLineasIncorrectasAntesDeSincronizar() = runBlocking {
        val dao = InMemoryMedicionTemperaturaDao()
        val source = TemperaturaLocalDataSource(dao)
        val repetida = listOf(
            entidad(lineaId = 1, idServidor = 9),
            entidad(lineaId = 1, idServidor = 9)
        )

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { source.sincronizarMedicionesRemotasPorLinea(1, repetida) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                source.sincronizarMedicionesRemotasPorLinea(
                    1,
                    listOf(entidad(lineaId = 2, idServidor = 10))
                )
            }
        }
        assertTrue(dao.filas.isEmpty())
    }

    @Test
    fun validaRangoYPaginacionAntesDeConsultarDao() {
        val dao = InMemoryMedicionTemperaturaDao()
        val source = TemperaturaLocalDataSource(dao)

        assertThrows(IllegalArgumentException::class.java) {
            source.observarPaginaHistorial(0, com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura(), 10, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            source.observarPaginaHistorial(1, com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura(), 101, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            source.observarMedicionesEnIntervalo(1, 20, 10)
        }
    }

    private fun entidad(
        id: Long = 0,
        lineaId: Int = 1,
        idServidor: Long? = 1,
        temperatura: Double = 21.0,
        origen: String = "MANUAL"
    ) = MedicionTemperaturaEntity(
        id = id,
        lineaId = lineaId,
        temperatura = temperatura,
        fechaHora = 1_000L,
        origen = origen,
        idServidor = idServidor
    )
}

private class InMemoryMedicionTemperaturaDao : MedicionTemperaturaDao {
    private val state = MutableStateFlow<List<MedicionTemperaturaEntity>>(emptyList())
    val filas get() = state.value
    private var siguienteId = 1L

    override suspend fun insertarMedicion(medicion: MedicionTemperaturaEntity): Long {
        val id = if (medicion.id > 0) medicion.id else siguienteId++
        siguienteId = maxOf(siguienteId, id + 1)
        state.value = state.value + medicion.copy(id = id)
        return id
    }

    override suspend fun actualizarMedicion(medicion: MedicionTemperaturaEntity): Int {
        val existe = state.value.any { it.id == medicion.id }
        if (existe) state.value = state.value.map { if (it.id == medicion.id) medicion else it }
        return if (existe) 1 else 0
    }

    override suspend fun obtenerPorIdServidor(idServidor: Long) =
        state.value.firstOrNull { it.idServidor == idServidor }

    override suspend fun obtenerMedicionesRemotas() = state.value.filter { it.idServidor != null }

    override suspend fun eliminarPorIdLocal(idLocal: Long): Int {
        val antes = state.value.size
        state.value = state.value.filterNot { it.id == idLocal }
        return antes - state.value.size
    }

    override fun observarHistorialPorLinea(lineaId: Int): Flow<List<MedicionTemperaturaEntity>> =
        state.map { filas -> filas.filter { it.lineaId == lineaId } }

    override fun observarPaginaHistorial(
        lineaId: Int,
        estado: String?,
        desdeInclusive: Long?,
        hastaExclusivo: Long?,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperaturaEntity>> = state.map { it.filter { fila -> fila.lineaId == lineaId } }

    override fun observarCantidadHistorial(
        lineaId: Int,
        estado: String?,
        desdeInclusive: Long?,
        hastaExclusivo: Long?
    ): Flow<Int> = state.map { filas -> filas.count { it.lineaId == lineaId } }

    override fun observarUltimaMedicionPorLinea(lineaId: Int): Flow<MedicionTemperaturaEntity?> =
        state.map { filas -> filas.filter { it.lineaId == lineaId }.maxByOrNull { it.fechaHora } }

    override fun observarUltimasMediciones(): Flow<List<MedicionTemperaturaEntity>> = state

    override suspend fun obtenerMedicion(lineaId: Int, medicionId: Long) =
        state.value.firstOrNull { it.lineaId == lineaId && it.id == medicionId }

    override suspend fun actualizarTemperatura(lineaId: Int, medicionId: Long, temperatura: Double): Int {
        val actual = obtenerMedicion(lineaId, medicionId) ?: return 0
        actualizarMedicion(actual.copy(temperatura = temperatura))
        return 1
    }

    override suspend fun eliminarMedicion(lineaId: Int, medicionId: Long): Int {
        val antes = state.value.size
        state.value = state.value.filterNot { it.lineaId == lineaId && it.id == medicionId }
        return antes - state.value.size
    }

    override fun observarMedicionesEnIntervalo(
        lineaId: Int,
        desdeInclusive: Long,
        hastaInclusive: Long
    ): Flow<List<MedicionTemperaturaEntity>> = state.map {
        it.filter { fila -> fila.lineaId == lineaId && fila.fechaHora in desdeInclusive..hastaInclusive }
    }
}
