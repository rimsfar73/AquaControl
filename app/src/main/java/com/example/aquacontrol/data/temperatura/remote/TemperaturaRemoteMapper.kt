package com.example.aquacontrol.data.temperatura.remote

import com.example.aquacontrol.data.remote.TemperaturaRemotaDto
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaEntity
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Convierte la fecha ISO local que usa Spring LocalDateTime a la fecha epoch de Room.
private fun formatoBackend(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).apply {
        isLenient = false
        timeZone = TimeZone.getDefault()
    }

fun Long.aFechaHoraBackend(): String = formatoBackend().format(Date(this))

fun String.aEpochMillisBackend(): Long {
    // Spring puede omitir la fracción o incluir nanosegundos; Room conserva milisegundos.
    val fechaSinFraccion = substringBefore('.')
    return try {
        formatoBackend().parse(fechaSinFraccion)?.time
            ?: throw IllegalArgumentException("Fecha de temperatura vacía.")
    } catch (error: ParseException) {
        throw IllegalArgumentException(
            "El backend devolvió una fecha de temperatura inválida.",
            error
        )
    }
}

fun TemperaturaRemotaDto.aEntidadLocal(): MedicionTemperaturaEntity {
    val idConfirmado = requireNotNull(id) {
        "El backend devolvió una temperatura sin ID."
    }
    require(
        idConfirmado > 0L &&
                lineaId >= 1L &&
                lineaId <= Int.MAX_VALUE.toLong()
    ) {
        "El backend devolvió identificadores de temperatura inválidos."
    }
    require(temperatura.isFinite()) {
        "El backend devolvió una temperatura inválida."
    }

    val origen = if (observacion?.startsWith("AQUACONTROL_SIMULADA") == true) {
        OrigenMedicion.SIMULADA
    } else {
        OrigenMedicion.MANUAL
    }

    return MedicionTemperaturaEntity(
        id = 0L,
        lineaId = lineaId.toInt(),
        temperatura = temperatura,
        fechaHora = fechaHora.aEpochMillisBackend(),
        origen = origen.name,
        idServidor = idConfirmado,
        observacion = observacion
    )
}

fun MedicionTemperaturaEntity.aDtoRemoto(
    id: Long? = idServidor,
    observacionParaCrear: String? = observacion
): TemperaturaRemotaDto = TemperaturaRemotaDto(
    id = id,
    lineaId = lineaId.toLong(),
    temperatura = temperatura,
    fechaHora = fechaHora.aFechaHoraBackend(),
    observacion = observacionParaCrear
)