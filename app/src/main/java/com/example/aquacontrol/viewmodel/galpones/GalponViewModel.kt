package com.example.aquacontrol.viewmodel.galpones

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.model.galpon.Galpon
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalponViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _galpones = MutableStateFlow<List<Galpon>>(emptyList())
    val galpones: StateFlow<List<Galpon>> = _galpones.asStateFlow()

    fun cargarGalpones(granjaId: Int) {
        _galpones.value = repository.obtenerGalpones(granjaId)
    }
}