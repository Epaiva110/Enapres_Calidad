package com.minedu.gob.pe.enaprescalidad.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.minedu.gob.pe.enaprescalidad.ui.navigation.Routes
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.Maintance.MaintanceScren
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.analytics.AnalyticsScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.home.HomeScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.settings.SettingsScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update.UpdateScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update.UpdateScreenPreview

//import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update.UpdateScreenPreview


/**
 * Área de contenido principal.
 *
 * Renderiza la pantalla correspondiente al ítem seleccionado
 * con una transición animada.
 *
 * Para agregar una pantalla nueva:
 *  1. Agrega su id en SidebarRepositoryImpl.
 *  2. Crea su Composable en ui/features/
 *  3. Agrega el `else if` aquí.
 */
@Composable
fun MainContent(
    selectedItemId: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = selectedItemId,
            label = "main_content_transition",
            transitionSpec = {
                (fadeIn(tween(220, delayMillis = 60)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(220, delayMillis = 60)))
                    .togetherWith(fadeOut(tween(90)))
            }
        ) { itemId ->
            when (itemId) {
                "home"      -> HomeScreen()
                "analytics" -> AnalyticsScreen()
                "settings"  -> SettingsScreen()
                //"CargaMarco"    -> UpdateScreen(userId = "ADM")
                "CargaMarco" -> UpdateScreenPreview()
                else        -> MaintanceScren (Routes.Login)
            }
        }
    }
}

sealed class DrawerScreen(val id: String) {
    object Home : DrawerScreen("home")
    object Analytics : DrawerScreen("analytics")
    object Settings : DrawerScreen("settings")
    object CargaMarco : DrawerScreen("CargaMarco")
}