package com.example.aquacontrol.repository.data

import com.example.aquacontrol.data.flushing.remote.FlushingApiClient
import com.example.aquacontrol.data.flushing.remote.FlushingRemoteDataSource
import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto
import com.example.aquacontrol.data.flushing.remote.FotoFlushingRemotaDto
import okhttp3.MultipartBody

// Ofrecemos las operaciones de flushing y fotos del servidor.
// Delegamos las solicitudes en FlushingRemoteDataSource.
class FlushingRemoteRepository(
    private val fuenteRemota: FlushingRemoteDataSource =
        FlushingRemoteDataSource(FlushingApiClient.service)
) {

    // Recibimos el registro confirmado por el backend, incluido su ID.
    suspend fun registrar(registro: FlushingRemotoDto): FlushingRemotoDto {
        return fuenteRemota.crear(registro)
    }

    // Consultamos los registros que pertenecen a una línea.
    suspend fun obtenerPorLinea(lineaId: Long): List<FlushingRemotoDto> {
        return fuenteRemota.listarPorLinea(lineaId)
    }

    // Consultamos un registro concreto usando su ID del servidor.
    suspend fun obtener(id: Long): FlushingRemotoDto {
        return fuenteRemota.obtener(id)
    }

    // Enviamos los cambios y recibimos la versión guardada.
    suspend fun actualizar(
        id: Long,
        registro: FlushingRemotoDto
    ): FlushingRemotoDto {
        return fuenteRemota.actualizar(id, registro)
    }

    // El backend comprueba el permiso de supervisor y elimina
    // el flushing junto con su foto. Los errores llegan al llamador.
    suspend fun eliminar(id: Long) {
        fuenteRemota.eliminar(id)
    }

    // Asociamos la imagen con un flushing que ya existe en PostgreSQL.
    // El archivo debe prepararse como una parte llamada "foto".
    suspend fun guardarFoto(
        id: Long,
        foto: MultipartBody.Part
    ): FotoFlushingRemotaDto {
        require(id > 0) {
            "Se necesita el ID del flushing guardado en el servidor."
        }
        return fuenteRemota.guardarFoto(id, foto)
    }

    // Obtenemos los bytes de la evidencia para visualizarla.
    // Conservamos los errores para distinguir una foto inexistente
    // de una sesión vencida o un problema de conexión.
    suspend fun obtenerFoto(id: Long): ByteArray {
        require(id > 0) {
            "Se necesita un ID de flushing válido."
        }
        return fuenteRemota.obtenerFoto(id)
    }
}