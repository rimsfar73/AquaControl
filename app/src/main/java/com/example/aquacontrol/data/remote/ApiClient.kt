package com.example.aquacontrol.data.remote

import com.example.aquacontrol.auth.remote.AuthApiClient

// Usamos la conexión autenticada que ya utiliza el login.
// Así compartimos la configuración de Retrofit y el envío del JWT.
class ApiClient(baseUrl: String) {

    // AuthApiClient incorpora AuthInterceptor, que obtiene el token
    // de AuthSessionManager y lo agrega a las solicitudes al backend.
    private val clienteAutenticado = AuthApiClient(baseUrl)

    // Creamos servicios como FlushingApiService usando esa conexión.
    // Conservamos esta función para no cambiar sus llamadas actuales.
    fun <T> crearServicio(interfaz: Class<T>): T {
        return clienteAutenticado.crearServicio(interfaz)
    }
}