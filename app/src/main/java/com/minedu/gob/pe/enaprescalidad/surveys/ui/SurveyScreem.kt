package com.minedu.gob.pe.enaprescalidad.surveys.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.viewmodel.SurveyViewModel
import androidx.core.graphics.toColorInt
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.page.SurveyPage
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question.ObservacionDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurveyScreen(
    muestraId: Int,
    jsonString: String,
    onNavigateBack: () -> Unit,
    viewModel: SurveyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var lastError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(muestraId) {
        viewModel.init(muestraId, jsonString)
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) onNavigateBack()
    }

    LaunchedEffect(uiState.error) {
        val error = uiState.error
        if (error != null && error != lastError) {
            lastError = error
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    if (uiState.isLoading) {
        SurveyLoadingScreen()
        return
    }

    val survey = uiState.survey ?: return
    val paginaActualObj = uiState.pagina ?: return

    val paginasVisibles = remember(survey.paginas) {
        viewModel.calcularPaginasVisibles().toList()
    }

    val indicePagina = remember(survey.paginas, paginaActualObj) {
        survey.paginas.indexOf(paginaActualObj).coerceAtLeast(0)
    }

    if (uiState.showObsDialog) {
        val obsKey = remember(paginaActualObj.seccion_id) {
            "OBS_${paginaActualObj.seccion_id}"
        }
        val textoObs = uiState.respuestas[obsKey]?.toString().orEmpty()

        ObservacionDialog(
            titulo = paginaActualObj.titulo_seccion,
            textoInicial = textoObs,
            minChars = 0,
            onSave = { viewModel.onGuardarObservacion(it) },
            onDismiss = { viewModel.closeObsDialog() },
        )
    }

    val onUpdateAnswer = remember(viewModel) {
        { v: String, value: Any? ->
            viewModel.onUpdateAnswer(v, value)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SurveyTopBar(
                title = survey.title,
                sectionTitle = paginaActualObj.titulo_seccion,
                highlightColor = survey.config.color_resaltado,
                mostrarProgreso = survey.config.mostrar_progreso,
                progresoProvider = { uiState.progreso(paginasVisibles) },
                isSaving = uiState.isSaving,
                obsValida = uiState.obsValida,
                onObsClick = { viewModel.openObsDialog() },
                onGuardarClick = { viewModel.onGuardar() },
                onCloseWindowClick = onNavigateBack
            )
        },
        bottomBar = {
            SurveyBottomBar(
                isFirstPage = uiState.historial.isEmpty(),
                isLastPage = uiState.isLastPage,
                isSaving = uiState.isSaving,
                onBackPageClick = { viewModel.onBackPage() },
                onNextPageClick = { viewModel.onNextPage() },
                paginaActualInfo ="${indicePagina + 1}/${uiState.totalPaginas}"
            )
        }
    ) { padding ->
        SurveyContent(
            indicePagina = indicePagina,
            totalPaginas = survey.paginas.size,
            paginaActual = paginaActualObj,
            respuestas = uiState.respuestas,
            variableEnFoco = uiState.obtenerVariableEnFoco(viewModel.evaluator), // Puede ser String?
            evaluator = viewModel.evaluator,
            onUpdateAnswer = onUpdateAnswer,
            modifier = Modifier.padding(padding) // Sigue pasando el padding para no superponerse con las barras fijas
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
    variableEnFoco: String, // Ajustado a Nullable por seguridad
    evaluator: ConditionEvaluator,
    onUpdateAnswer: (String, Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = indicePagina,
        pageCount = { totalPaginas }
    )

    LaunchedEffect(indicePagina) {
        if (pagerState.currentPage != indicePagina) {
            pagerState.scrollToPage(indicePagina)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = false
    ) {
        SurveyPage(
            pagina = paginaActual,
            respuestas = respuestas,
            variableEnFoco = variableEnFoco,
            evaluator = evaluator,
            onUpdateAnswer = onUpdateAnswer,
        )
    }
}


@Composable
fun SurveyTopBar(
    title: String,
    sectionTitle: String,
    highlightColor: String,
    mostrarProgreso: Boolean,
    progresoProvider: () -> Float,
    isSaving: Boolean,
    obsValida: Boolean,
    onObsClick: () -> Unit,
    onGuardarClick: () -> Unit,
    onCloseWindowClick: () -> Unit,
) {
    val colorDinamico = remember(highlightColor) {
        try {
            Color(highlightColor.toColorInt())
        } catch (_: Exception) {
            null
        }
    }

    val contenedorColor = colorDinamico?.copy(alpha = 0.08f) ?: MaterialTheme.colorScheme.surfaceContainerLow
    val contenidoColor = MaterialTheme.colorScheme.onSurface

    Surface(
        color = contenedorColor,
        tonalElevation = 1.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Textos centralizados y compactos (Cero desperdicio de espacio vertical)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp,
                        color = contenidoColor.copy(alpha = 0.55f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = sectionTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contenidoColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onObsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Observaciones",
                        modifier = Modifier.size(22.dp),
                        tint = if (obsValida) {
                            colorDinamico ?: MaterialTheme.colorScheme.primary
                        } else {
                            contenidoColor.copy(alpha = 0.6f)
                        }
                    )
                }
                IconButton(
                    onClick = onGuardarClick,
                    enabled = !isSaving,
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = colorDinamico ?: MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar",
                            modifier = Modifier.size(20.dp),
                            tint = contenidoColor
                        )
                    }
                }
                // Botón Cerrar
                IconButton(
                    onClick = onCloseWindowClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        modifier = Modifier.size(20.dp),
                        tint = contenidoColor
                    )
                }
            }
            // Barra de progreso milimétrica pegada al borde inferior
            if (mostrarProgreso) {
                LinearProgressIndicator(
                    progress = progresoProvider,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = colorDinamico ?: MaterialTheme.colorScheme.primary,
                    trackColor = (colorDinamico ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f)
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
    onBackPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
) {
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = if (!isFirstPage) {Modifier
                    .weight(1.4f)
                    .padding(horizontal = 2.dp)} else {Modifier
                    .height(0.dp)
                    .width(0.dp)} ,
                contentAlignment = Alignment.CenterStart
            ) {
                if (!isFirstPage) {
                    OutlinedButton(
                        onClick = onBackPageClick,
                        enabled = !isSaving,
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp), // Un poco más compacto para equilibrar
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "ANTERIOR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
            Text(
                text = "(Pág. $paginaActualInfo)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold, // Un poco más de peso para que destaque entre los dos botones
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                modifier = Modifier
                    .padding(horizontal = 0.dp)
                    .weight(1.0f),
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .weight(2.0f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.CenterEnd,

            ) {
                Button(
                    onClick = onNextPageClick,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp),

                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),

                    ) {
                        Text(
                            text = if (isLastPage) "FINALIZAR" else "SIGUIENTE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = if (isLastPage) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurveyLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}