package com.minedu.gob.pe.enaprescalidad.utils

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

@Composable
fun SetupMapSystemUI(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    allowRotation: Boolean = false
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context as? ComponentActivity ?: return

    // Gestionamos la orientación con un ciclo de vida completo
    DisposableEffect(allowRotation) {
        activity.requestedOrientation = if (allowRotation) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // ESTO ES LO QUE FALTA: Resetear al salir
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Configuración visual (Borde a borde e iconos)
    SideEffect {
        activity.enableEdgeToEdge()
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, view)

        // Si el tema es oscuro, los iconos deben ser claros (false)
        // Si el tema es claro, los iconos deben ser oscuros (true)
        controller.isAppearanceLightStatusBars = !isDarkTheme
        controller.isAppearanceLightNavigationBars = !isDarkTheme
    }
}
