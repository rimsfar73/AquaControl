package com.example.aquacontrol.data.temperatura.remote

import com.example.aquacontrol.data.remote.AquaControlApiClient
import com.example.aquacontrol.data.remote.AquaControlApiService
import com.example.aquacontrol.data.remote.TemperaturaRemotaDto

// Encapsula las llamadas de temperatura al backend autenticado.
class TemperaturaRemoteDataSource(
    private val api: AquaControlApiService = AquaControlApiClient.service
) {

    suspend fun listar(lineaId: Int? = null): List<TemperaturaRemotaDto> {
        require(lineaId == null || lineaId > 0) {
            "Selecciona una línea válida."
        }
        return api.listarTemperaturas(lineaId?.toLong())
    }

    suspend fun crear(
        medicion: TemperaturaRemotaDto
    ): TemperaturaRemotaDto = api.crearTemperatura(medicion)

    suspend fun actualizar(
        idServidor: Long,
        medicion: TemperaturaRemotaDto
    ): TemperaturaRemotaDto {
        require(idServidor > 0L) { "El ID del servidor no es válido." }
        return api.actualizarTemperatura(idServidor, medicion)
    }

    suspend fun eliminar(idServidor: Long) {
        require(idServidor > 0L) { "El ID del servidor no es válido." }
        api.eliminarTemperatura(idServidor)
    }
}