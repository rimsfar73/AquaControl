package com.example.aquacontrol.viewmodel.flushing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aquacontrol.data.flushing.remote.PreparadorFotoFlushing
import com.example.aquacontrol.repository.data.FlushingRemoteRepository

class RegistrarFlushingViewModelFactory(
    context: Context,
    private val repository: FlushingRemoteRepository
) : ViewModelProvider.Factory {

    // Usamos el contexto de la aplicación para no retener la pantalla.
    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrarFlushingViewModel::class.java)) {

            // Preparamos el archivo cuando el usuario solicita guardarlo.
            val viewModel = RegistrarFlushingViewModel(
                repository = repository,
                prepararFoto = { nombreArchivo ->
                    PreparadorFotoFlushing.preparar(
                        context = appContext,
                        nombreArchivo = nombreArchivo
                    )
                }
            )

            @Suppress("UNCHECKED_CAST")
            return viewModel as T
        }

        throw IllegalArgumentException(
            "ViewModel no reconocido: ${modelClass.name}"
        )
    }
}