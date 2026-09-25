package com.example.aquacontrol.repository.bebedero

import com.example.aquacontrol.model.linea.LineaBebedero
import com.example.aquacontrol.model.estado.EstadoLinea

class BebederoRepository {

    fun obtenerLineas(galponId: Int): List<LineaBebedero> {
        return listOf(
            LineaBebedero(1, "Línea 1", 18.5, EstadoLinea.NORMAL, "2026-09-16 14:00"),
            LineaBebedero(2, "Línea 2", 22.1, EstadoLinea.ADVERTENCIA, "2026-09-16 14:05"),
            LineaBebedero(3, "Línea 3", 28.7, EstadoLinea.CRITICO, "2026-09-16 14:10")
        )
    }
}