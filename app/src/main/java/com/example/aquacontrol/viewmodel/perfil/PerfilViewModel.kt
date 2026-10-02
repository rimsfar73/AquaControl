package com.example.aquacontrol.viewmodel.perfil

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.model.perfil.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PerfilViewModel : ViewModel() {

    // Nombre del usuario (puedes cambiarlo cuando tengas login real)
    private val _nombreUsuario = MutableStateFlow("Camila")
    val nombreUsuario: StateFlow<String> = _nombreUsuario.asStateFlow()

    // Rol seleccionado
    private val _perfilSeleccionado = MutableStateFlow<PerfilUsuario?>(null)
    val perfilSeleccionado: StateFlow<PerfilUsuario?> = _perfilSeleccionado.asStateFlow()

    fun seleccionarPerfil(perfil: PerfilUsuario) {
        _perfilSeleccionado.value = perfil
    }

    fun limpiarPerfil() {
        _perfilSeleccionado.value = null
    }

    // Si en el futuro quieres cambiar el nombre dinámicamente:
    fun actualizarNombre(nombre: String) {
        _nombreUsuario.value = nombre
    }
}
