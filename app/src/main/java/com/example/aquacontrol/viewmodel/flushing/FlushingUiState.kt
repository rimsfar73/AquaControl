package com.example.aquacontrol.viewmodel.flushing

import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity
import com.example.aquacontrol.model.flushing.EventoFlushing

// Representamos lo que debe mostrar la pantalla durante la consulta:
// carga, resultados, historial vacío o error.
sealed class FlushingUiState {

    object Loading : FlushingUiState()

    data class Success(

        // Conservamos el formato local mientras adaptamos las pantallas.
        // No combinamos estos registros con los descargados del servidor.
        val eventos: List<EventoFlushing> = emptyList(),

        // Recibimos los registros remotos con sus identificadores originales.
        val registrosRemotos: List<FlushingRemotoEntity> = emptyList(),

        // Indicamos si mostramos una copia anterior por falta de conexión.
        val desdeCopiaLocal: Boolean = false

    ) : FlushingUiState()

    // Usamos este estado cuando la consulta confirma un historial vacío.
    object Empty : FlushingUiState()

    // Mostramos el motivo del fallo y permitimos reintentar.
    data class Error(
        val mensaje: String
    ) : FlushingUiState()
}