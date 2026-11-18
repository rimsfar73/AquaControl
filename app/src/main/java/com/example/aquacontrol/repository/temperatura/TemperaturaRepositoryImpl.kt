// Sincroniza las lecturas manuales con el backend y conserva su copia observable en Room.
// Conecta con: data/temperatura/local/TemperaturaLocalDataSource.kt (desde com/example/aquacontrol).
// Conecta con: notificaciones/NotificadorAlertas.kt (desde com/example/aquacontrol).
package com.example.aquacontrol.repository.temperatura

import android.util.Log
import com.example.aquacontrol.data.temperatura.remote.TemperaturaRemoteDataSource
import com.example.aquacontrol.data.temperatura.remote.aDtoRemoto
import com.example.aquacontrol.data.temperatura.remote.aEntidadLocal
import com.example.aquacontrol.data.temperatura.local.MedicionTemperaturaEntity
import com.example.aquacontrol.data.temperatura.local.TemperaturaLocalDataSource
import com.example.aquacontrol.model.perfil.PerfilUsuario
import com.example.aquacontrol.model.temperatura.FiltroHistorialTemperatura
import com.example.aquacontrol.model.temperatura.MedicionTemperatura
import com.example.aquacontrol.model.temperatura.OrigenMedicion
import com.example.aquacontrol.notificaciones.NotificadorAlertas
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

class TemperaturaRepositoryImpl(
    private val localDataSource: TemperaturaLocalDataSource,
    private val notificadorAlertas: NotificadorAlertas,
    private val fuenteRemota: TemperaturaRemoteDataSource
) : TemperaturaRepository {

    // Registra una lectura con su línea, temperatura y origen.
    override suspend fun registrarMedicion(
        lineaId: Int,
        temperatura: Double,
        origen: OrigenMedicion
    ): MedicionTemperatura {
        val ubicacion = obtenerUbicacion(lineaId)

        require(temperatura.isFinite()) {
            "La temperatura debe ser un número válido."
        }

        // Las lecturas manuales se confirman primero en el servidor.
        // La simulación permanece local para no llenar PostgreSQL con datos ficticios.
        if (origen == OrigenMedicion.MANUAL) {
            sincronizarDesdeBackend(lineaId)
        }

        // Serializa lectura anterior, guardado y aviso para evitar comparaciones simultáneas.
        return bloqueoRegistro.withLock {
            val anterior = localDataSource
                .observarUltimaMedicionPorLinea(lineaId)
                .first()
                ?.toModelo()

            val entity = MedicionTemperaturaEntity(
                lineaId = lineaId,
                temperatura = temperatura,
                fechaHora = System.currentTimeMillis(),
                origen = origen.name
            )

            val entityGuardada = if (origen == OrigenMedicion.MANUAL) {
                val respuesta = fuenteRemota.crear(
                    entity.aDtoRemoto(
                        id = null,
                        observacionParaCrear = "Registro manual desde AquaControl"
                    )
                )
                val confirmada = respuesta.aEntidadLocal()
                require(
                    confirmada.lineaId == lineaId &&
                            confirmada.temperatura == temperatura
                ) {
                    "El backend devolvió una medición distinta a la enviada."
                }
                val idLocal = localDataSource.guardarMedicionRemota(confirmada)
                actualizarMarcaSincronizacion(lineaId)
                confirmada.copy(id = idLocal)
            } else {
                val idLocal = localDataSource.guardarMedicion(entity)
                entity.copy(id = idLocal)
            }

            val medicionGuardada = entityGuardada.toModelo()

            try {
                notificadorAlertas.notificarCambio(
                    medicion = medicionGuardada,
                    estadoAnterior = anterior?.estado,
                    ubicacion = ubicacion
                )
                // Propaga la cancelación de la corrutina; no la presenta como un fallo de datos.
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Un fallo al notificar no invalida la medición guardada.
                Log.e(
                    "TemperaturaRepository",
                    "La medición se guardó, pero no se pudo emitir el aviso.",
                    e
                )
            }

            medicionGuardada
        }
    }

    // Emite el historial de la línea y sus cambios posteriores.
    override fun observarHistorialPorLinea(
        lineaId: Int
    ): Flow<List<MedicionTemperatura>> {
        obtenerUbicacion(lineaId)

        return observarRoomTrasSincronizar(lineaId) {
            localDataSource.observarHistorialPorLinea(lineaId)
        }.map { mediciones ->
            mediciones.map { it.toModelo() }
        }
    }

    // Emite solo la página solicitada, respetando filtros de estado y fecha.
    override fun observarPaginaHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura,
        limite: Int,
        desplazamiento: Int
    ): Flow<List<MedicionTemperatura>> {
        obtenerUbicacion(lineaId)

        return observarRoomTrasSincronizar(lineaId) {
            localDataSource.observarPaginaHistorial(
                lineaId = lineaId,
                filtro = filtro,
                limite = limite,
                desplazamiento = desplazamiento
            )
        }.map { mediciones ->
            mediciones.map { it.toModelo() }
        }
    }

    // Emite el total filtrado para calcular los límites de paginación.
    override fun observarCantidadHistorial(
        lineaId: Int,
        filtro: FiltroHistorialTemperatura
    ): Flow<Int> {
        obtenerUbicacion(lineaId)

        return observarRoomTrasSincronizar(lineaId) {
            localDataSource.observarCantidadHistorial(
                lineaId = lineaId,
                filtro = filtro
            )
        }
    }

    // Emite la lectura más reciente de la línea o null si aún no hay datos.
    override fun observarUltimaMedicionPorLinea(
        lineaId: Int
    ): Flow<MedicionTemperatura?> {
        obtenerUbicacion(lineaId)

        return observarRoomTrasSincronizar(lineaId) {
            localDataSource.observarUltimaMedicionPorLinea(lineaId)
        }.map { medicion ->
            medicion?.toModelo()
        }
    }

    // Emite una última lectura por línea para actualizar resúmenes y alertas.
    override fun observarUltimasMediciones():
            Flow<List<MedicionTemperatura>> {
        return observarRoomTrasSincronizar(lineaId = null) {
            localDataSource.observarUltimasMediciones()
        }.map { mediciones ->
            mediciones.map { it.toModelo() }
        }
    }

    // Consulta el registro exacto para cargar el detalle o el formulario de edición.
    override suspend fun obtenerMedicion(
        lineaId: Int,
        medicionId: Long
    ): MedicionTemperatura? {
        obtenerUbicacion(lineaId)
        sincronizarSiDisponible(lineaId)
        return localDataSource.obtenerMedicion(lineaId, medicionId)?.toModelo()
    }

    // Solo Operario puede corregir temperaturas. No crea otra lectura ni cambia su fecha u origen.
    override suspend fun actualizarTemperatura(
        lineaId: Int,
        medicionId: Long,
        temperatura: Double,
        perfil: PerfilUsuario
    ): MedicionTemperatura {
        require(perfil == PerfilUsuario.OPERARIO) {
            "Solo el operario puede editar temperaturas."
        }
        obtenerUbicacion(lineaId)
        require(temperatura.isFinite()) {
            "La temperatura debe ser un número válido."
        }

        // Usa el mismo bloqueo del registro para no intercalar cambios con la simulación.
        return bloqueoRegistro.withLock {
            val existente = requireNotNull(
                localDataSource.obtenerMedicion(lineaId, medicionId)
            ) {
                "La medición ya no existe en esta línea. Actualiza el historial."
            }

            val temperaturaConfirmada = if (existente.idServidor != null) {
                val respuesta = fuenteRemota.actualizar(
                    existente.idServidor,
                    existente.copy(temperatura = temperatura).aDtoRemoto()
                )
                val confirmada = respuesta.aEntidadLocal()
                require(
                    confirmada.idServidor == existente.idServidor &&
                            confirmada.lineaId == lineaId
                ) {
                    "El backend devolvió una medición distinta a la editada."
                }
                confirmada.temperatura
            } else {
                temperatura
            }

            val actualizadas = localDataSource.actualizarTemperatura(
                lineaId = lineaId,
                medicionId = medicionId,
                temperatura = temperaturaConfirmada
            )
            check(actualizadas == 1) {
                "No se pudo actualizar la medición. Actualiza el historial."
            }

            // Retira el aviso original solo si su temperatura realmente cambió.
            // La corrección actualiza los indicadores mediante Room, sin emitir otra alarma.
            if (existente.temperatura != temperaturaConfirmada) {
                notificadorAlertas.retirarAvisoDeMedicion(lineaId, medicionId)
            }

            // Room emite los cambios a historiales y resúmenes; no se registra una lectura nueva.
            existente.copy(temperatura = temperaturaConfirmada).toModelo()
        }
    }

    // Solo Supervisor puede eliminar; el ID de línea impide afectar otro historial.
    override suspend fun eliminarMedicion(
        lineaId: Int,
        medicionId: Long,
        perfil: PerfilUsuario
    ) {
        require(perfil == PerfilUsuario.SUPERVISOR) {
            "Solo el supervisor puede eliminar temperaturas."
        }
        obtenerUbicacion(lineaId)

        bloqueoRegistro.withLock {
            val existente = requireNotNull(
                localDataSource.obtenerMedicion(lineaId, medicionId)
            ) {
                "La medición ya no existe en esta línea. Actualiza el historial."
            }

            // Eliminamos primero en PostgreSQL; si falla, Room conserva la fila.
            existente.idServidor?.let { fuenteRemota.eliminar(it) }

            val eliminadas = localDataSource.eliminarMedicion(lineaId, medicionId)
            check(eliminadas == 1) {
                "La medición ya no existe en esta línea. Actualiza el historial."
            }
            // Evita que quede un aviso cuyo destino acaba de eliminarse de Room.
            notificadorAlertas.retirarAvisoDeMedicion(lineaId, medicionId)
        }
        // Los Flow de Room recalculan el historial y la última medición disponible.
    }

    // Valida la línea y convierte las entidades de Room para el gráfico del supervisor.
    // Conserva el orden cronológico recibido de TemperaturaLocalDataSource.
    override fun observarMedicionesEnIntervalo(
        lineaId: Int,
        desdeInclusive: Long,
        hastaInclusive: Long
    ): Flow<List<MedicionTemperatura>> {
        obtenerUbicacion(lineaId)
        return observarRoomTrasSincronizar(lineaId) {
            localDataSource.observarMedicionesEnIntervalo(
                lineaId = lineaId,
                desdeInclusive = desdeInclusive,
                hastaInclusive = hastaInclusive
            )
        }.map { mediciones ->
            mediciones.map { it.toModelo() }
        }
    }

    // Room sigue siendo la fuente que observan los ViewModels. Antes de observarla,
    // actualizamos su copia desde el servidor; ante una caída de red usamos la caché.
    private fun <T> observarRoomTrasSincronizar(
        lineaId: Int?,
        crearFlujoLocal: () -> Flow<T>
    ): Flow<T> = flow {
        if (lineaId == null || lineaId > 0) {
            sincronizarSiDisponible(lineaId)
        }
        emitAll(crearFlujoLocal())
    }

    private suspend fun sincronizarSiDisponible(lineaId: Int?) {
        try {
            sincronizarDesdeBackend(lineaId)
        } catch (error: IOException) {
            // No ocultamos 401/403 ni respuestas HTTP inválidas; solo usamos Room
            // como respaldo cuando la conexión de red no está disponible.
            Log.w(
                "TemperaturaRepository",
                "Backend no disponible; se conserva la copia local.",
                error
            )
        }
    }

    private suspend fun sincronizarDesdeBackend(lineaId: Int?) {
        require(lineaId == null || lineaId > 0) {
            "Debes seleccionar una línea válida."
        }

        bloqueoSincronizacion.withLock {
            val clave = lineaId ?: CLAVE_SINCRONIZACION_GLOBAL
            val ahora = System.currentTimeMillis()
            val ultima = ultimaSincronizacion[clave] ?: 0L
            if (ahora - ultima < INTERVALO_MINIMO_SINCRONIZACION_MS) {
                return@withLock
            }

            val respuesta = fuenteRemota.listar(lineaId)
            val entidades = respuesta.map { it.aEntidadLocal() }

            require(entidades.all { lineaId == null || it.lineaId == lineaId }) {
                "El backend devolvió mediciones de otra línea."
            }

            if (lineaId == null) {
                localDataSource.sincronizarTodasLasMedicionesRemotas(entidades)
            } else {
                localDataSource.sincronizarMedicionesRemotasPorLinea(
                    lineaId = lineaId,
                    mediciones = entidades
                )
            }
            ultimaSincronizacion[clave] = System.currentTimeMillis()
        }
    }

    private suspend fun actualizarMarcaSincronizacion(lineaId: Int) {
        bloqueoSincronizacion.withLock {
            ultimaSincronizacion[lineaId] = System.currentTimeMillis()
        }
    }

    // Valida el formato del identificador sin consultar datos de demostración.
    // La identidad de la línea proviene del backend; una línea válida con ID 6
    // no debe rechazarse por no figurar en BebederoRepository.
    // El texto se utiliza únicamente como contexto de las notificaciones.
    private fun obtenerUbicacion(lineaId: Int): String {
        require(lineaId > 0) {
            "Debes seleccionar una línea válida."
        }
        return "Línea ID $lineaId"
    }

    // Convierte la entidad Room a una medición del dominio, recuperando su origen.
    private fun MedicionTemperaturaEntity.toModelo():
            MedicionTemperatura {
        return MedicionTemperatura(
            id = id,
            lineaId = lineaId,
            temperatura = temperatura,
            fechaHora = fechaHora,
            origen = OrigenMedicion.valueOf(origen),
            observacion = observacion
        )
    }

    companion object {
        // Evita comparar y guardar simultáneamente desde varias instancias.
        private val bloqueoRegistro = Mutex()
        private val bloqueoSincronizacion = Mutex()
        private val ultimaSincronizacion = mutableMapOf<Int, Long>()
        private const val CLAVE_SINCRONIZACION_GLOBAL = Int.MIN_VALUE
        private const val INTERVALO_MINIMO_SINCRONIZACION_MS = 15_000L
    }
}