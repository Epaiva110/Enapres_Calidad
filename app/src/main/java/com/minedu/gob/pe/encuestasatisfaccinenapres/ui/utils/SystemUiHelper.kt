package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.utils

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import org.w3c.dom.Text


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

//Modifier.statusBarsPadding(): Añade espacio arriba para no chocar con el reloj/notificaciones.
//Modifier.navigationBarsPadding(): Añade espacio abajo para no quedar detrás de la barra de gestos o botones de "Atrás/Inicio".
//Modifier.safeDrawingPadding(): Aplica ambos automáticamente.

@Preview
@Composable
fun pantalla () {
    SetupMapSystemUI()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ){
        Text(
            text = "Erick Paiva",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .statusBarsPadding()
        )
    }
}
