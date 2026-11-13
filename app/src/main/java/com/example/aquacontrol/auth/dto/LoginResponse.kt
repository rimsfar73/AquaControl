package com.example.aquacontrol.auth.dto

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
    val rol: String
)