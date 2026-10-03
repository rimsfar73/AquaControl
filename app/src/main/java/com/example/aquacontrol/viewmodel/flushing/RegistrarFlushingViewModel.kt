package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RegistrarFlushingViewModel : ViewModel() {

    fun registrarFlushing(observacion: String) {
        viewModelScope.launch {
            // Aquí va tu lógica real:
            // llamada a API, guardar en Room, etc.
            println("Registrando flushing con observación: $observacion")
        }
    }
}
