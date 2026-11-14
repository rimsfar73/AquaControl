package com.example.aquacontrol.repository.data

import com.example.aquacontrol.data.flushing.local.FlushingRemotoDao
import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity
import com.example.aquacontrol.data.flushing.remote.aCopiaLocal
import retrofit2.HttpException
import java.io.IOException

// Informamos si el historial proviene del servidor o de una copia anterior.
data class ConsultaHistorialFlushing(
    val registros: List<FlushingRemotoEntity>,
    val desdeCopiaLocal: Boolean
)

// null indica que el servidor confirmó que el registro ya no existe.
data class ConsultaDetalleFlushing(
    val registro: FlushingRemotoEntity?,
    val desdeCopiaLocal: Boolean
)

// Consultamos el backend y conservamos una copia para lectura offline.
// No creamos operaciones pendientes ni mezclamos los registros antiguos.
class FlushingConsultaRepository(
    private val dao: FlushingRemotoDao,
    private val remoto: FlushingRemoteRepository,
    private val servidor: String
) {

    init {
        require(servidor.isNotBlank()) {
            "Debes indicar el servidor de origen."
        }
    }

    suspend fun obtenerPorLinea(
        lineaId: Long
    ): ConsultaHistorialFlushing {
        require(lineaId > 0) {
            "Debes seleccionar una línea válida."
        }

        // Solo recurrimos a la copia ante errores de comunicación.
        // Los errores de sesión o permisos se muestran como tales.
        val registrosRemotos = try {
            remoto.obtenerPorLinea(lineaId)
        } catch (error: IOException) {
            return consultarCopiaLocal(lineaId, error)
        }

        val momentoDescarga = System.currentTimeMillis()

        // Validamos la respuesta completa antes de modificar Room.
        val nuevasCopias = registrosRemotos.map { registro ->
            require(registro.lineaId == lineaId) {
                "El servidor devolvió un registro de otra línea."
            }

            registro.aCopiaLocal(
                servidor = servidor,
                descargadoEn = momentoDescarga
            )
        }

        require(nuevasCopias.map { it.id }.distinct().size == nuevasCopias.size) {
            "El servidor devolvió identificadores de flushing repetidos."
        }

        // Renovamos el historial en una sola transacción.
        // Una respuesta vacía confirmada también debe vaciar la copia.
        dao.reemplazarPorLinea(
            servidor = servidor,
            lineaId = lineaId,
            registros = nuevasCopias
        )

        return ConsultaHistorialFlushing(
            registros = dao.obtenerPorLinea(servidor, lineaId),
            desdeCopiaLocal = false
        )
    }

    // Consultamos el detalle con su ID remoto y comprobamos su línea.
    suspend fun obtenerDetalle(
        lineaId: Long,
        id: Long
    ): ConsultaDetalleFlushing {
        require(lineaId > 0 && id > 0) {
            "La línea y el registro deben tener identificadores válidos."
        }

        val registroRemoto = try {
            remoto.obtener(id)
        } catch (error: HttpException) {
            if (error.code() == 404) {
                // Si fue eliminado en el servidor, retiramos la copia
                // para no seguir mostrándolo como un registro disponible.
                dao.eliminar(servidor, id)

                return ConsultaDetalleFlushing(
                    registro = null,
                    desdeCopiaLocal = false
                )
            }
            throw error
        } catch (error: IOException) {
            val copia = dao.obtener(servidor, id)

            if (copia == null || copia.lineaId != lineaId) {
                throw IOException(
                    "No se pudo conectar y este registro no tiene " +
                            "una copia descargada disponible.",
                    error
                )
            }

            return ConsultaDetalleFlushing(
                registro = copia,
                desdeCopiaLocal = true
            )
        }

        require(registroRemoto.id == id && registroRemoto.lineaId == lineaId) {
            "El registro recibido no corresponde al detalle solicitado."
        }

        // Conservamos la referencia de la foto descargada.
        // La actualización de la imagen se gestiona por separado.
        val copiaAnterior = dao.obtener(servidor, id)
        val nuevaCopia = registroRemoto.aCopiaLocal(
            servidor = servidor,
            descargadoEn = System.currentTimeMillis(),
            fotoRuta = copiaAnterior?.fotoRuta
        )

        dao.guardar(nuevaCopia)

        return ConsultaDetalleFlushing(
            registro = nuevaCopia,
            desdeCopiaLocal = false
        )
    }

    private suspend fun consultarCopiaLocal(
        lineaId: Long,
        causa: IOException
    ): ConsultaHistorialFlushing {
        val registros = dao.obtenerPorLinea(servidor, lineaId)

        // Sin copias disponibles no afirmamos que el servidor esté vacío.
        if (registros.isEmpty()) {
            throw IOException(
                "No se pudo conectar con el servidor y no hay " +
                        "registros descargados para esta línea.",
                causa
            )
        }

        return ConsultaHistorialFlushing(
            registros = registros,
            desdeCopiaLocal = true
        )
    }
}