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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyCatalog
import com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyContext
import com.minedu.gob.pe.enaprescalidad.surveys.ui.SurveyScreen
import com.minedu.gob.pe.enaprescalidad.ui.navigation.Routes
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.Maintance.MaintanceScren
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.analytics.AnalyticsScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.home.HomeScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.settings.SettingsScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update.UpdateScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.verification.conglomerado.ConglomeradoScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.verification.reentrevista.ReentrevistaScreen
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.verification.vivienda.ViviendaScreen
import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  NAV IDS
// ─────────────────────────────────────────────────────────────────────────────

object NavIds {
    const val HOME         = "home"
    const val ANALYTICS    = "analytics"
    const val SETTINGS     = "settings"
    const val CARGA_MARCO  = "CargaMarco"
    const val CONGLOMERADO = "verificacionConglomerado"
    const val VIVIENDA     = "verificacionVivienda"
    const val REENTREVISTA = "verificacionReentrevista"
}

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY NAV STATE — qué survey está abierto y quién lo responde
// ─────────────────────────────────────────────────────────────────────────────

data class SurveyNavState(
    val context: SurveyContext,
    val soloLectura: Boolean,
)

private val SurveyNavStateSaver = Saver<SurveyNavState?, List<Any?>>(
    save    = { state ->
        if (state == null) emptyList()
        else listOf(state.context.contextKey, state.context.surveyType.name, state.soloLectura)
    },
    restore = { saved ->
        if (saved.isEmpty()) null
        else {
            val ctx = SurveyContext.fromKey(saved[0] as String) ?: return@Saver null
            SurveyNavState(ctx, saved[2] as Boolean)
        }
    },
)

// ─────────────────────────────────────────────────────────────────────────────
//  MAIN CONTENT
// ─────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MainContent(
    surveyCatalog: SurveyCatalog,
    selectedItemId: String,
    modifier: Modifier = Modifier,
    onSurveyActiveChange: (Boolean) -> Unit = {},
    onSurveyClosed: () -> Unit = {},
) {
    val conglomeradoViewModel: ConglomeradoViewModel = hiltViewModel()

    var surveyNav by rememberSaveable(
        inputs     = arrayOf(selectedItemId),
        stateSaver = SurveyNavStateSaver,
    ) { mutableStateOf<SurveyNavState?>(null) }

    LaunchedEffect(surveyNav, selectedItemId) {
        onSurveyActiveChange(surveyNav != null)
    }

    Box(modifier = modifier) {
        AnimatedContent(
            targetState  = selectedItemId,
            label        = "main_content_transition",
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

                    NavIds.CONGLOMERADO -> SurveyNavHost(
                        surveyNav  = surveyNav,
                        onBack     = {
                            surveyNav = null
                            conglomeradoViewModel.refreshProgresoEncuestas()
                            onSurveyClosed()
                        },
                        listContent = {
                            ConglomeradoScreen(
                                onNavigateCuestionario = { muestraId ->
                                    surveyNav = SurveyNavState(
                                        SurveyContext.Conglomerado(muestraId), false
                                    )
                                },
                                onLeerCuestionario = { muestraId ->
                                    surveyNav = SurveyNavState(
                                        SurveyContext.Conglomerado(muestraId), true
                                    )
                                },
                            )
                        },
                    )

                    NavIds.VIVIENDA -> SurveyNavHost(
                        surveyNav   = surveyNav,
                        onBack      = { surveyNav = null; onSurveyClosed() },
                        listContent = {
                            ViviendaScreen(
                                onNavigateCuestionario = { viviendaId, muestraId, orden ->
                                    surveyNav = SurveyNavState(
                                        SurveyContext.Vivienda(viviendaId, muestraId, orden), false
                                    )
                                },
                            )
                        },
                    )

                    NavIds.REENTREVISTA -> SurveyNavHost(
                        surveyNav   = surveyNav,
                        onBack      = { surveyNav = null; onSurveyClosed() },
                        listContent = {
                            ReentrevistaScreen(
                                onNavigateCuestionario = { personaId, hogarId, orden, nombre ->
                                    surveyNav = SurveyNavState(
                                        SurveyContext.Persona(personaId, hogarId, orden, nombre), false
                                    )
                                },
                            )
                        },
                    )

                    else -> MaintanceScren(Routes.Login)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY NAV HOST
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SurveyNavHost(
    surveyNav: SurveyNavState?,
    onBack: () -> Unit,
    listContent: @Composable () -> Unit,
) {
    if (surveyNav == null) {
        listContent()
    } else {
        key(surveyNav.context.contextKey, surveyNav.soloLectura) {
            SurveyScreen(
                surveyContext  = surveyNav.context,
                soloLectura    = surveyNav.soloLectura,
                onNavigateBack = onBack,
            )
        }
    }
}