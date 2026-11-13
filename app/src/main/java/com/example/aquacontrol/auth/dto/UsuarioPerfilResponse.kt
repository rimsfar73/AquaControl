
package com.example.aquacontrol.auth.dto

data class UsuarioPerfilResponse(
    val id: Long,
    val username: String,
    val rol: String,
    val activo: Boolean
)
