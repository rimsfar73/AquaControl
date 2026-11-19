package com.example.aquacontrol.data.simulacion

import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random

/**
 * Genera temperaturas de demostración, independientes por línea.
 * Un escenario aplicado manualmente permanece fijado hasta liberarlo o reiniciar.
 */
class GeneradorTemperatura(
    private val random: Random = Random.Default
) {
    private data class EstadoSimulado(
        var temperatura: Double,
        var escenario: EscenarioSimulacion,
        var objetivo: Double,
        var lecturasRestantes: Int,
        var objetivoAlcanzado: Boolean = false,
        var controlManual: Boolean = false
    )

    private val estadosPorLinea = mutableMapOf<Int, EstadoSimulado>()

    fun generarTemperatura(lineaId: Int): Double {
        validarLinea(lineaId)

        val existente = estadosPorLinea[lineaId]
        if (existente == null) {
            val inicial = crearEstadoInicial()
            estadosPorLinea[lineaId] = inicial
            return inicial.temperatura
        }

        if (!existente.objetivoAlcanzado) {
            avanzarHaciaObjetivo(existente)
        } else {
            // Una vez alcanzado el objetivo, solo fluctúa levemente.
            // Nunca sale del rango objetivo del escenario fijado manualmente.
            val variacion = existente.objetivo + random.nextDouble(-0.2, 0.2)
            existente.temperatura = redondear(
                if (existente.controlManual) {
                    val rango = rangoDelEscenario(existente.escenario)
                    variacion.coerceIn(rango.start, rango.endInclusive)
                } else {
                    variacion
                }
            )

            if (!existente.controlManual) {
                existente.lecturasRestantes--
                if (existente.lecturasRestantes <= 0) {
                    val siguiente = when (existente.escenario) {
                        EscenarioSimulacion.CRITICO_CALOR,
                        EscenarioSimulacion.CRITICO_FRIO -> EscenarioSimulacion.RECUPERACION
                        else -> elegirEscenarioAleatorio()
                    }
                    configurarEscenario(existente, siguiente, manual = false)
                }
            }
        }

        return existente.temperatura
    }

    /** Fija un escenario en una línea sin modificar las demás. */
    fun seleccionarEscenario(lineaId: Int, escenario: EscenarioSimulacion) {
        validarLinea(lineaId)
        val estado = estadosPorLinea.getOrPut(lineaId) { crearEstadoInicial() }
        configurarEscenario(estado, escenario, manual = true)
    }

    /** Devuelve la línea al comportamiento automático, sin saltos instantáneos. */
    fun liberarEscenario(lineaId: Int) {
        validarLinea(lineaId)
        val estado = estadosPorLinea[lineaId] ?: return
        configurarEscenario(estado, elegirEscenarioAleatorio(), manual = false)
    }

    fun escenarioManual(lineaId: Int): EscenarioSimulacion? {
        validarLinea(lineaId)
        return estadosPorLinea[lineaId]
            ?.takeIf { it.controlManual }
            ?.escenario
    }

    fun objetivoDeLinea(lineaId: Int): Double? {
        validarLinea(lineaId)
        return estadosPorLinea[lineaId]?.objetivo
    }

    fun reiniciar() {
        estadosPorLinea.clear()
    }

    private fun crearEstadoInicial(): EstadoSimulado {
        val escenario = elegirEscenarioAleatorio()
        return EstadoSimulado(
            temperatura = redondear(random.nextDouble(18.5, 20.5)),
            escenario = escenario,
            objetivo = generarObjetivo(escenario),
            lecturasRestantes = random.nextInt(3, 8)
        )
    }

    private fun avanzarHaciaObjetivo(estado: EstadoSimulado) {
        val diferencia = estado.objetivo - estado.temperatura
        // Los escenarios manuales convergen antes, pero de forma gradual.
        val paso = if (estado.controlManual) {
            random.nextDouble(1.5, 3.0)
        } else {
            random.nextDouble(0.3, 1.2)
        }

        if (abs(diferencia) <= paso) {
            estado.temperatura = estado.objetivo
            estado.objetivoAlcanzado = true
        } else {
            estado.temperatura = redondear(
                estado.temperatura + if (diferencia > 0) paso else -paso
            )
        }
    }

    private fun configurarEscenario(
        estado: EstadoSimulado,
        escenario: EscenarioSimulacion,
        manual: Boolean
    ) {
        estado.escenario = escenario
        estado.objetivo = generarObjetivo(escenario)
        estado.lecturasRestantes = random.nextInt(3, 8)
        estado.objetivoAlcanzado = false
        estado.controlManual = manual
    }

    private fun elegirEscenarioAleatorio(): EscenarioSimulacion =
        when (random.nextInt(100)) {
            in 0..39 -> EscenarioSimulacion.ESTABILIDAD
            in 40..59 -> EscenarioSimulacion.CALENTAMIENTO
            in 60..74 -> EscenarioSimulacion.ENFRIAMIENTO
            in 75..89 -> EscenarioSimulacion.CRITICO_CALOR
            else -> EscenarioSimulacion.CRITICO_FRIO
        }

    private fun generarObjetivo(escenario: EscenarioSimulacion): Double {
        val rango = rangoDelEscenario(escenario)
        return redondear(random.nextDouble(rango.start, rango.endInclusive))
    }

    private fun rangoDelEscenario(escenario: EscenarioSimulacion): ClosedFloatingPointRange<Double> =
        when (escenario) {
            EscenarioSimulacion.ESTABILIDAD,
            EscenarioSimulacion.RECUPERACION -> 18.5..20.5
            EscenarioSimulacion.CALENTAMIENTO -> 23.0..28.0
            EscenarioSimulacion.ENFRIAMIENTO -> 8.0..16.0
            EscenarioSimulacion.CRITICO_CALOR -> 31.0..35.0
            EscenarioSimulacion.CRITICO_FRIO -> 1.0..3.5
        }

    private fun validarLinea(lineaId: Int) {
        require(lineaId > 0) { "La línea debe tener un identificador válido." }
    }

    private fun redondear(valor: Double): Double = round(valor * 10.0) / 10.0
}
