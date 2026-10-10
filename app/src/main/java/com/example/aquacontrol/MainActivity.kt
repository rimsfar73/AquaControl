package com.example.aquacontrol

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.aquacontrol.iu.navigation.AppNavHost
import com.example.aquacontrol.notificaciones.NotificadorAlertas
import com.example.aquacontrol.ui.theme.AquaControlTheme

class MainActivity : ComponentActivity() {

    private var alertaPendiente by mutableStateOf<Pair<Int, Long>?>(null)

    private var avisoProcesado = false

    private val solicitarPermisoNotificaciones =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedido ->
            if (!concedido) {
                Toast.makeText(
                    this,
                    "Puedes activar las notificaciones desde los ajustes del celular.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        avisoProcesado = savedInstanceState?.getBoolean(
            CLAVE_AVISO_PROCESADO,
            false
        ) ?: false

        if (!avisoProcesado) {
            recibirAlerta(intent)
        }

        setContent {
            AquaControlTheme {
                val pendiente = alertaPendiente

                AppNavHost(
                    lineaIdNotificacion = pendiente?.first,
                    medicionIdNotificacion = pendiente?.second,
                    onNotificacionAbierta = {
                        alertaPendiente = null
                        avisoProcesado = true
                    }
                )
            }
        }

        comprobarPermisoNotificaciones()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        avisoProcesado = false
        recibirAlerta(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(
            CLAVE_AVISO_PROCESADO,
            avisoProcesado
        )

        super.onSaveInstanceState(outState)
    }

    private fun recibirAlerta(intent: Intent?) {
        if (intent?.action != NotificadorAlertas.ACCION_ABRIR_ALERTA) {
            return
        }

        val lineaId = intent.getIntExtra(
            NotificadorAlertas.EXTRA_LINEA_ID,
            -1
        )

        val medicionId = intent.getLongExtra(
            NotificadorAlertas.EXTRA_MEDICION_ID,
            -1L
        )

        if (lineaId <= 0 || medicionId <= 0L) {
            return
        }

        alertaPendiente = lineaId to medicionId
    }

    private fun comprobarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permisoConcedido = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (permisoConcedido) {
            return
        }

        val preferencias = getSharedPreferences(
            "permisos_aquacontrol",
            MODE_PRIVATE
        )

        val solicitudRealizada = preferencias.getBoolean(
            "notificaciones_solicitadas",
            false
        )

        if (solicitudRealizada) {
            return
        }

        preferencias.edit()
            .putBoolean("notificaciones_solicitadas", true)
            .apply()

        solicitarPermisoNotificaciones.launch(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    companion object {
        private const val CLAVE_AVISO_PROCESADO =
            "aviso_notificacion_procesado"
    }
}