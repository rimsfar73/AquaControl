package com.example.aquacontrol.iu.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aquacontrol.model.estado.EstadoLinea
import com.example.aquacontrol.ui.theme.*

@Composable
fun IndicadorEstado(
    estado: EstadoLinea?,
    modifier: Modifier = Modifier,
    destacado: Boolean = false
) {
    val temaOscuro =
        MaterialTheme.colorScheme.background.luminance() < 0.5f

    val fondo = when (estado) {
        EstadoLinea.NORMAL ->
            if (temaOscuro) FondoNormalOscuro else FondoNormalClaro

        EstadoLinea.ADVERTENCIA ->
            if (temaOscuro) FondoAdvertenciaOscuro else FondoAdvertenciaClaro

        EstadoLinea.CRITICO ->
            if (temaOscuro) FondoCriticoOscuro else FondoCriticoClaro

        null ->
            if (temaOscuro) FondoSinMedicionesOscuro else FondoSinMedicionesClaro
    }

    val contenido = when (estado) {
        EstadoLinea.NORMAL ->
            if (temaOscuro) ContenidoNormalOscuro else ContenidoNormalClaro

        EstadoLinea.ADVERTENCIA ->
            if (temaOscuro) ContenidoAdvertenciaOscuro else ContenidoAdvertenciaClaro

        EstadoLinea.CRITICO ->
            if (temaOscuro) ContenidoCriticoOscuro else ContenidoCriticoClaro

        null ->
            if (temaOscuro) ContenidoSinMedicionesOscuro else ContenidoSinMedicionesClaro
    }

    val texto = when (estado) {
        EstadoLinea.NORMAL -> "Normal"
        EstadoLinea.ADVERTENCIA -> "Advertencia"
        EstadoLinea.CRITICO -> "Crítico"
        null -> "Sin mediciones"
    }

    val icono = when (estado) {
        EstadoLinea.NORMAL -> Icons.Default.CheckCircle
        EstadoLinea.ADVERTENCIA -> Icons.Default.Warning
        EstadoLinea.CRITICO -> Icons.Default.Error
        null -> Icons.Default.Info
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = fondo,
        contentColor = contenido
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (destacado) 16.dp else 12.dp,
                vertical = if (destacado) 12.dp else 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(
                    if (destacado) 28.dp else 20.dp
                )
            )

            Text(
                text = texto,
                style = if (destacado) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.bodyMedium
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}