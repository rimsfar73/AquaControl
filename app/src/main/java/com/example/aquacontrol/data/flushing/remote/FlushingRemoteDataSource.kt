package com.example.aquacontrol.data.flushing.remote

import okhttp3.MultipartBody

// Agrupamos las operaciones que enviamos al backend.
// El repositorio utiliza esta clase y FlushingApiService define las rutas.
class FlushingRemoteDataSource(
    private val api: FlushingApiService
) {

    // Consultamos el historial de la línea seleccionada.
    suspend fun listarPorLinea(lineaId: Long): List<FlushingRemotoDto> {
        return api.listar(lineaId)
    }

    // Obtenemos un registro concreto para mostrar su detalle.
    suspend fun obtener(id: Long): FlushingRemotoDto {
        return api.obtener(id)
    }

    // Recibimos el registro confirmado y su ID generado por el servidor.
    suspend fun crear(registro: FlushingRemotoDto): FlushingRemotoDto {
        return api.crear(registro)
    }

    // Recibimos los datos que quedaron guardados después de editar.
    suspend fun actualizar(
        id: Long,
        registro: FlushingRemotoDto
    ): FlushingRemotoDto {
        return api.actualizar(id, registro)
    }

    // Solicitamos la eliminación. Si el servidor responde con un error,
    // lo dejamos llegar al repositorio para no informar un éxito falso.
    suspend fun eliminar(id: Long) {
        api.eliminar(id)
    }

    // Enviamos el archivo preparado con el nombre de formulario "foto".
    // Usamos el ID remoto del flushing para asociar correctamente la imagen.
    suspend fun guardarFoto(
        id: Long,
        foto: MultipartBody.Part
    ): FotoFlushingRemotaDto {
        return api.guardarFoto(id, foto)
    }

    // Leemos la imagen recibida y cerramos la respuesta mediante use.
    // Si no existe o falla la conexión, el repositorio recibirá el error
    // y podrá distinguir una foto ausente de un problema del servidor.
    suspend fun obtenerFoto(id: Long): ByteArray {
        return api.obtenerFoto(id).use { respuesta ->
            respuesta.bytes()
        }
    }
}