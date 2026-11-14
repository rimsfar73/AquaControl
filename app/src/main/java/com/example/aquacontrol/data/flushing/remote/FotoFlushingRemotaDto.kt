package com.example.aquacontrol.data.flushing.remote

// Recibimos la confirmación del backend cuando guardamos una foto.
// Coincide con FotoDTO del backend, dentro de foto/dto/FotoDTO.java.
data class FotoFlushingRemotaDto(

    // Identificamos el registro de flushing al que quedó asociada la foto.
    val flushingId: Long,

    // Recibimos el formato validado por el servidor: image/jpeg o image/png.
    val tipoContenido: String,

    // Recibimos el tamaño guardado en bytes para comprobar la respuesta.
    val tamanioBytes: Long
)