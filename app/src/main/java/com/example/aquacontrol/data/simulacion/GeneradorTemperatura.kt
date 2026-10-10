package com.example.aquacontrol.data.simulacion

import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random

class GeneradorTemperatura(
    private val random: Random = Random.Default
) {

    private data class EstadoSimulado(
        var temperatura: Double,
        var etapa: Int = 0,
        var lecturasEnObjetivo: Int = 0,
        var permanencia: Int
    )

    // Escenarios de demostración:
    // normal, calentamiento, crítico por calor, recuperación,
    // enfriamiento, crítico por frío y recuperación.
    private val objetivos = listOf(
        20.0,
        26.0,
        33.0,
        20.0,
        12.0,
        3.0,
        20.0
    )

    private val estadosPorLinea = mutableMapOf<Int, EstadoSimulado>()

    fun generarTemperatura(lineaId: Int): Double {
        require(lineaId > 0) {
            "La línea debe tener un identificador válido."
        }

        val estadoExistente = estadosPorLinea[lineaId]

        if (estadoExistente == null) {
            val temperaturaInicial = redondear(
                random.nextDouble(18.0, 21.0)
            )

            estadosPorLinea[lineaId] = EstadoSimulado(
                temperatura = temperaturaInicial,
                permanencia = random.nextInt(2, 5)
            )

            return temperaturaInicial
        }

        val estado = estadoExistente
        val objetivo = objetivos[estado.etapa]
        val diferencia = objetivo - estado.temperatura
        val paso = random.nextDouble(0.8, 1.8)

        val nuevaTemperatura = when {
            abs(diferencia) <= paso -> objetivo
            diferencia > 0 -> estado.temperatura + paso
            else -> estado.temperatura - paso
        }

        estado.temperatura = redondear(nuevaTemperatura)

        if (abs(estado.temperatura - objetivo) < 0.05) {
            estado.lecturasEnObjetivo++

            if (estado.lecturasEnObjetivo >= estado.permanencia) {
                estado.etapa = (estado.etapa + 1) % objetivos.size
                estado.lecturasEnObjetivo = 0
                estado.permanencia = random.nextInt(2, 5)
            }
        } else {
            estado.lecturasEnObjetivo = 0
        }

        return estado.temperatura
    }

    fun reiniciar() {
        estadosPorLinea.clear()
    }

    private fun redondear(valor: Double): Double {
        return round(valor * 10.0) / 10.0
    }
}