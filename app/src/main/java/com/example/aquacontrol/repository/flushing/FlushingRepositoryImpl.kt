package com.example.aquacontrol.repository.flushing

import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.model.flushing.FlushingDTO

class FlushingRepositoryImpl : FlushingRepository {

    override suspend fun registrarFlushing(dto: FlushingDTO): EventoFlushing {

        // Simulación de guardado (luego lo conectamos a Room)
        return EventoFlushing(
            id = (0..9999).random(),              // ID simulado
            lineaId = dto.lineaId,
            fechaHora = dto.fechaHora,
            temperaturaAntes = dto.temperaturaAntes,
            temperaturaDespues = dto.temperaturaDespues,
            duracionSegundos = dto.duracionSegundos ?: 0
        )
    }
}
