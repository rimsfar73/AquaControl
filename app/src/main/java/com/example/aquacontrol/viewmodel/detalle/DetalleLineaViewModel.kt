package com.example.aquacontrol.viewmodel.detalle

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.model.historial.HistorialTemperatura
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetalleLineaViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _historial =
        MutableStateFlow<List<HistorialTemperatura>>(emptyList())

    val historial: StateFlow<List<HistorialTemperatura>> =
        _historial.asStateFlow()

    fun cargarHistorial(lineaId: Int) {
        _historial.value = repository.obtenerHistorial(lineaId)
    }
}