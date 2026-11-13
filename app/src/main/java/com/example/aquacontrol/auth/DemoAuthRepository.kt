package com.example.aquacontrol.auth

import com.example.aquacontrol.model.perfil.PerfilUsuario

/** DEMOSTRACIÓN LOCAL. No usar estas credenciales en producción. */
object DemoAuthRepository {

    data class UsuarioDemo(
        val nombre: String,
        val rol: PerfilUsuario
    )

    fun ingresar(
        usuario: String,
        clave: String
    ): UsuarioDemo? = when {

        usuario.trim().equals(
            "operario",
            ignoreCase = true
        ) && clave == "demo1234" ->
            UsuarioDemo(
                "Operario demo",
                PerfilUsuario.OPERARIO
            )

        usuario.trim().equals(
            "supervisor",
            ignoreCase = true
        ) && clave == "demo5678" ->
            UsuarioDemo(
                "Supervisor demo",
                PerfilUsuario.SUPERVISOR
            )

        else -> null
    }
}