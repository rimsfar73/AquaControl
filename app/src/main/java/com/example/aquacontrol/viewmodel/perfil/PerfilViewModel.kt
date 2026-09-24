package com.example.aquacontrol.viewmodel.perfil

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.model.perfil.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PerfilViewModel : ViewModel() {

    private val _perfilSeleccionado =
        MutableStateFlow<PerfilUsuario?>(null)

    val perfilSeleccionado: StateFlow<PerfilUsuario?> =
        _perfilSeleccionado.asStateFlow()

    fun seleccionarPerfil(perfil: PerfilUsuario) {
        _perfilSeleccionado.value = perfil
    }

    fun limpiarPerfil() {
        _perfilSeleccionado.value = null
    }
}