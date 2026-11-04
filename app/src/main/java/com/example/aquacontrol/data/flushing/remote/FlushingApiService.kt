package com.example.aquacontrol.data.flushing.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Aquí definimos las rutas de flushing que ofrece el backend.
// FlushingRemoteDataSource llama a estos métodos y Retrofit envía las peticiones.
interface FlushingApiService {

    // Para mostrar el historial de una línea, enviamos su ID como filtro.
    // Si omitimos el filtro, el servidor devuelve los registros de todas las líneas.
    @GET("api/v1/flushing")
    suspend fun listar(
        @Query("lineaId") lineaId: Long? = null
    ): List<FlushingRemotoDto>

    // Cuando abrimos un detalle, ponemos en la ruta el ID del flushing elegido.
    @GET("api/v1/flushing/{id}")
    suspend fun obtener(@Path("id") id: Long): FlushingRemotoDto

    // Al registrar un flushing, enviamos los datos en el cuerpo de la petición.
    // Recibimos el registro guardado con el ID que le asignó el servidor.
    @POST("api/v1/flushing")
    suspend fun crear(@Body registro: FlushingRemotoDto): FlushingRemotoDto

    // Al editar, usamos el ID en la ruta para elegir el registro
    // y enviamos los datos corregidos en el cuerpo de la petición.
    @PUT("api/v1/flushing/{id}")
    suspend fun actualizar(
        @Path("id") id: Long,
        @Body registro: FlushingRemotoDto
    ): FlushingRemotoDto

    // Para eliminar, enviamos el ID del flushing. El servidor confirma
    // con el código HTTP 204, por eso este método no devuelve un registro.
    @DELETE("api/v1/flushing/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}