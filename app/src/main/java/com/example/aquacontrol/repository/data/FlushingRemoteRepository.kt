package com.example.aquacontrol.repository.data

import com.example.aquacontrol.data.flushing.remote.FlushingApiClient
import com.example.aquacontrol.data.flushing.remote.FlushingRemoteDataSource
import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto

// Ofrece las operaciones de flushing remoto y delega las llamadas al servidor
// en FlushingRemoteDataSource. En las pruebas se puede entregar otra fuente.
class FlushingRemoteRepository(
    private val fuenteRemota: FlushingRemoteDataSource =
        FlushingRemoteDataSource(FlushingApiClient.service)
) {

    // Devuelve el registro confirmado por el servidor, incluido el ID asignado.
    suspend fun registrar(registro: FlushingRemotoDto): FlushingRemotoDto {
        return fuenteRemota.crear(registro)
    }

    // Usa el ID de la línea para consultar su historial de flushing.
    suspend fun obtenerPorLinea(lineaId: Long): List<FlushingRemotoDto> {
        return fuenteRemota.listarPorLinea(lineaId)
    }

    // Usa el ID del flushing para abrir un registro concreto del historial.
    suspend fun obtener(id: Long): FlushingRemotoDto {
        return fuenteRemota.obtener(id)
    }

    // Envía la edición y devuelve la versión que quedó guardada en el servidor.
    suspend fun actualizar(id: Long, registro: FlushingRemotoDto): FlushingRemotoDto {
        return fuenteRemota.actualizar(id, registro)
    }

    // Solicita eliminar el flushing por su ID. Si el servidor falla,
    // el error llega a quien llamó para que pueda informar el resultado.
    suspend fun eliminar(id: Long) {
        fuenteRemota.eliminar(id)
    }
}