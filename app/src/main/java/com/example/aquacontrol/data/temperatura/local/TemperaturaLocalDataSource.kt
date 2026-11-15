package com.example.aquacontrol.data.temperatura.local

import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import kotlinx.coroutines.flow.Flow
import kotlin.collections.iterator

class TemperaturaLocalDataSource(
    private val dao: MedicionTemperaturaDao
) {

    // Delega al DAO la inserción de la medición y devuelve su ID.
    suspend fun guardarMedicion(
        medicion: MedicionTemperaturaEntity
    ): Long {
        return dao.insertarMedicion(medicion)
    }

    // Inserta o actualiza la copia local de una medición confirmada por PostgreSQL.
    // Si ya existe, conserva el ID local para no invalidar referencias de la interfaz.
    suspend fun guardarMedicionRemota(
        medicion: MedicionTemperaturaEntity
    ): Long {
        val idServidor = requireNotNull(medicion.idServidor) {
            "La medición remota debe incluir el ID del servidor."
        }
        require(idServidor > 0L) {
            "El servidor devolvió un ID de medición inválido."
        }

        val existente = dao.obtenerPorIdServidor(idServidor)
        if (existente != null) {
            val actualizadas = dao.actualizarMedicion(
                medicion.copy(id = existente.id)
            )
            check(actualizadas == 1) {
                "No se pudo actualizar la copia local de la medición."
            }
            return existente.id
        }

        return dao.insertarMedicion(medicion.copy(id = 0L))
    }

    // Sincroniza las filas remotas de una línea y conserva las mediciones locales simuladas.
    suspend fun sincronizarMedicionesRemotasPorLinea(
        lineaId: Int,
        mediciones: List<MedicionTemperaturaEntity>
    ) {
        require(lineaId > 0) { "Selecciona una línea válida." }
        require(mediciones.all { it.lineaId == lineaId && it.idServidor != null }) {
            "La respuesta remota contiene mediciones de otra línea o sin ID de servidor."
        }

        val recibidasPorId = mediciones.associateBy { it.idServidor!! }
        require(recibidasPorId.size == mediciones.size) {
            "El servidor devolvió IDs de temperatura repetidos."
        }

        val anteriores = dao.obtenerMedicionesRemotas()
            .filter { it.lineaId == lineaId }
            .associateBy { it.idServidor!! }

        for (medicion in mediciones) {
            guardarMedicionRemota(medicion)
        }

        // Retiramos copias eliminadas en PostgreSQL sin tocar filas locales/simuladas.
        for ((idServidor, anterior) in anteriores) {
            if (idServidor !in recibidasPorId) {
                dao.eliminarPorIdLocal(anterior.id)
            }
        }
    }

    // Actualiza todas las líneas recibidas y elimina de Room las copias remotas ausentes.
    suspend fun sincronizarTodasLasMedicionesRemotas(
        mediciones: List<MedicionTemperaturaEntity>
    ) {
        require(mediciones.all { it.idServidor != null && it.lineaId > 0 }) {
            "La respuesta remota contiene una medición sin identificadores válidos."
        }
        require(mediciones.map { it.idServidor }.distinct().size == mediciones.size) {
            "El servidor devolvió IDs de temperatura repetidos."
        }

        val recibidasPorId = mediciones.associateBy { it.idServidor!! }
        val anteriores = dao.obtenerMedicionesRemotas()
            .associateBy { it.idServidor!! }

        for (medicion in mediciones) {
            guardarMedicionRemota(medicion)
        }

        for ((idServidor, anterior) in anteriores) {
            if (idServidor !in recibidasPorId) {
                dao.eliminarPorIdLocal(anterior.id)
            }
        }
    }

    // Emite el historial de la línea y sus cambios posteriores.
    fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperaturaEntity>> {
        return dao.observarHistorialPorLinea(lineaId)
    }

    // Emite solo la página solicitada, respetando filtros de estado y fecha.
    fun observarPaginaHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperaturaEntity>> {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }

        require(filtro.rangoValido) {
            "La fecha inicial no puede ser posterior a la fecha final."
        }

        require(limite in 1..100) {
            "El tamaño de página debe estar entre 1 y 100."
        }

        require(desplazamiento >= 0) {
            "La posición de la página no puede ser negativa."
        }

        return dao.observarPaginaHistorial(
            lineaId = lineaId,
            estado = filtro.estado?.name,
            desdeInclusive = filtro.desdeInclusive,
            hastaExclusivo = filtro.hastaExclusivo,
            limite = limite,
            desplazamiento = desplazamiento
        )
    }

    // Emite el total filtrado para calcular los límites de paginación.
    fun observarCantidadHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura
    ): Flow<Int> {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }

        require(filtro.rangoValido) {
            "La fecha inicial no puede ser posterior a la fecha final."
        }

        return dao.observarCantidadHistorial(
            lineaId = lineaId,
            estado = filtro.estado?.name,
            desdeInclusive = filtro.desdeInclusive,
            hastaExclusivo = filtro.hastaExclusivo
        )
    }

    // Emite la lectura más reciente de la línea o null si aún no hay datos.
    fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperaturaEntity?> {
        return dao.observarUltimaMedicionPorLinea(lineaId)
    }

    // Emite una última lectura por línea para actualizar resúmenes y alertas.
    fun observarUltimasMediciones():
            Flow<List<MedicionTemperaturaEntity>> {
        return dao.observarUltimasMediciones()
    }

    // Busca una medición concreta mediante MedicionTemperaturaDao, dentro de su línea.
    suspend fun obtenerMedicion(
        lineaId: Int,
        medicionId: Long
    ): MedicionTemperaturaEntity? {
        validarIdentificadores(lineaId, medicionId)
        return dao.obtenerMedicion(lineaId, medicionId)
    }

    // Entrega la corrección al DAO; devuelve las filas afectadas para detectar registros inexistentes.
    suspend fun actualizarTemperatura(
        lineaId: Int,
        medicionId: Long,
        temperatura: Double
    ): Int {
        validarIdentificadores(lineaId, medicionId)
        require(temperatura.isFinite()) {
            "La temperatura debe ser un número válido."
        }
        return dao.actualizarTemperatura(lineaId, medicionId, temperatura)
    }

    // Delega la eliminación de una sola medición; el repositorio recibe la cantidad eliminada.
    suspend fun eliminarMedicion(
        lineaId: Int,
        medicionId: Long
    ): Int {
        validarIdentificadores(lineaId, medicionId)
        return dao.eliminarMedicion(lineaId, medicionId)
    }

    // Valida el intervalo del gráfico y conecta con MedicionTemperaturaDao.
    // Room vuelve a emitir los datos cuando se registran, editan o eliminan mediciones.
    fun observarMedicionesEnIntervalo(
        lineaId: Int,
        desdeInclusive: Long,
        hastaInclusive: Long
    ): Flow<List<MedicionTemperaturaEntity>> {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }
        require(desdeInclusive <= hastaInclusive) {
            "La fecha inicial no puede ser posterior a la fecha final."
        }
        return dao.observarMedicionesEnIntervalo(
            lineaId = lineaId,
            desdeInclusive = desdeInclusive,
            hastaInclusive = hastaInclusive
        )
    }

    // Evita enviar identificadores inválidos a las consultas de edición y eliminación.
    private fun validarIdentificadores(lineaId: Int, medicionId: Long) {
        require(lineaId > 0) {
            "Selecciona una línea válida."
        }
        require(medicionId > 0L) {
            "Selecciona una medición válida."
        }
    }
}