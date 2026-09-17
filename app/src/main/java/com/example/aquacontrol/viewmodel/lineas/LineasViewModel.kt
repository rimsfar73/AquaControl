package com.example.aquacontrol.viewmodel.lineas

import androidx.lifecycle.ViewModel
import com.example.aquacontrol.repository.bebedero.BebederoRepository
import com.example.aquacontrol.model.linea.LineaBebedero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LineaViewModel(
    private val repo: BebederoRepository = BebederoRepository()
) : ViewModel() {

    private val _lineas = MutableStateFlow<List<LineaBebedero>>(emptyList())
    val lineas: StateFlow<List<LineaBebedero>> = _lineas.asStateFlow()

    fun cargar(galponId: Int) {
        _lineas.value = repo.obtenerLineas(galponId)
    }
}