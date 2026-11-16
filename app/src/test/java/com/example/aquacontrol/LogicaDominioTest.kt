package com.example.aquacontrol

import com.example.aquacontrol.data.flushing.remote.FlushingRemotoDto
import com.example.aquacontrol.data.flushing.remote.aCopiaLocal
import com.example.aquacontrol.data.flushing.remote.aDtoRemoto
import com.example.aquacontrol.data.remote.TemperaturaRemotaDto
import com.example.aquacontrol.data.temperatura.remote.aDtoRemoto
import com.example.aquacontrol.data.temperatura.remote.aEntidadLocal
import com.example.aquacontrol.data.temperatura.remote.aEpochMillisBackend
import com.example.aquacontrol.data.temperatura.remote.aFechaHoraBackend
import com.example.aquacontrol.data.simulacion.GeneradorTemperatura
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.flushing.FechasCalendarioFlushing
import com.example.aquacontrol.model.simulacion.EscenarioSimulacion
import com.example.aquacontrol.model.temperatura.FechasHistorialTemperatura
import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.viewmodel.historial.HistorialTemperaturaUiState
import com.example.aquacontrol.viewmodel.temperatura.DetalleTemperaturaUiState
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone
import kotlin.random.Random

class LogicaDominioTest {

    @Test
    fun estadoTemperaturaCubreLimitesYValoresNormales() {
        assertEquals(EstadoLinea.CRITICO, EstadoLinea.desdeTemperatura(4.99))
        assertEquals(EstadoLinea.ADVERTENCIA, EstadoLinea.desdeTemperatura(5.0))
        assertEquals(EstadoLinea.ADVERTENCIA, EstadoLinea.desdeTemperatura(17.99))
        assertEquals(EstadoLinea.NORMAL, EstadoLinea.desdeTemperatura(18.0))
        assertEquals(EstadoLinea.NORMAL, EstadoLinea.desdeTemperatura(21.0))
        assertEquals(EstadoLinea.ADVERTENCIA, EstadoLinea.desdeTemperatura(21.01))
        assertEquals(EstadoLinea.ADVERTENCIA, EstadoLinea.desdeTemperatura(29.99))
        assertEquals(EstadoLinea.CRITICO, EstadoLinea.desdeTemperatura(30.0))
    }

    @Test
    fun estadoRechazaNaNYValoresInfinitos() {
        listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).forEach {
            assertThrows(IllegalArgumentException::class.java) {
                EstadoLinea.desdeTemperatura(it)
            }
        }
    }

    @Test
    fun filtroDetectaFiltrosYRangoInvalido() {
        assertFalse(FiltroHistorialTemperatura().tieneFiltros)
        assertTrue(FiltroHistorialTemperatura(estado = EstadoLinea.CRITICO).tieneFiltros)
        assertTrue(FiltroHistorialTemperatura(desdeInclusive = 10L).tieneFiltros)
        assertTrue(
            FiltroHistorialTemperatura(
                desdeInclusive = 10L,
                hastaExclusivo = 11L
            ).rangoValido
        )
        assertFalse(
            FiltroHistorialTemperatura(
                desdeInclusive = 11L,
                hastaExclusivo = 11L
            ).rangoValido
        )
        assertFalse(
            FiltroHistorialTemperatura(
                desdeInclusive = 12L,
                hastaExclusivo = 11L
            ).rangoValido
        )
        assertTrue(
            FiltroHistorialTemperatura(
                desdeInclusive = null,
                hastaExclusivo = 11L
            ).rangoValido
        )
    }

    @Test
    fun estadoHistorialCalculaPaginasYRangoVisible() {
        val sinResultados = HistorialTemperaturaUiState()
        assertEquals(0, sinResultados.totalPaginas)
        assertEquals(0, sinResultados.primerResultadoVisible)
        assertEquals(0, sinResultados.ultimoResultadoVisible)

        val primera = HistorialTemperaturaUiState(totalResultados = 61)
        assertEquals(3, primera.totalPaginas)
        assertFalse(primera.puedeIrAnterior)
        assertTrue(primera.puedeIrSiguiente)

        val segunda = primera.copy(
            paginaActual = 1,
            mediciones = List(30) { index ->
                MedicionTemperatura(
                    index.toLong(), 1, 20.0, index.toLong(), OrigenMedicion.MANUAL
                )
            }
        )
        assertTrue(segunda.puedeIrAnterior)
        assertTrue(segunda.puedeIrSiguiente)
        assertEquals(31, segunda.primerResultadoVisible)
        assertEquals(60, segunda.ultimoResultadoVisible)
        assertFalse(segunda.copy(cargando = true).puedeIrAnterior)
        assertFalse(segunda.copy(error = "fallo").puedeIrSiguiente)
    }

    @Test
    fun estadoDetalleBloqueaInteraccionMientrasEstaOcupado() {
        val medicion = MedicionTemperatura(
            1, 1, 20.0, 1, OrigenMedicion.MANUAL
        )
        val cargado = DetalleTemperaturaUiState(
            cargando = false,
            medicion = medicion
        )

        assertFalse(cargado.ocupado)
        assertTrue(cargado.puedeInteractuar)

        val guardando = cargado.copy(guardando = true)
        assertTrue(guardando.ocupado)
        assertFalse(guardando.puedeInteractuar)

        val eliminando = cargado.copy(eliminando = true)
        assertTrue(eliminando.ocupado)
        assertFalse(eliminando.puedeInteractuar)

        assertFalse(cargado.copy(eliminado = true).puedeInteractuar)
    }

    @Test
    fun fechasFlushingParseaDiaYRechazaFormatoOFechaInvalida() {
        val esperado = utcMillis(2026, Calendar.NOVEMBER, 15)
        assertEquals(
            esperado,
            FechasCalendarioFlushing.diaDelRegistro(" 2026-11-15 07:22:54 ")
        )
        assertNull(FechasCalendarioFlushing.diaDelRegistro("15/11/2026"))
        assertNull(FechasCalendarioFlushing.diaDelRegistro("2026-02-30 10:00:00"))
        assertNull(
            FechasCalendarioFlushing.diaDelRegistro("2026-11-15 07:22:54 extra")
        )
    }

    @Test
    fun calendarioFlushingNavegaMesesYValidaDias() {
        val noviembre = utcMillis(2026, Calendar.NOVEMBER, 15)
        val diciembre = FechasCalendarioFlushing.moverMes(noviembre, 1)

        assertEquals(31, FechasCalendarioFlushing.diasDelMes(diciembre))
        assertEquals(30, FechasCalendarioFlushing.diasDelMes(noviembre))
        assertEquals(
            utcMillis(2026, Calendar.DECEMBER, 1),
            FechasCalendarioFlushing.inicioMes(diciembre)
        )
        assertEquals(
            utcMillis(2026, Calendar.NOVEMBER, 15),
            FechasCalendarioFlushing.fechaDelDia(noviembre, 15)
        )
        assertThrows(IllegalArgumentException::class.java) {
            FechasCalendarioFlushing.fechaDelDia(noviembre, 32)
        }
        assertEquals(
            0,
            FechasCalendarioFlushing.espaciosAntesDelPrimerDia(
                utcMillis(2024, Calendar.JANUARY, 1)
            )
        )
    }

    @Test
    fun fechasHistorialConvierteDiaSelectorAInicioYFinExclusivo() {
        val selector = utcMillis(2026, Calendar.NOVEMBER, 15)
        val inicio = FechasHistorialTemperatura.inicioLocalDesdeSelector(selector)
        val fin = FechasHistorialTemperatura.finLocalExclusivoDesdeSelector(selector)
        val local = Calendar.getInstance().apply { timeInMillis = inicio }

        assertEquals(0, local.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, local.get(Calendar.MINUTE))
        assertTrue(fin > inicio)
        assertEquals(selector, FechasHistorialTemperatura.selectorDesdeInicioLocal(inicio))
        assertEquals(selector, FechasHistorialTemperatura.selectorDesdeFinLocalExclusivo(fin))
    }

    @Test
    fun generadorRespetaLineaYProduceValoresRedondeados() {
        val generador = GeneradorTemperatura(Random(42))
        assertThrows(IllegalArgumentException::class.java) {
            generador.generarTemperatura(0)
        }

        val linea1 = (1..20).map { generador.generarTemperatura(1) }
        val linea2 = generador.generarTemperatura(2)
        assertTrue(
            linea1.all {
                it.isFinite() &&
                    kotlin.math.round(it * 10.0) / 10.0 == it
            }
        )
        assertTrue(linea2.isFinite())

        generador.seleccionarEscenario(1, EscenarioSimulacion.CRITICO_CALOR)
        val posterior = (1..100).map { generador.generarTemperatura(1) }
        assertTrue(posterior.all { it.isFinite() })

        generador.reiniciar()
        assertTrue(generador.generarTemperatura(1).isFinite())
    }

    @Test
    fun mapperTemperaturaConvierteFechaYOrigen() {
        val dto = TemperaturaRemotaDto(
            8L, 1L, 21.5,
            "2026-11-15T07:22:54.123456",
            "AQUACONTROL_SIMULADA"
        )
        val entidad = dto.aEntidadLocal()

        assertEquals(8L, entidad.idServidor)
        assertEquals(1, entidad.lineaId)
        assertEquals(OrigenMedicion.SIMULADA.name, entidad.origen)
        assertEquals("2026-11-15T07:22:54", entidad.fechaHora.aFechaHoraBackend())
        assertEquals(entidad.fechaHora, "2026-11-15T07:22:54".aEpochMillisBackend())

        val reenviado = entidad.aDtoRemoto(id = null, observacionParaCrear = "manual")
        assertNull(reenviado.id)
        assertEquals("manual", reenviado.observacion)
    }

    @Test
    fun mapperTemperaturaRechazaIdsFechaYTemperaturaInvalidos() {
        assertThrows(IllegalArgumentException::class.java) {
            TemperaturaRemotaDto(null, 1L, 20.0, "2026-11-15T00:00:00").aEntidadLocal()
        }
        assertThrows(IllegalArgumentException::class.java) {
            TemperaturaRemotaDto(1L, 0L, 20.0, "2026-11-15T00:00:00").aEntidadLocal()
        }
        assertThrows(IllegalArgumentException::class.java) {
            TemperaturaRemotaDto(1L, 1L, Double.NaN, "2026-11-15T00:00:00").aEntidadLocal()
        }
        assertThrows(IllegalArgumentException::class.java) {
            "fecha-mala".aEpochMillisBackend()
        }
    }

    @Test
    fun mapperFlushingValidaYOmiteDatosLocalesAlVolverAlServidor() {
        val dto = FlushingRemotoDto(
            5L, 2L, "2026-11-15T10:00:00", 120, "prueba", 25.0, 19.0
        )
        val local = dto.aCopiaLocal("http://api", 123L, "foto.jpg")

        assertEquals(5L, local.id)
        assertEquals("foto.jpg", local.fotoRuta)
        assertEquals(dto, local.aDtoRemoto())

        assertThrows(IllegalArgumentException::class.java) {
            dto.copy(id = null).aCopiaLocal("api", 1L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            dto.aCopiaLocal(" ", 1L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            dto.copy(duracionSegundos = 0).aCopiaLocal("api", 1L)
        }
        assertThrows(IllegalArgumentException::class.java) {
            dto.copy(temperaturaAntes = Double.POSITIVE_INFINITY).aCopiaLocal("api", 1L)
        }
    }

    private fun utcMillis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis
}
