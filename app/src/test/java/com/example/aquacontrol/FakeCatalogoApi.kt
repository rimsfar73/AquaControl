package com.example.aquacontrol

import com.example.aquacontrol.data.remote.*

// Fake determinista: permite probar IDs reales no incluidos en el catálogo demo antiguo.
internal class FakeCatalogoApi(
    private val conLineaSeis: Boolean = false
) : AquaControlApiService {
    override suspend fun listarGranjas(): List<GranjaRemotaDto> = listOf(
        GranjaRemotaDto(1, "Granja Norte"), GranjaRemotaDto(3, "Granja Sur")
    )

    override suspend fun listarGalpones(granjaId: Long): List<GalponRemotoDto> =
        when (granjaId) {
            1L -> listOf(GalponRemotoDto(1, "Galpón 1", 1), GalponRemotoDto(2, "Galpón 2", 1))
            3L -> listOf(GalponRemotoDto(3, "Galpón 3", 3), GalponRemotoDto(5, "Galpón 4", 3))
            else -> emptyList()
        }

    override suspend fun listarLineas(galponId: Long): List<LineaRemotaDto> = when (galponId) {
        1L -> listOf(LineaRemotaDto(1, "Línea 1", 1), LineaRemotaDto(2, "Línea 2", 1))
        2L -> listOf(LineaRemotaDto(3, "Línea 1", 2))
        3L -> listOf(LineaRemotaDto(4, "Línea 1", 3))
        5L -> listOfNotNull(LineaRemotaDto(5, "Línea 1", 5),
            if (conLineaSeis) LineaRemotaDto(6, "Línea 2", 5) else null)
        else -> emptyList()
    }

    override suspend fun obtenerLineaPorId(id: Long): LineaRemotaDto =
        listOf(1L, 2L, 3L, 4L, 5L, 6L).firstOrNull { it == id }
            ?.let { LineaRemotaDto(it, "Línea 1", 5) }
            ?: error("No existe")

    override suspend fun listarTemperaturas(lineaId: Long?): List<TemperaturaRemotaDto> = emptyList()
    override suspend fun obtenerTemperatura(id: Long): TemperaturaRemotaDto = error("No usado")
    override suspend fun crearTemperatura(temperatura: TemperaturaRemotaDto): TemperaturaRemotaDto = error("No usado")
    override suspend fun actualizarTemperatura(id: Long, temperatura: TemperaturaRemotaDto): TemperaturaRemotaDto = error("No usado")
    override suspend fun eliminarTemperatura(id: Long) { error("No usado") }
}
