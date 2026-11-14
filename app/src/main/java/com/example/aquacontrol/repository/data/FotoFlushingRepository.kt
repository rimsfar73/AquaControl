package com.example.aquacontrol.repository.data

import android.content.Context
import android.graphics.BitmapFactory
import com.example.aquacontrol.data.flushing.FotosFlushing
import com.example.aquacontrol.data.flushing.local.FlushingRemotoDao
import com.example.aquacontrol.data.flushing.local.FlushingRemotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed interface ResultadoFotoFlushing {

    data class Disponible(
        val nombreArchivo: String,
        val desdeCopiaLocal: Boolean
    ) : ResultadoFotoFlushing

    // El servidor confirmó que el registro no tiene una foto disponible.
    data object SinFoto : ResultadoFotoFlushing

    // No tenemos conexión ni una imagen descargada para mostrar.
    data object SinCopiaLocal : ResultadoFotoFlushing
}

class FotoFlushingRepository(
    context: Context,
    private val dao: FlushingRemotoDao,
    private val remoto: FlushingRemoteRepository,
    private val servidor: String
) {
    private val appContext = context.applicationContext

    init {
        require(servidor.isNotBlank()) {
            "Se necesita la dirección del servidor."
        }
    }

    suspend fun consultar(
        registro: FlushingRemotoEntity,
        soloCopiaLocal: Boolean = false
    ): ResultadoFotoFlushing = withContext(Dispatchers.IO) {
        require(registro.servidor == servidor && registro.id > 0) {
            "El registro no corresponde al servidor seleccionado."
        }

        // Consultamos la referencia más reciente de la copia remota.
        val copia = dao.obtener(servidor, registro.id)
        val nombreAnterior = copia?.fotoRuta

        if (soloCopiaLocal) {
            return@withContext consultarCopia(nombreAnterior)
        }

        // Solo recurrimos a la copia ante un problema de conexión.
        // Los errores de autenticación y permisos llegan al ViewModel.
        val bytes = try {
            remoto.obtenerFoto(registro.id)
        } catch (e: HttpException) {
            if (e.code() == 404) {
                dao.actualizarFoto(servidor, registro.id, null)
                return@withContext ResultadoFotoFlushing.SinFoto
            }
            throw e
        } catch (e: IOException) {
            return@withContext consultarCopia(nombreAnterior)
        }

        validarImagen(bytes)

        // Escribimos un archivo nuevo para no dañar la copia anterior
        // si se interrumpe la descarga o falla el almacenamiento.
        val archivo = FotosFlushing.crearArchivo(appContext)

        try {
            archivo.outputStream().use { salida ->
                salida.write(bytes)
            }
        } catch (e: IOException) {
            FotosFlushing.eliminarFotoNoGuardada(appContext, archivo.name)
            throw IOException(
                "No se pudo guardar la copia de la foto en el celular.",
                e
            )
        }

        // Asociamos el archivo únicamente con este registro remoto.
        dao.actualizarFoto(
            servidor = servidor,
            id = registro.id,
            fotoRuta = archivo.name
        )

        ResultadoFotoFlushing.Disponible(
            nombreArchivo = archivo.name,
            desdeCopiaLocal = false
        )
    }

    private fun consultarCopia(
        nombreArchivo: String?
    ): ResultadoFotoFlushing {
        return if (
            nombreArchivo != null &&
            FotosFlushing.esFotoValida(appContext, nombreArchivo)
        ) {
            ResultadoFotoFlushing.Disponible(
                nombreArchivo = nombreArchivo,
                desdeCopiaLocal = true
            )
        } else {
            ResultadoFotoFlushing.SinCopiaLocal
        }
    }

    private fun validarImagen(bytes: ByteArray) {
        require(bytes.isNotEmpty() && bytes.size <= 5 * 1024 * 1024) {
            "La foto descargada está vacía o supera el tamaño permitido."
        }

        val opciones = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)

        require(
            opciones.outWidth > 0 &&
                    opciones.outHeight > 0 &&
                    opciones.outWidth.toLong() * opciones.outHeight.toLong() <=
                    25_000_000L
        ) {
            "La imagen descargada no es válida o supera el tamaño permitido."
        }

        require(
            opciones.outMimeType == "image/jpeg" ||
                    opciones.outMimeType == "image/png"
        ) {
            "La foto descargada debe ser JPEG o PNG."
        }
    }
}