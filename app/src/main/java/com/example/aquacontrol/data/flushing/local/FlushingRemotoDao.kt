package com.example.aquacontrol.data.flushing.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

// Consultamos y actualizamos únicamente la copia de datos del servidor.
// No modificamos la tabla de registros locales antiguos.
@Dao
abstract class FlushingRemotoDao {

    // Mostramos los registros descargados de una línea.
    @Query(
        """
        SELECT * FROM flushing_remoto
        WHERE servidor = :servidor AND lineaId = :lineaId
        ORDER BY fechaHora DESC, id DESC
        """
    )
    abstract suspend fun obtenerPorLinea(
        servidor: String,
        lineaId: Long
    ): List<FlushingRemotoEntity>

    // Buscamos un registro usando su servidor y su ID remoto.
    @Query(
        """
        SELECT * FROM flushing_remoto
        WHERE servidor = :servidor AND id = :id
        LIMIT 1
        """
    )
    abstract suspend fun obtener(
        servidor: String,
        id: Long
    ): FlushingRemotoEntity?

    // Guardamos la versión confirmada por el backend.
    // Si ya existe, reemplazamos su copia con los datos recibidos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardar(
        registro: FlushingRemotoEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun guardarTodos(
        registros: List<FlushingRemotoEntity>
    )

    // Quitamos una copia después de confirmar la eliminación remota.
    @Query(
        """
        DELETE FROM flushing_remoto
        WHERE servidor = :servidor AND id = :id
        """
    )
    abstract suspend fun eliminar(
        servidor: String,
        id: Long
    )

    // Limpiamos solo la copia de la línea que vamos a actualizar.
    @Query(
        """
        DELETE FROM flushing_remoto
        WHERE servidor = :servidor AND lineaId = :lineaId
        """
    )
    abstract suspend fun eliminarPorLinea(
        servidor: String,
        lineaId: Long
    )

    // Actualizamos la referencia local después de descargar una foto.
    // null permite retirar la referencia si el servidor confirma que no existe.
    @Query(
        """
        UPDATE flushing_remoto
        SET fotoRuta = :fotoRuta
        WHERE servidor = :servidor AND id = :id
        """
    )
    abstract suspend fun actualizarFoto(
        servidor: String,
        id: Long,
        fotoRuta: String?
    )

    // Sustituimos el historial únicamente tras recibir la lista completa
    // del backend. Si falla la consulta remota, no llamamos a este método.
    @Transaction
    open suspend fun reemplazarPorLinea(
        servidor: String,
        lineaId: Long,
        registros: List<FlushingRemotoEntity>
    ) {
        require(
            registros.all {
                it.servidor == servidor && it.lineaId == lineaId
            }
        ) {
            "Los registros no corresponden al servidor y línea solicitados."
        }

        // Conservamos las fotos ya descargadas de los registros existentes.
        // Su actualización se gestiona por separado al consultar la evidencia.
        val anteriores = obtenerPorLinea(servidor, lineaId)
            .associateBy { it.id }

        val actualizados = registros.map { registro ->
            registro.copy(
                fotoRuta = registro.fotoRuta
                    ?: anteriores[registro.id]?.fotoRuta
            )
        }

        // Room ejecuta ambos pasos juntos: si falla, conserva la copia anterior.
        // Una respuesta vacía del servidor también debe vaciar este historial.
        eliminarPorLinea(servidor, lineaId)
        guardarTodos(actualizados)
    }
}