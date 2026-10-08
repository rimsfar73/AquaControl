package com.example.aquacontrol.viewmodel.flushing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquacontrol.model.flushing.FlushingDTO
import com.example.aquacontrol.repository.flushing.FlushingRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrarFlushingViewModel(
    private val repository: FlushingRepository
) : ViewModel() {

    fun registrarFlushing(
        lineaId: Int,
        observacion: String,
        temperaturaAntes: Double,
        temperaturaDespues: Double,
        duracionSegundos: Int?
    ) {
        viewModelScope.launch {

            val dto = FlushingDTO(
                fechaHora = obtenerFechaActual(),
                lineaId = lineaId,
                observacion = observacion,
                temperaturaAntes = temperaturaAntes,
                temperaturaDespues = temperaturaDespues,
                duracionSegundos = duracionSegundos
            )

            repository.registrarFlushing(dto)
        }
    }

    private fun obtenerFechaActual(): String {
        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formato.format(Date())
    }
}
