package com.minedu.gob.pe.enaprescalidad.surveys.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConstraintResult
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyContext
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.page.SurveyPage
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question.ObservacionDialog
import com.minedu.gob.pe.enaprescalidad.surveys.viewmodel.SurveyViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY SCREEN
//
//  Recibe un SurveyContext que identifica exactamente qué unidad se responde.
//  El JSON se resuelve internamente via SurveyVersionRepository.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurveyScreen(
    surveyContext: SurveyContext,
    onNavigateBack: () -> Unit,
    soloLectura: Boolean = false,
    viewModel: SurveyViewModel = hiltViewModel(),
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState  = remember { SnackbarHostState() }

    LaunchedEffect(surveyContext) { viewModel.init(surveyContext) }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) { viewModel.consumeCompleted(); onNavigateBack() }
    }

    // FIX: error se muestra CADA VEZ que se pulsa Siguiente
    data class ErrorEvent(val msg: String, val ts: Long)
    var errorEvent by remember { mutableStateOf<ErrorEvent?>(null) }
    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrBlank())
            errorEvent = ErrorEvent(uiState.error!!, System.currentTimeMillis())
    }
    LaunchedEffect(errorEvent) {
        val ev = errorEvent ?: return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(ev.msg, duration = SnackbarDuration.Short)
    }

    // Mensaje de guardado diferenciado
    LaunchedEffect(uiState.saveMessage) {
        val msg = uiState.saveMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.consumeSaveMessage()
        }
    }

    if (uiState.isLoading) { Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }; return }

    val survey          = uiState.survey ?: return
    val paginaActualObj = uiState.pagina  ?: return

    val paginasVisibles = remember(survey.paginas, uiState.respuestas) {
        viewModel.calcularPaginasVisibles().toList()
    }
    val indicePagina = remember(survey.paginas, paginaActualObj) {
        survey.paginas.indexOf(paginaActualObj).coerceAtLeast(0)
    }

    if (uiState.showObsDialog) {
        val obsKey   = "OBS_${paginaActualObj.seccion_id}"
        val textoObs = uiState.respuestas[obsKey]?.toString().orEmpty()
        ObservacionDialog(
            titulo       = paginaActualObj.titulo_seccion,
            textoInicial = textoObs,
            minChars     = 0,
            onSave       = { viewModel.onGuardarObservacion(it) },
            onDismiss    = { viewModel.closeObsDialog() },
        )
    }

    val onUpdateAnswer: (String, Any?) -> Unit = if (soloLectura) { _, _ -> }
    else remember(viewModel) { { v: String, value: Any? -> viewModel.onUpdateAnswer(v, value) } }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isSuccess = data.visuals.message.startsWith("Datos guardados con éxito")
                val isPartial = data.visuals.message.startsWith("Guardado, pero")
                val containerColor = when {
                    isSuccess -> Color(0xFF166534)
                    isPartial -> Color(0xFF854D0E)
                    else      -> MaterialTheme.colorScheme.errorContainer
                }
                val contentColor = when {
                    isSuccess || isPartial -> Color.White
                    else -> MaterialTheme.colorScheme.onErrorContainer
                }
                Snackbar(data, containerColor = containerColor, contentColor = contentColor)
            }
        },
        topBar = {
            SurveyTopBar(
                title              = survey.title,
                sectionTitle       = paginaActualObj.titulo_seccion,
                contextoDescripcion= surveyContext.descripcion,
                highlightColor     = survey.config.color_resaltado,
                mostrarProgreso    = survey.config.mostrar_progreso,
                progresoProvider   = { uiState.progreso(paginasVisibles) },
                isSaving           = uiState.isSaving,
                soloLectura        = soloLectura,
                onObsClick         = { viewModel.openObsDialog() },
                onGuardarClick     = { viewModel.onGuardar() },
                onCloseWindowClick = onNavigateBack,
            )
        },
        bottomBar = {
            SurveyBottomBar(
                isFirstPage      = uiState.historial.isEmpty(),
                isLastPage       = uiState.isLastPage,
                isSaving         = uiState.isSaving,
                soloLectura      = soloLectura,
                onBackPageClick  = { viewModel.onBackPage() },
                onNextPageClick  = {
                    if (!soloLectura) viewModel.onNextPage() else viewModel.onNextPageReadOnly()
                },
                paginaActualInfo = "${indicePagina + 1}/${uiState.totalPaginas}",
            )
        },
    ) { padding ->
        SurveyContent(
            indicePagina            = indicePagina,
            totalPaginas            = survey.paginas.size,
            paginaActual            = paginaActualObj,
            respuestas              = uiState.respuestas,
            variableEnFoco          = uiState.obtenerVariableEnFoco(viewModel.evaluator),
            variablesConError       = uiState.variablesConError,
            variablesBlockedByError = uiState.variablesBlockedByError,
            constraintResults       = uiState.constraintResults,
            evaluator               = viewModel.evaluator,
            soloLectura             = soloLectura,
            onUpdateAnswer          = onUpdateAnswer,
            modifier                = Modifier.padding(padding),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurveyContent(
    indicePagina: Int,
    totalPaginas: Int,
    paginaActual: Pagina,
    respuestas: Map<String, Any?>,
    variableEnFoco: String,
    variablesConError: Set<String> = emptySet(),
    variablesBlockedByError: Set<String> = emptySet(),
    constraintResults: List<ConstraintResult> = emptyList(),
    evaluator: ConditionEvaluator,
    soloLectura: Boolean = false,
    onUpdateAnswer: (String, Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = indicePagina, pageCount = { totalPaginas })
    LaunchedEffect(indicePagina) {
        if (pagerState.currentPage != indicePagina) pagerState.scrollToPage(indicePagina)
    }
    HorizontalPager(state = pagerState, modifier = modifier.fillMaxSize(), userScrollEnabled = false) {
        SurveyPage(
            pagina                  = paginaActual,
            respuestas              = respuestas,
            variableEnFoco          = variableEnFoco,
            variablesConError       = variablesConError,
            variablesBlockedByError = variablesBlockedByError,
            constraintResults       = constraintResults,
            evaluator               = evaluator,
            soloLectura             = soloLectura,
            onUpdateAnswer          = onUpdateAnswer,
            minObsCaracteres = 0
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyTopBar(
    title: String,
    sectionTitle: String,
    contextoDescripcion: String,
    highlightColor: String,
    mostrarProgreso: Boolean,
    progresoProvider: () -> Float,
    isSaving: Boolean,
    soloLectura: Boolean,
    onObsClick: () -> Unit,
    onGuardarClick: () -> Unit,
    onCloseWindowClick: () -> Unit,
) {
    val fondoColor = remember(highlightColor) {
        runCatching { Color(highlightColor.toColorInt()) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary
    val contenidoColor = Color.White

    Surface(color = fondoColor, tonalElevation = 4.dp, shadowElevation = 4.dp) {
        Column {
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(title, fontSize = 10.sp, color = contenidoColor.copy(0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(sectionTitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = contenidoColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    // Contexto: muestra "Hogar #2 — Juan Pérez", "Persona #3", etc.
                    if (contextoDescripcion.isNotBlank()) {
                        Text(contextoDescripcion, fontSize = 10.sp, color = contenidoColor.copy(0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (soloLectura) Text("SOLO LECTURA", fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = Color(0xFFFCA5A5))
                }
                IconButton(onClick = onObsClick, enabled = !soloLectura, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.EditNote, "Observaciones", Modifier.size(22.dp), tint = contenidoColor)
                }
                IconButton(onClick = onGuardarClick, enabled = !isSaving && !soloLectura, modifier = Modifier.size(40.dp)) {
                    if (isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = contenidoColor)
                    else Icon(Icons.Default.Save, "Guardar", Modifier.size(20.dp), tint = contenidoColor)
                }
                IconButton(onClick = onCloseWindowClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Close, "Cerrar", Modifier.size(20.dp), tint = contenidoColor)
                }
            }
            if (mostrarProgreso) {
                LinearProgressIndicator(
                    progress   = progresoProvider,
                    modifier   = Modifier.fillMaxWidth().height(2.5.dp),
                    color      = contenidoColor.copy(0.9f),
                    trackColor = contenidoColor.copy(0.2f),
                )
            }
        }
    }
}

@Composable
fun SurveyBottomBar(
    paginaActualInfo: String,
    isFirstPage: Boolean,
    isLastPage: Boolean,
    isSaving: Boolean,
    soloLectura: Boolean = false,
    onBackPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
) {
    Surface(tonalElevation = 6.dp, shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = if (!isFirstPage) Modifier.weight(1.4f) else Modifier.size(0.dp)) {
                if (!isFirstPage) {
                    OutlinedButton(
                        onClick        = onBackPageClick,
                        enabled        = !isSaving,
                        modifier       = Modifier.height(40.dp),
                        shape          = RoundedCornerShape(8.dp),
                        border         = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ANTERIOR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                "(Pág. $paginaActualInfo)",
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.9f),
                modifier   = Modifier.weight(1f),
                textAlign  = TextAlign.Center,
            )
            Box(modifier = Modifier.weight(2f), contentAlignment = Alignment.CenterEnd) {
                Button(
                    onClick        = onNextPageClick,
                    enabled        = !isSaving,
                    modifier       = Modifier.fillMaxWidth(),
                    shape          = RoundedCornerShape(8.dp),
                    colors         = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                        contentColor   = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                ) {
                    Text(
                        if (isLastPage) (if (soloLectura) "CERRAR" else "FINALIZAR") else "SIGUIENTE",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (isLastPage) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                        null, Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}