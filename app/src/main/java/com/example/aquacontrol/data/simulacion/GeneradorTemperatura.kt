package com.example.aquacontrol.data.simulacion

import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
import kotlin.math.abs
import kotlin.math.round
import kotlin.random.Random

class GeneradorTemperatura(
    private val random: Random = Random.Default
) {

    private data class EstadoSimulado(
        var temperatura: Double,
        var escenario: EscenarioSimulacion,
        var objetivo: Double,
        var lecturasRestantes: Int,
        var objetivoAlcanzado: Boolean = false
    )

    private val estadosPorLinea = mutableMapOf<Int, EstadoSimulado>()

    fun generarTemperatura(lineaId: Int): Double {
        validarLinea(lineaId)

        val estadoExistente = estadosPorLinea[lineaId]

        if (estadoExistente == null) {
            val nuevoEstado = crearEstadoInicial()
            estadosPorLinea[lineaId] = nuevoEstado

            return nuevoEstado.temperatura
        }

        val estado = estadoExistente

        if (!estado.objetivoAlcanzado) {
            avanzarHaciaObjetivo(estado)
        } else {
            // Pequeñas variaciones mientras permanece en el episodio.
            estado.temperatura = redondear(
                estado.objetivo + random.nextDouble(-0.2, 0.2)
            )

            estado.lecturasRestantes--

            if (estado.lecturasRestantes <= 0) {
                val siguienteEscenario = when (estado.escenario) {
                    EscenarioSimulacion.CRITICO_CALOR,
                    EscenarioSimulacion.CRITICO_FRIO ->
                        EscenarioSimulacion.RECUPERACION

                    else -> elegirEscenarioAleatorio()
                }

                configurarEscenario(
                    estado = estado,
                    escenario = siguienteEscenario
                )
            }
        }

        return estado.temperatura
    }

    fun seleccionarEscenario(
        lineaId: Int,
        escenario: EscenarioSimulacion
    ) {
        validarLinea(lineaId)

        val estado = estadosPorLinea.getOrPut(lineaId) {
            crearEstadoInicial()
        }

        configurarEscenario(
            estado = estado,
            escenario = escenario
        )
    }

    fun reiniciar() {
        estadosPorLinea.clear()
    }

    private fun crearEstadoInicial(): EstadoSimulado {
        val temperaturaInicial = redondear(
            random.nextDouble(18.5, 20.5)
        )

        val escenario = elegirEscenarioAleatorio()

        return EstadoSimulado(
            temperatura = temperaturaInicial,
            escenario = escenario,
            objetivo = generarObjetivo(escenario),
            lecturasRestantes = random.nextInt(3, 8)
        )
    }

    private fun avanzarHaciaObjetivo(estado: EstadoSimulado) {
        val diferencia = estado.objetivo - estado.temperatura
        val paso = random.nextDouble(0.3, 1.2)

        if (abs(diferencia) <= paso) {
            estado.temperatura = estado.objetivo
            estado.objetivoAlcanzado = true
            return
        }

        estado.temperatura = redondear(
            if (diferencia > 0) {
                estado.temperatura + paso
            } else {
                estado.temperatura - paso
            }
        )
    }

    private fun configurarEscenario(
        estado: EstadoSimulado,
        escenario: EscenarioSimulacion
    ) {
        estado.escenario = escenario
        estado.objetivo = generarObjetivo(escenario)
        estado.lecturasRestantes = random.nextInt(3, 8)
        estado.objetivoAlcanzado = false
    }

    private fun elegirEscenarioAleatorio(): EscenarioSimulacion {
        // Distribución para demostración, no probabilidades
        // medidas en una instalación real.
        return when (random.nextInt(100)) {
            in 0..39 -> EscenarioSimulacion.ESTABILIDAD
            in 40..59 -> EscenarioSimulacion.CALENTAMIENTO
            in 60..74 -> EscenarioSimulacion.ENFRIAMIENTO
            in 75..89 -> EscenarioSimulacion.CRITICO_CALOR
            else -> EscenarioSimulacion.CRITICO_FRIO
        }
    }

    private fun generarObjetivo(
        escenario: EscenarioSimulacion
    ): Double {
        val objetivo = when (escenario) {
            EscenarioSimulacion.ESTABILIDAD,
            EscenarioSimulacion.RECUPERACION ->
                random.nextDouble(18.5, 20.5)

            EscenarioSimulacion.CALENTAMIENTO ->
                random.nextDouble(23.0, 28.0)

            EscenarioSimulacion.ENFRIAMIENTO ->
                random.nextDouble(8.0, 16.0)

            EscenarioSimulacion.CRITICO_CALOR ->
                random.nextDouble(31.0, 35.0)

            EscenarioSimulacion.CRITICO_FRIO ->
                random.nextDouble(1.0, 3.5)
        }

        return redondear(objetivo)
    }

    private fun validarLinea(lineaId: Int) {
        require(lineaId > 0) {
            "La línea debe tener un identificador válido."
        }
    }

    private fun redondear(valor: Double): Double {
        return round(valor * 10.0) / 10.0
    }
}