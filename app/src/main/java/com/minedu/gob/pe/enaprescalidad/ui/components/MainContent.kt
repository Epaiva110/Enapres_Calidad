// ── Cambio en MainContent.kt ──────────────────────────────────────────────────
//
// Antes:
//   "CargaMarco" -> UpdateScreen(
//       isSyncing = false,
//       onSyncAllData = { },
//       onSyncGroup = {},
//       onSyncIndividual = {}
//   )
//
// Después:
//   "CargaMarco" -> UpdateScreen()
//
// El nuevo UpdateScreen ya obtiene su propio ViewModel internamente,
// por lo que MainContent no necesita pasar ningún parámetro.
//
// ─────────────────────────────────────────────────────────────────────────────

package com.minedu.gob.pe.enaprescalidad.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.minedu.gob.pe.enaprescalidad.ui.navigation.Routes
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.Maintance.MaintanceScren
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.analytics.AnalyticsScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.home.HomeScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.settings.SettingsScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update.UpdateScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.verification.conglomerado.ConglomeradoScreen

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
//@Composable
//fun MainContent(
//    selectedItemId: String,
//    modifier: Modifier = Modifier,
//) {
//    Box(modifier = modifier) {
//        AnimatedContent(
//            targetState = selectedItemId,
//            label = "main_content_transition",
//            transitionSpec = {
//                (fadeIn(tween(220, delayMillis = 60)) +
//                        scaleIn(initialScale = 0.96f, animationSpec = tween(220, delayMillis = 60)))
//                    .togetherWith(fadeOut(tween(90)))
//            },
//        ) { itemId ->
//            when (itemId) {
//                "home"       -> HomeScreen()
//                "analytics"  -> AnalyticsScreen()
//                "settings"   -> SettingsScreen()
//                // Sin parámetros — el ViewModel lo maneja todo internamente
//                "CargaMarco" -> UpdateScreen()
//                "verificacionConglomerado" -> ConglomeradoScreen({})
//                else         -> MaintanceScren(Routes.Login)
//            }
//        }
//    }
//}
//
//sealed class DrawerScreen(val id: String) {
//    object Home       : DrawerScreen("home")
//    object Analytics  : DrawerScreen("analytics")
//    object Settings   : DrawerScreen("settings")
//    object CargaMarco : DrawerScreen("CargaMarco")
//}


// ─────────────────────────────────────────────────────────────────────────────
//  MainContent.kt  — versión corregida
//
//  CAMBIO CLAVE: agregar key(itemId) { ... } dentro del AnimatedContent.
//
//  Qué hace key():
//   - Cuando itemId cambia (usuario cambia de menú), Compose destruye y recrea
//     el subárbol completo, incluyendo los hiltViewModel() de cada pantalla.
//   - Cuando solo rota la pantalla, itemId NO cambia → el subárbol se conserva
//     → el ViewModel sobrevive con su SavedStateHandle intacto.
//
//  Resultado:
//   ✓ Cambiar de menú  → ViewModel se destruye → filtros se reinician
//   ✓ Rotar pantalla   → ViewModel sobrevive   → filtros se mantienen
// ─────────────────────────────────────────────────────────────────────────────


import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.minedu.gob.pe.enaprescalidad.surveys.ui.SurveyScreen
import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoViewModel
import com.minedu.gob.pe.enaprescalidad.ui.components.maps.MapScreen

// Datos de navegación hacia el survey de conglomerado
data class SurveyNavState(
    val muestraId: Int,
    val soloLectura: Boolean,
)

private val SurveyNavStateSaver = Saver<SurveyNavState?, List<Any?>>(
    save = { state ->
        if (state == null) emptyList()
        else listOf(state.muestraId, state.soloLectura)
    },
    restore = { saved ->
        if (saved.isEmpty()) null
        else SurveyNavState(
            muestraId = saved[0] as Int,
            soloLectura = saved[1] as Boolean,
        )
    },
)

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MainContent(
    selectedItemId: String,
    modifier: Modifier = Modifier,
    onSurveyActiveChange: (Boolean) -> Unit = {},
    onSurveyClosed: () -> Unit = {},
) {
    val conglomeradoViewModel: ConglomeradoViewModel = hiltViewModel()
    val context = LocalContext.current

    // JSON de la encuesta de conglomerado (se lee una sola vez)
    val jsonConglomerado = remember {
        context.assets.open("survey_conglomerado.json").bufferedReader().use { it.readText() }
    }

    // Estado de navegación interna hacia el survey de conglomerado.
    // null = mostrar lista de conglomerados; non-null = mostrar survey.
    // rememberSaveable: conserva la encuesta abierta al rotar la pantalla
    var surveyNav by rememberSaveable(
        inputs = arrayOf(selectedItemId),
        stateSaver = SurveyNavStateSaver,
    ) { mutableStateOf<SurveyNavState?>(null) }

    LaunchedEffect(surveyNav, selectedItemId) {
        onSurveyActiveChange(
            selectedItemId == NavIds.CONGLOMERADO && surveyNav != null
        )
    }

    Box(modifier = modifier) {
        AnimatedContent(
            targetState = selectedItemId,
            label = "main_content_transition",
            transitionSpec = {
                (fadeIn(tween(220, delayMillis = 60)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(220, delayMillis = 60)))
                    .togetherWith(fadeOut(tween(90)))
            },
        ) { itemId ->

            key(itemId) {
                when (itemId) {
                    NavIds.HOME        -> HomeScreen()
                    NavIds.ANALYTICS   -> AnalyticsScreen()
                    NavIds.SETTINGS    -> SettingsScreen()
                    NavIds.CARGA_MARCO -> UpdateScreen()

                    NavIds.CONGLOMERADO -> {
                        val nav = surveyNav
                        if (nav == null) {
                            // ── Lista de conglomerados ──────────────────────
                            ConglomeradoScreen(
                                onNavigateCuestionario = { muestraId ->
                                    surveyNav = SurveyNavState(muestraId, soloLectura = false)
                                },
                                onLeerCuestionario = { muestraId ->
                                    surveyNav = SurveyNavState(muestraId, soloLectura = true)
                                }
                            )
                        } else {
                            // ── Survey del conglomerado (pantalla propia, sin TopBar principal) ──
                            key(nav.muestraId, nav.soloLectura) {
                                SurveyScreen(
                                    muestraId = nav.muestraId,
                                    jsonString = jsonConglomerado,
                                    soloLectura = nav.soloLectura,
                                    onNavigateBack = {
                                        surveyNav = null
                                        conglomeradoViewModel.refreshProgresoEncuestas()
                                        onSurveyClosed()
                                    },
                                )
                            }
                        }
                    }

                    NavIds.VIVIENDA     -> SurveyScreen(101, jsonConglomerado, onNavigateBack = {})
                    NavIds.REENTREVISTA -> MapScreen()
                    else                -> MaintanceScren(Routes.Login)
                }
            }
        }
    }
}

// Con key(), el onCleared() del ViewModel se llama automáticamente al cambiar de menú,
// así que el savedState.remove() que tienes en onCleared() funciona perfectamente
// sin necesidad de llamar a resetModoSeleccion() desde la Screen.

object NavIds {
    const val HOME                      = "home"
    const val ANALYTICS                 = "analytics"
    const val SETTINGS                  = "settings"
    const val CARGA_MARCO               = "CargaMarco"
    const val CONGLOMERADO              = "verificacionConglomerado"
    const val VIVIENDA                  = "verificacionVivienda"
    const val REENTREVISTA              = "verificacionReentrevista"
}