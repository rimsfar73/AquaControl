package com.example.aquacontrol.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class GranjaRemotaDto(
    val id: Long,
    val nombre: String,
    val ubicacion: String? = null
)

data class GalponRemotoDto(
    val id: Long,
    val nombre: String,
    val granjaId: Long
)

data class LineaRemotaDto(
    val id: Long,
    val nombre: String,
    val galponId: Long
)

data class TemperaturaRemotaDto(
    val id: Long? = null,
    val lineaId: Long,
    val temperatura: Double,
    val fechaHora: String,
    val observacion: String? = null
)

interface AquaControlApiService {

    @GET("api/v1/granjas")
    suspend fun listarGranjas(): List<GranjaRemotaDto>

    @GET("api/v1/galpones/por-granja/{granjaId}")
    suspend fun listarGalpones(
        @Path("granjaId") granjaId: Long
    ): List<GalponRemotoDto>

    @GET("api/v1/lineas/por-galpon/{galponId}")
    suspend fun listarLineas(
        @Path("galponId") galponId: Long
    ): List<LineaRemotaDto>

    @GET("api/v1/temperatura")
    suspend fun listarTemperaturas(
        @Query("lineaId") lineaId: Long? = null
    ): List<TemperaturaRemotaDto>

    @GET("api/v1/temperatura/{id}")
    suspend fun obtenerTemperatura(
        @Path("id") id: Long
    ): TemperaturaRemotaDto

    @POST("api/v1/temperatura")
    suspend fun crearTemperatura(
        @Body temperatura: TemperaturaRemotaDto
    ): TemperaturaRemotaDto

    @PUT("api/v1/temperatura/{id}")
    suspend fun actualizarTemperatura(
        @Path("id") id: Long,
        @Body temperatura: TemperaturaRemotaDto
    ): TemperaturaRemotaDto

    @DELETE("api/v1/temperatura/{id}")
    suspend fun eliminarTemperatura(
        @Path("id") id: Long
    )
}