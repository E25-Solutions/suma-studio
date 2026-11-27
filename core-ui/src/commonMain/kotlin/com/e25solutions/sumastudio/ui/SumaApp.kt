package com.e25solutions.sumastudio.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

/**
 * Punto de entrada compartido de la interfaz para Android e iOS.
 *
 * @param colorScheme colores de la plataforma (en Android 12+, los dinámicos de Material You).
 *   Si es nulo, se usa la paleta propia en modo claro u oscuro según el sistema.
 */
@Composable
fun SumaApp(colorScheme: ColorScheme? = null) {
    val dark = isSystemInDarkTheme()
    val scheme = colorScheme ?: if (dark) SumaDarkColors else SumaLightColors
    MaterialTheme(colorScheme = scheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            CalculatorScreen()
        }
    }
}

// Paleta neutra y sobria para cuando no hay colores dinámicos.
private val SumaLightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1F5F57),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFDCE7E4),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF1A2B28),
)

private val SumaDarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF8DD0C5),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF003731),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF2E3B39),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFDCE7E4),
)
