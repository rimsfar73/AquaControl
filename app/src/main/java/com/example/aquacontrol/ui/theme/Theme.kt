package com.example.aquacontrol.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFB32624),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE5E1),
    onPrimaryContainer = Color(0xFF6D1716),

    secondary = Color(0xFF49616C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4EDF0),
    onSecondaryContainer = Color(0xFF263D47),

    tertiary = Color(0xFF52634D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE5EDDF),
    onTertiaryContainer = Color(0xFF303E2C),

    background = Color(0xFFF7F7F3),
    onBackground = Color(0xFF252A2D),

    surface = Color(0xFFFCFCF9),
    onSurface = Color(0xFF252A2D),
    surfaceVariant = Color(0xFFEEEFEA),
    onSurfaceVariant = Color(0xFF4E5558),
    surfaceTint = Color(0xFFB32624),

    outline = Color(0xFF737B7D),
    outlineVariant = Color(0xFFCDD2CE),

    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFFCE5E2),
    onErrorContainer = Color(0xFF781D18),

    inverseSurface = Color(0xFF303638),
    inverseOnSurface = Color(0xFFF1F3EE),
    inversePrimary = Color(0xFFFFB4AA),

    scrim = Color.Black,

    surfaceDim = Color(0xFFDDDFD9),
    surfaceBright = Color(0xFFFCFCF9),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F6F1),
    surfaceContainer = Color(0xFFEEEFEA),
    surfaceContainerHigh = Color(0xFFE8EAE4),
    surfaceContainerHighest = Color(0xFFE2E4DE)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4AA),
    onPrimary = Color(0xFF650E0C),
    primaryContainer = Color(0xFF713330),
    onPrimaryContainer = Color(0xFFFFDAD5),

    secondary = Color(0xFFB1CBD5),
    onSecondary = Color(0xFF19333D),
    secondaryContainer = Color(0xFF344C56),
    onSecondaryContainer = Color(0xFFD5EAF2),

    tertiary = Color(0xFFBACDB0),
    onTertiary = Color(0xFF263521),
    tertiaryContainer = Color(0xFF3D5036),
    onTertiaryContainer = Color(0xFFDDEAD4),

    background = Color(0xFF171C1F),
    onBackground = Color(0xFFE5E9E7),

    surface = Color(0xFF1B2124),
    onSurface = Color(0xFFE5E9E7),
    surfaceVariant = Color(0xFF343E42),
    onSurfaceVariant = Color(0xFFC1CBCB),
    surfaceTint = Color(0xFFFFB4AA),

    outline = Color(0xFF8B9798),
    outlineVariant = Color(0xFF465255),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF65302D),
    onErrorContainer = Color(0xFFFFDAD6),

    inverseSurface = Color(0xFFE5E9E7),
    inverseOnSurface = Color(0xFF293033),
    inversePrimary = Color(0xFFB32624),

    scrim = Color.Black,

    surfaceDim = Color(0xFF121719),
    surfaceBright = Color(0xFF363E41),
    surfaceContainerLowest = Color(0xFF101517),
    surfaceContainerLow = Color(0xFF1B2124),
    surfaceContainer = Color(0xFF20282B),
    surfaceContainerHigh = Color(0xFF293235),
    surfaceContainerHighest = Color(0xFF343E42)
)

@Composable
fun AquaControlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}