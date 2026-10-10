package com.example.aquacontrol.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.aquacontrol.MainActivity
import com.example.aquacontrol.R
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.model.temperatura.MedicionTemperatura

class NotificadorAlertas(context: Context) {

    private val appContext = context.applicationContext

    fun crearCanales() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = appContext.getSystemService(
            NotificationManager::class.java
        )

        val advertencias = NotificationChannel(
            CANAL_ADVERTENCIAS,
            "Advertencias de temperatura",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos cuando una línea entra en advertencia."
        }

        val criticas = NotificationChannel(
            CANAL_CRITICAS,
            "Alertas críticas de temperatura",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos cuando una línea alcanza un estado crítico."
            enableVibration(true)
        }

        manager.createNotificationChannels(
            listOf(advertencias, criticas)
        )
    }

    fun notificarCambio(
        medicion: MedicionTemperatura,
        estadoAnterior: EstadoLinea?,
        ubicacion: String
    ) {
        val estadoActual = medicion.estado

        // No se notifican estados normales ni lecturas del mismo estado.
        if (
            estadoActual == EstadoLinea.NORMAL ||
            estadoActual == estadoAnterior
        ) {
            return
        }

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = NotificationManagerCompat.from(appContext)

        if (!manager.areNotificationsEnabled()) {
            return
        }

        crearCanales()

        val esCritica = estadoActual == EstadoLinea.CRITICO
        val canal = if (esCritica) {
            CANAL_CRITICAS
        } else {
            CANAL_ADVERTENCIAS
        }

        val titulo = if (esCritica) {
            "Crítico: ${medicion.temperatura} °C"
        } else {
            "Advertencia: ${medicion.temperatura} °C"
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            action = ACCION_ABRIR_ALERTA

            // Cada medición tiene un destino identificable.
            data = Uri.Builder()
                .scheme("aquacontrol")
                .authority("alerta")
                .appendPath(medicion.id.toString())
                .build()

            putExtra(EXTRA_LINEA_ID, medicion.lineaId)
            putExtra(EXTRA_MEDICION_ID, medicion.id)

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val abrirAlerta = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(appContext, canal)
            .setSmallIcon(R.drawable.ic_notificacion_alerta)
            .setContentTitle(titulo)
            .setContentText(ubicacion)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(ubicacion)
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(
                if (esCritica) {
                    NotificationCompat.PRIORITY_HIGH
                } else {
                    NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(abrirAlerta)
            .setAutoCancel(true)
            .setWhen(medicion.fechaHora)
            .setShowWhen(true)
            .build()

        try {
            // Mantiene un aviso visible por línea.
            // Un nuevo cambio de estado reemplaza el aviso anterior.
            manager.notify(
                "temperatura_linea_${medicion.lineaId}",
                0,
                notificacion
            )
        } catch (e: SecurityException) {
            // El permiso puede cambiar mientras se prepara el aviso.
            Log.w(
                "NotificadorAlertas",
                "No se pudo mostrar la notificación por falta de permiso.",
                e
            )
        }
    }

    companion object {
        private const val CANAL_ADVERTENCIAS =
            "temperatura_advertencias"

        private const val CANAL_CRITICAS =
            "temperatura_criticas"

        const val ACCION_ABRIR_ALERTA =
            "com.example.aquacontrol.ABRIR_ALERTA"

        const val EXTRA_LINEA_ID = "alerta_linea_id"
        const val EXTRA_MEDICION_ID = "alerta_medicion_id"
    }
}