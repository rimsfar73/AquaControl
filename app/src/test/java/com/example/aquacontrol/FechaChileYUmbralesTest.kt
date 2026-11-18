package com.example.aquacontrol

import com.example.aquacontrol.data.temperatura.remote.aEpochMillisBackend
import com.example.aquacontrol.data.temperatura.remote.aFechaHoraBackend
import com.example.aquacontrol.model.estado.EstadoLinea
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class FechaChileYUmbralesTest {
    @Test fun fechaChileNoDependeDelRelojUTCDelEmulador() {
        val fecha = "2026-11-17T17:20:00"
        val epoch = fecha.aEpochMillisBackend()
        assertEquals(fecha, epoch.aFechaHoraBackend())
        val utc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(epoch))
        assertEquals("2026-11-17T20:20:00", utc)
    }

    @Test fun umbralesYValoresExtremosMantienenElContratoActual() {
        assertEquals(EstadoLinea.CRITICO, EstadoLinea.desdeTemperatura(1.3))
        assertEquals(EstadoLinea.CRITICO, EstadoLinea.desdeTemperatura(56.0))
        assertEquals(EstadoLinea.ADVERTENCIA, EstadoLinea.desdeTemperatura(5.0))
        assertEquals(EstadoLinea.NORMAL, EstadoLinea.desdeTemperatura(18.0))
        assertEquals(EstadoLinea.NORMAL, EstadoLinea.desdeTemperatura(21.0))
        assertEquals(EstadoLinea.ADVERTENCIA, EstadoLinea.desdeTemperatura(21.1))
        assertEquals(EstadoLinea.CRITICO, EstadoLinea.desdeTemperatura(30.0))
    }
}
