package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.model.flushing.EventoFlushing
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlushingViewModel : ViewModel() {

    private val repository = BebederoRepository()

    private val _flushingEvents =
        MutableStateFlow<List<EventoFlushing>>(emptyList())

    val flushingEvents: StateFlow<List<EventoFlushing>> =
        _flushingEvents.asStateFlow()

    fun cargarFlushing(lineaId: Int) {
        _flushingEvents.value = repository.obtenerFlushing(lineaId)
    }
}