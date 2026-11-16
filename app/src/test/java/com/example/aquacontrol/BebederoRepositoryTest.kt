package com.example.aquacontrol

import com.example.aquacontrol.repository.bebedero.BebederoRepository
import org.junit.Assert.*
import org.junit.Test

class BebederoRepositoryTest {
    private val repository = BebederoRepository()

    @Test
    fun relacionesDeGranjaGalponYLineaConservanSusIds() {
        val granjas = repository.obtenerGranjas()
        assertEquals(listOf(1, 2), granjas.map { it.id })
        assertEquals(2, granjas.size)

        val galponesNorte = repository.obtenerGalpones(1)
        assertEquals(listOf(1, 2), galponesNorte.map { it.id })
        assertTrue(galponesNorte.all { it.granjaId == 1 })

        val lineasGalponDos = repository.obtenerLineas(2)
        assertEquals(listOf(3), lineasGalponDos.map { it.id })
        assertEquals(2, lineasGalponDos.single().galponId)

        assertTrue(repository.obtenerGalpones(999).isEmpty())
        assertTrue(repository.obtenerLineas(999).isEmpty())
    }

    @Test
    fun historialYFlushingSeFiltranPorLinea() {
        assertEquals(listOf(1, 2), repository.obtenerHistorial(1).map { it.id })
        assertEquals(1, repository.obtenerHistorial(2).size)
        assertTrue(repository.obtenerHistorial(999).isEmpty())

        assertEquals(1, repository.obtenerFlushing(1).size)
        assertEquals(2, repository.obtenerFlushing(2).single().lineaId)
        assertTrue(repository.obtenerFlushing(3).isEmpty())
    }
}
