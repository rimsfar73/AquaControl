package com.example.aquacontrol.data.flushing.remote

import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity

// Convertimos un registro confirmado por el backend en su copia local.
// El repositorio utilizará esta función antes de guardarlo en Room.
fun FlushingRemotoDto.aCopiaLocal(
    servidor: String,
    descargadoEn: Long,
    fotoRuta: String? = null
): FlushingRemotoEntity {

    // No inventamos un ID si la respuesta del servidor está incompleta.
    val idRemoto = requireNotNull(id) {
        "El servidor devolvió un flushing sin identificador."
    }

    require(idRemoto > 0 && lineaId > 0) {
        "El servidor devolvió identificadores inválidos."
    }

    require(servidor.isNotBlank()) {
        "Falta identificar el servidor de origen."
    }

    require(fechaHora.isNotBlank() && duracionSegundos > 0) {
        "El servidor devolvió una fecha vacía o una duración inválida."
    }

    // Admitimos temperaturas sin informar, pero rechazamos valores
    // no numéricos como infinito o NaN antes de actualizar la copia.
    require(
        temperaturaAntes?.isFinite() != false &&
                temperaturaDespues?.isFinite() != false
    ) {
        "El servidor devolvió temperaturas inválidas."
    }

    return FlushingRemotoEntity(
        servidor = servidor,
        id = idRemoto,
        lineaId = lineaId,
        fechaHora = fechaHora,
        duracionSegundos = duracionSegundos,
        observacion = observacion,
        temperaturaAntes = temperaturaAntes,
        temperaturaDespues = temperaturaDespues,
        fotoRuta = fotoRuta,
        descargadoEn = descargadoEn
    )
}

// Recuperamos el formato remoto desde una copia descargada.
// No enviamos al backend la ruta local de la foto ni la fecha de descarga.
fun FlushingRemotoEntity.aDtoRemoto(): FlushingRemotoDto {
    return FlushingRemotoDto(
        id = id,
        lineaId = lineaId,
        fechaHora = fechaHora,
        duracionSegundos = duracionSegundos,
        observacion = observacion,
        temperaturaAntes = temperaturaAntes,
        temperaturaDespues = temperaturaDespues
    )
}