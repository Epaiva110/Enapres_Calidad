package com.minedu.gob.pe.enaprescalidad.surveys.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.minedu.gob.pe.enaprescalidad.surveys.adapter.DynamicQuestionAdapter

import com.minedu.gob.pe.enaprescalidad.surveys.models.Survey
import kotlinx.coroutines.launch



import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.enaprescalidad.surveys.adapter.DynamicQuestionAdapter
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.viewmodel.SurveyUiState
import com.minedu.gob.pe.enaprescalidad.surveys.viewmodel.SurveyViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SurveyScreen(
    muestraId: Int,
    jsonString: String,
    onNavigateBack: () -> Unit,
    viewModel: SurveyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Init solo una vez (el ViewModel ignora llamadas repetidas con el mismo id)
    LaunchedEffect(muestraId) { viewModel.init(muestraId, jsonString) }

    // Navegación automática al completar
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) onNavigateBack()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val survey = uiState.survey ?: return

    // PagerState sincronizado con paginaActual del ViewModel
    val pagerState = rememberPagerState(
        initialPage = uiState.paginaActual,
        pageCount   = { survey.paginas.size },
    )

    // Mantener el pager en sincronía con el ViewModel (sin que el usuario pueda
    // deslizar — el control de navegación es exclusivo de los botones)
    LaunchedEffect(uiState.paginaActual) {
        if (pagerState.currentPage != uiState.paginaActual) {
            pagerState.animateScrollToPage(uiState.paginaActual)
        }
    }

    var showObsDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SurveyTopBar(
                title      = survey.title,
                seccion    = uiState.pagina?.titulo_seccion ?: "",
                pagina     = uiState.paginaActual + 1,
                total      = uiState.totalPaginas,
                progreso   = uiState.progreso,
                obsValida  = (uiState.respuestas["OBS_${uiState.pagina?.seccion_id}"] ?: "")
                    .length >= (survey.config.min_caracteres_observacion),
                mostrarProgreso = survey.config.mostrar_progreso,
                isSaving   = uiState.isSaving,
                onObsClick = { showObsDialog = true },
                onBack     = onNavigateBack,
                onSave     = { viewModel.onGuardar() },
            )
        },
        bottomBar = {
            SurveyBottomBar(
                isLastPage = uiState.isLastPage,
                isValid    = uiState.paginaValida,
                canGoBack  = uiState.historial.isNotEmpty(),
                isSaving   = uiState.isSaving,
                onBack     = { viewModel.onAnterior() },
                onNext     = { viewModel.onSiguiente() },
            )
        },
    ) { padding ->

        if (showObsDialog) {
            val seccionId = uiState.pagina?.seccion_id ?: ""
            val llaveObs  = "OBS_$seccionId"
            ObservacionDialog(
                titulo       = uiState.pagina?.titulo_seccion ?: "",
                textoInicial = uiState.respuestas[llaveObs] ?: "",
                minChars     = survey.config.min_caracteres_observacion,
                onSave       = { viewModel.onRespuesta(llaveObs, it); showObsDialog = false },
                onDismiss    = { showObsDialog = false },
            )
        }

        HorizontalPager(
            state          = pagerState,
            modifier       = Modifier.padding(padding),
            userScrollEnabled = false,   // solo los botones controlan la navegación
        ) { index ->
            SurveyPage(
                pagina        = survey.paginas[index],
                respuestas    = uiState.respuestas,
                variableEnFoco = uiState.variableEnFoco,
                onValueChange = { v, val_ -> viewModel.onRespuesta(v, val_) },
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyTopBar(
    title: String, seccion: String,
    pagina: Int, total: Int, progreso: Float,
    obsValida: Boolean, mostrarProgreso: Boolean,
    isSaving: Boolean,
    onObsClick: () -> Unit, onBack: () -> Unit, onSave: () -> Unit,
) {
    Column {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor      = MaterialTheme.colorScheme.surface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                }
            },
            title = {
                Column {
                    Text(title, fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(seccion, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        maxLines = 1)
                }
            },
            actions = {
                // Botón guardar manual
                IconButton(onClick = onSave, enabled = !isSaving) {
                    AnimatedContent(isSaving, label = "save") { saving ->
                        if (saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Save, "Guardar",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                // Botón observación
                IconButton(onClick = onObsClick) {
                    Icon(
                        Icons.Default.EditNote, "Observación",
                        tint = if (obsValida) Color(0xFF22C55E) else Color(0xFFEF4444),
                    )
                }
            },
        )
        // Barra de progreso + contador de páginas
        if (mostrarProgreso) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinearProgressIndicator(
                    progress  = { progreso },
                    modifier  = Modifier.weight(1f).height(4.dp),
                )
                Text("$pagina/$total", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  BOTTOM BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SurveyBottomBar(
    isLastPage: Boolean, isValid: Boolean,
    canGoBack: Boolean, isSaving: Boolean,
    onBack: () -> Unit, onNext: () -> Unit,
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick  = onBack,
                enabled  = canGoBack,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Anterior")
            }

            Button(
                onClick  = onNext,
                enabled  = isValid && !isSaving,
                modifier = Modifier.weight(1.5f),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(if (isLastPage) "Finalizar" else "Siguiente")
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (isLastPage) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                        null, Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PÁGINA DE PREGUNTAS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SurveyPage(
    pagina: Pagina,
    respuestas: Map<String, String>,
    variableEnFoco: String,
    onValueChange: (String, String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(pagina.titulo, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        pagina.preguntas.forEach { pregunta ->
            DynamicQuestionAdapter(
                pregunta       = pregunta,
                respuestas     = respuestas,
                variableEnFoco = variableEnFoco,
                onValueChange  = onValueChange,
            )
        }
        Spacer(Modifier.height(120.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIÁLOGO DE OBSERVACIÓN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ObservacionDialog(
    titulo: String, textoInicial: String, minChars: Int,
    onSave: (String) -> Unit, onDismiss: () -> Unit,
) {
    var texto by remember { mutableStateOf(textoInicial) }
    val valido = texto.trim().length >= minChars

    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Default.EditNote, null) },
        title = { Text("Observación: $titulo") },
        text  = {
            Column {
                OutlinedTextField(
                    value         = texto,
                    onValueChange = { texto = it },
                    modifier      = Modifier.fillMaxWidth().height(140.dp),
                    placeholder   = { Text("Describa la situación observada...") },
                    shape         = RoundedCornerShape(8.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${texto.length}/$minChars caracteres mínimos",
                    fontSize = 12.sp,
                    color    = if (valido) Color(0xFF22C55E) else MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(texto) }, enabled = valido) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}