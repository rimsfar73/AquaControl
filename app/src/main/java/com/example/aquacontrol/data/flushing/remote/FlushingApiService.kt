package com.example.aquacontrol.data.flushing.remote

import com.example.aquacontrol.model.flushing.FlushingDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FlushingApiService {

    @POST("flushing")
    suspend fun registrarFlushing(@Body dto: FlushingDTO): Boolean

    @GET("flushing/{lineaId}")
    suspend fun obtenerFlushingPorLinea(@Path("lineaId") lineaId: Int): List<FlushingDTO>
}
