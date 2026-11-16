package com.example.aquacontrol

import com.example.aquacontrol.auth.AuthSessionManager
import com.example.aquacontrol.auth.LoginViewModel
import com.example.aquacontrol.auth.dto.LoginResponse
import org.junit.Assert.*
import org.junit.Test

class AuthSessionManagerTest {
    @Test
    fun guardaTokenTipoYRolYLosLimpiaAlCerrarSesion() {
        AuthSessionManager.limpiarSesion()
        val respuesta = LoginResponse(
            accessToken = "jwt-prueba",
            tokenType = "Bearer",
            expiresIn = 3_600,
            rol = "SUPERVISOR"
        )

        AuthSessionManager.guardarSesion(respuesta)
        assertEquals("jwt-prueba", AuthSessionManager.obtenerToken())
        assertEquals("SUPERVISOR", AuthSessionManager.obtenerRol())

        AuthSessionManager.limpiarSesion()
        assertNull(AuthSessionManager.obtenerToken())
        assertNull(AuthSessionManager.obtenerRol())
    }

    @Test
    fun rechazaSesionInvalidaSinReemplazarLaSesionVigente() {
        AuthSessionManager.limpiarSesion()
        AuthSessionManager.guardarSesion(
            LoginResponse("token-valido", "Bearer", 3_600, "OPERARIO")
        )

        val invalidas = listOf(
            LoginResponse(" ", "Bearer", 100, "OPERARIO"),
            LoginResponse("token", "Basic", 100, "OPERARIO"),
            LoginResponse("token", "Bearer", 0, "OPERARIO")
        )
        invalidas.forEach { respuesta ->
            try {
                AuthSessionManager.guardarSesion(respuesta)
                fail("La sesión inválida debe rechazarse")
            } catch (_: IllegalArgumentException) {
                assertEquals("token-valido", AuthSessionManager.obtenerToken())
            }
        }
        AuthSessionManager.limpiarSesion()
    }

    @Test
    fun loginValidaCamposVaciosSinLlamarAlBackendYLimpiaResultado() {
        val vm = LoginViewModel()

        vm.ingresar("  ", "clave")
        assertEquals("Ingresa usuario y contraseña.", vm.uiState.value.error)
        assertFalse(vm.uiState.value.cargando)

        vm.ingresar("usuario", "")
        assertEquals("Ingresa usuario y contraseña.", vm.uiState.value.error)

        vm.limpiarResultado()
        assertNull(vm.uiState.value.error)
        assertNull(vm.uiState.value.respuesta)
        assertFalse(vm.uiState.value.cargando)
        AuthSessionManager.limpiarSesion()
    }
}
