package com.example.aquacontrol.viewmodel.flushing

import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto
import com.example.aquacontrol.model.flushing.EventoFlushing

data class RegistrarFlushingUiState(
    val lineaId: Int? = null,
    val observacion: String = "",
    val temperaturaAntes: String = "",
    val temperaturaDespues: String = "",
    val duracionSegundos: String = "",
    val fotoRuta: String? = null,
    val errorFoto: String? = null,

    val errorLinea: String? = null,
    val errorObservacion: String? = null,
    val errorTemperaturaAntes: String? = null,
    val errorTemperaturaDespues: String? = null,
    val errorDuracion: String? = null,

    val guardando: Boolean = false,
    val errorGuardado: String? = null,

    // Conservamos temporalmente el resultado del formulario local
    // mientras completamos la conexión con el backend.
    val eventoGuardado: EventoFlushing? = null,

    // Recordamos el registro confirmado por el backend para
    // reintentar su foto sin volver a crear el flushing.
    val registroConfirmado: FlushingRemotoDto? = null,

    // Confirmamos que terminó el guardado del registro y,
    // cuando corresponde, también la subida de su foto.
    val guardadoRemotoCompleto: Boolean = false
)