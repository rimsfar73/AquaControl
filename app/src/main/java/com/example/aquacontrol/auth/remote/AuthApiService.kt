
package com.example.aquacontrol.auth.remote

import com.example.aquacontrol.auth.dto.LoginRequest
import com.example.aquacontrol.auth.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}
