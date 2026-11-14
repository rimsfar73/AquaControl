package com.example.aquacontrol.data.flushing.remote

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// Definimos las rutas que ofrece nuestro backend.
// FlushingRemoteDataSource utiliza estos métodos para enviar las solicitudes.
interface FlushingApiService {

    // Consultamos el historial de una línea mediante su ID.
    // Si no enviamos filtro, obtenemos los registros de todas las líneas.
    @GET("api/v1/flushing")
    suspend fun listar(
        @Query("lineaId") lineaId: Long? = null
    ): List<FlushingRemotoDto>

    // Consultamos un registro concreto para mostrar su detalle.
    @GET("api/v1/flushing/{id}")
    suspend fun obtener(
        @Path("id") id: Long
    ): FlushingRemotoDto

    // Enviamos los datos y recibimos el registro confirmado por el servidor,
    // incluido el ID que utilizaremos para asociar su foto.
    @POST("api/v1/flushing")
    suspend fun crear(
        @Body registro: FlushingRemotoDto
    ): FlushingRemotoDto

    // Enviamos los cambios del registro seleccionado.
    // El backend comprueba que el usuario tenga permiso de operario.
    @PUT("api/v1/flushing/{id}")
    suspend fun actualizar(
        @Path("id") id: Long,
        @Body registro: FlushingRemotoDto
    ): FlushingRemotoDto

    // Solicitamos eliminar el registro con una sesión de supervisor.
    // El backend elimina también su foto y confirma con HTTP 204.
    @DELETE("api/v1/flushing/{id}")
    suspend fun eliminar(
        @Path("id") id: Long
    )

    // Enviamos la imagen como archivo, separada del JSON del registro.
    // Al preparar MultipartBody.Part debemos usar el nombre "foto",
    // porque así lo recibe FotoController en el backend.
    @Multipart
    @PUT("api/v1/flushing/{id}/foto")
    suspend fun guardarFoto(
        @Path("id") id: Long,
        @Part foto: MultipartBody.Part
    ): FotoFlushingRemotaDto

    // Descargamos los bytes de la imagen para mostrar la evidencia.
    // Quien reciba ResponseBody debe cerrarlo con use al leerlo.
    // Si no existe foto, el backend responde HTTP 404.
    @GET("api/v1/flushing/{id}/foto")
    suspend fun obtenerFoto(
        @Path("id") id: Long
    ): ResponseBody
}