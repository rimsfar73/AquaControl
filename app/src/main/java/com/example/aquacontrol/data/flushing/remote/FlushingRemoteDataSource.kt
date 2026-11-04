package com.example.aquacontrol.data.flushing.remote

// Agrupa las consultas y cambios de flushing que se envían al backend mediante Retrofit.
class FlushingRemoteDataSource(
    private val api: FlushingApiService
) {

    // Consulta el historial de la línea seleccionada.
    suspend fun listarPorLinea(lineaId: Long): List<FlushingRemotoDto> {
        return api.listar(lineaId)
    }

    // Obtiene un registro concreto para mostrar su detalle.
    suspend fun obtener(id: Long): FlushingRemotoDto {
        return api.obtener(id)
    }

    // Devuelve el registro con el ID asignado por el servidor.
    suspend fun crear(registro: FlushingRemotoDto): FlushingRemotoDto {
        return api.crear(registro)
    }

    // Devuelve los datos que el servidor guardó después de la edición.
    suspend fun actualizar(id: Long, registro: FlushingRemotoDto): FlushingRemotoDto {
        return api.actualizar(id, registro)
    }

    // Una respuesta HTTP de error se propaga al repositorio.
    suspend fun eliminar(id: Long) {
        api.eliminar(id)
    }
}