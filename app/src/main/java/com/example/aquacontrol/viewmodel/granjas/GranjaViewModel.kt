package com.example.aquacontrol.viewmodel.granjas

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.model.granja.Granja
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GranjaViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _granjas = MutableStateFlow<List<Granja>>(emptyList())
    val granjas: StateFlow<List<Granja>> = _granjas.asStateFlow()

    init {
        cargarGranjas()
    }

    fun cargarGranjas() {
        _granjas.value = repository.obtenerGranjas()
    }
}