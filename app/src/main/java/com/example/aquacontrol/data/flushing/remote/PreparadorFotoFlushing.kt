package com.example.aquacontrol.data.flushing.remote

import android.content.Context
import android.graphics.BitmapFactory
import com.example.aquacontrol.data.flushing.FotosFlushing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

// Preparamos la evidencia del celular para enviarla a FotoController.
// Conservamos la fotografía original sin modificarla ni eliminarla.
object PreparadorFotoFlushing {

    // Aplicamos los mismos límites que FotoService del backend.
    private const val MAX_BYTES = 5L * 1024 * 1024
    private const val MAX_PIXELES = 25_000_000L

    // Revisamos el archivo fuera del hilo de la interfaz.
    suspend fun preparar(
        context: Context,
        nombreArchivo: String
    ): MultipartBody.Part = withContext(Dispatchers.IO) {

        // Buscamos la foto dentro de la carpeta privada de evidencias.
        val archivo = requireNotNull(
            FotosFlushing.obtenerArchivo(context, nombreArchivo)
        ) {
            "No se encontró la foto de evidencia."
        }

        require(archivo.canRead() && archivo.length() > 0) {
            "La foto está vacía o no se puede leer."
        }

        require(archivo.length() <= MAX_BYTES) {
            "La foto supera los 5 MiB permitidos."
        }

        // Leemos dimensiones y formato sin cargar la imagen completa.
        val opciones = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeFile(archivo.absolutePath, opciones)

        val ancho = opciones.outWidth
        val alto = opciones.outHeight

        require(
            ancho > 0 &&
                    alto > 0 &&
                    ancho.toLong() * alto.toLong() <= MAX_PIXELES
        ) {
            "La foto no es válida o supera los 25 megapíxeles."
        }

        // Comprobamos el formato detectado en el contenido de la imagen.
        val tipoContenido = when (opciones.outMimeType) {
            "image/jpeg" -> "image/jpeg"
            "image/png" -> "image/png"
            else -> throw IllegalArgumentException(
                "La foto debe tener formato JPEG o PNG."
            )
        }

        // Utilizamos los métodos compatibles con OkHttp 3 del proyecto.
        val tipo = requireNotNull(MediaType.parse(tipoContenido)) {
            "No se pudo determinar el formato de la foto."
        }
        val cuerpo = RequestBody.create(tipo, archivo)

        // "foto" coincide con el nombre que recibe FotoController.
        // OkHttp leerá el archivo al ejecutar la solicitud.
        MultipartBody.Part.createFormData(
            "foto",
            archivo.name,
            cuerpo
        )
    }
}