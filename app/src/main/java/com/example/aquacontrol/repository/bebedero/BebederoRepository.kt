package com.example.aquacontrol.repository.bebedero

import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.galpon.Galpon
import com.example.aquacontrol.model.granja.Granja
import com.example.aquacontrol.model.linea.LineaBebedero
import com.example.aquacontrol.model.historial.HistorialTemperatura
import com.example.aquacontrol.model.flushing.EventoFlushing

class BebederoRepository {

    fun obtenerGranjas(): List<Granja> {
        return listOf(
            Granja(id = 1, nombre = "Granja Norte"),
            Granja(id = 2, nombre = "Granja Sur")
        )
    }

    fun obtenerGalpones(granjaId: Int): List<Galpon> {
        val galpones = listOf(
            Galpon(id = 1, nombre = "Galpón 1", granjaId = 1),
            Galpon(id = 2, nombre = "Galpón 2", granjaId = 1),
            Galpon(id = 3, nombre = "Galpón 3", granjaId = 2),
            Galpon(id = 4, nombre = "Galpón 4", granjaId = 2)
        )

        return galpones.filter { it.granjaId == granjaId }
    }

    fun obtenerLineas(galponId: Int): List<LineaBebedero> {
        val lineas = listOf(
            LineaBebedero(
                id = 1,
                nombre = "Línea 1",
                temperatura = 18.5,
                estado = EstadoLinea.NORMAL,
                actualizado = "2026-09-16 14:00",
                galponId = 1
            ),
            LineaBebedero(
                id = 2,
                nombre = "Línea 2",
                temperatura = 22.1,
                estado = EstadoLinea.ADVERTENCIA,
                actualizado = "2026-09-16 14:05",
                galponId = 1
            ),
            LineaBebedero(
                id = 3,
                nombre = "Línea 1",
                temperatura = 28.7,
                estado = EstadoLinea.CRITICO,
                actualizado = "2026-09-16 14:10",
                galponId = 2
            ),
            LineaBebedero(
                id = 4,
                nombre = "Línea 1",
                temperatura = 19.0,
                estado = EstadoLinea.NORMAL,
                actualizado = "2026-09-16 14:15",
                galponId = 3
            ),
            LineaBebedero(
                id = 5,
                nombre = "Línea 1",
                temperatura = 18.8,
                estado = EstadoLinea.NORMAL,
                actualizado = "2026-09-16 14:20",
                galponId = 4
            )
        )

        return lineas.filter { it.galponId == galponId }
    }
    fun obtenerHistorial(lineaId: Int): List<HistorialTemperatura> {
        val historial = listOf(
            HistorialTemperatura(
                id = 1,
                lineaId = 1,
                temperatura = 18.2,
                fechaHora = "2026-09-16 13:00"
            ),
            HistorialTemperatura(
                id = 2,
                lineaId = 1,
                temperatura = 18.5,
                fechaHora = "2026-09-16 14:00"
            ),
            HistorialTemperatura(
                id = 3,
                lineaId = 2,
                temperatura = 22.1,
                fechaHora = "2026-09-16 14:05"
            ),
            HistorialTemperatura(
                id = 4,
                lineaId = 3,
                temperatura = 28.7,
                fechaHora = "2026-09-16 14:10"
            ),
            HistorialTemperatura(
                id = 5,
                lineaId = 4,
                temperatura = 19.0,
                fechaHora = "2026-09-16 14:15"
            ),
            HistorialTemperatura(
                id = 6,
                lineaId = 5,
                temperatura = 18.8,
                fechaHora = "2026-09-16 14:20"
            )
        )

        return historial.filter { it.lineaId == lineaId }
    }
    fun obtenerFlushing(lineaId: Int): List<EventoFlushing> {
        val eventos = listOf(
            EventoFlushing(
                id = 1,
                lineaId = 1,
                fechaHora = "2026-09-15 10:00",
                temperaturaAntes = 27.5,
                temperaturaDespues = 19.2,
                duracionSegundos = 120
            ),
            EventoFlushing(
                id = 2,
                lineaId = 2,
                fechaHora = "2026-09-15 11:00",
                temperaturaAntes = 26.8,
                temperaturaDespues = 18.9,
                duracionSegundos = 90
            )
        )

        return eventos.filter { it.lineaId == lineaId }
    }
}