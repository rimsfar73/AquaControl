package com.example.aquacontrol

import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.linea.LineaBebedero
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun temperaturasNormales() {
        listOf(18.0, 18.5, 21.0).forEach { temperatura ->
            assertEquals(
                "Temperatura: $temperatura",
                EstadoLinea.NORMAL,
                EstadoLinea.desdeTemperatura(temperatura)
            )
        }
    }

    @Test
    fun temperaturasEnAdvertencia() {
        listOf(5.0, 17.99, 21.01, 28.7, 29.99).forEach { temperatura ->
            assertEquals(
                "Temperatura: $temperatura",
                EstadoLinea.ADVERTENCIA,
                EstadoLinea.desdeTemperatura(temperatura)
            )
        }
    }

    @Test
    fun temperaturasCriticas() {
        listOf(4.99, 30.0, 32.0).forEach { temperatura ->
            assertEquals(
                "Temperatura: $temperatura",
                EstadoLinea.CRITICO,
                EstadoLinea.desdeTemperatura(temperatura)
            )
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaNaN() {
        EstadoLinea.desdeTemperatura(Double.NaN)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaInfinitoPositivo() {
        EstadoLinea.desdeTemperatura(Double.POSITIVE_INFINITY)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaInfinitoNegativo() {
        EstadoLinea.desdeTemperatura(Double.NEGATIVE_INFINITY)
    }

    @Test
    fun lineaCalculaSuEstadoYLoActualizaAlCopiar() {
        val linea = LineaBebedero(
            id = 1,
            nombre = "Línea de prueba",
            temperatura = 18.5,
            actualizado = "2026-10-07 14:00",
            galponId = 1
        )

        assertEquals(EstadoLinea.NORMAL, linea.estado)

        val lineaActualizada = linea.copy(temperatura = 32.0)

        assertEquals(EstadoLinea.CRITICO, lineaActualizada.estado)
        assertEquals(EstadoLinea.NORMAL, linea.estado)
    }
}