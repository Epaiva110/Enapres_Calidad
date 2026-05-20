package com.minedu.gob.pe.enaprescalidad.surveys.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.viewmodel.SurveyViewModel
import androidx.core.graphics.toColorInt
import com.minedu.gob.pe.enaprescalidad.surveys.question.GpsQuestion
import com.minedu.gob.pe.enaprescalidad.surveys.question.PhotoQuestion

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
        contentWindowInsets = WindowInsets(0), // Correcto: Evita paddings extraños de sistema dentro de subventanas
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SurveyTopBar(
                title = survey.title,
                sectionTitle = paginaActualObj.titulo_seccion,
                highlightColor = survey.config.color_resaltado,
                mostrarProgreso = survey.config.mostrar_progreso,
                progresoProvider = { uiState.progreso(paginasVisibles) },
//                paginaActualInfo = "${indicePagina + 1}/${uiState.totalPaginas}",
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

    // CORRECCIÓN 1: Cambiado animateScrollToPage por scrollToPage para evitar
    // transiciones visuales rotas cuando hay saltos lógicos entre páginas no consecutivas.
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

@Preview(showBackground = true, widthDp = 360) // Le añadimos un ancho de smartphone para ver cómo escala
@Composable
fun SurveyTopBarPreview() {
    MaterialTheme { // Es buena práctica envolverlo en el tema para que use los colores correctos
        SurveyTopBar(
            title = "Encuesta de Calidad de Vida 2026",
            sectionTitle = "Sección II: Datos Demográficos",
            highlightColor = "#1565C0",
            mostrarProgreso = true,
            progresoProvider = { 0.45f }, // 45% de progreso simulado (entre 0.0f y 1.0f)
//            paginaActualInfo = "3/7",
            isSaving = false, // Cambiado a false para que puedas ver el icono de guardar en el Preview
            obsValida = true,
            onObsClick = {},
            onGuardarClick = {},
            onCloseWindowClick = {}
        )
    }
}

@Preview
@Composable
fun Prueba2 () {
     SurveyBottomBar(
        paginaActualInfo= "3/7",
        isFirstPage= false,
        isLastPage= false,
        isSaving =  false,
        onBackPageClick={},
        onNextPageClick ={  },
    )
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
            // Fila principal optimizada a solo 48.dp de alto (más delgada que el estándar de 56)
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

                // Acciones Laterales
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

            // 1. BOTÓN ANTERIOR (Ahora como OutlinedButton para mejor UX)
            Box(
                modifier = if (!isFirstPage) {Modifier.weight(1.4f).padding(horizontal = 2.dp)} else {Modifier.height(0.dp).width(0.dp)} ,
                contentAlignment = Alignment.CenterStart
            ) {
                if (!isFirstPage) {
                    OutlinedButton(
                        onClick = onBackPageClick,
                        enabled = !isSaving,
                        modifier = Modifier.height(40.dp), // Emparejamos la altura con el botón Siguiente
                        shape = RoundedCornerShape(8.dp),
                        // Definimos un borde suave usando el esquema de Material 3
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

            // 2. CONTADOR DE PÁGINAS CENTRALIZADO
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

            // 3. BOTÓN SIGUIENTE / FINALIZAR
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




// ─────────────────────────────────────────────────────────────────────────────
//  PÁGINA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SurveyPage(
    pagina: Pagina,
    respuestas: Map<String, Any?>,
    variableEnFoco: String,
    evaluator: ConditionEvaluator,
    onUpdateAnswer: (String, Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Cabecera de sección
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                .padding(12.dp),
        ) {
            Text(
                pagina.titulo_seccion.uppercase(),
                style      = MaterialTheme.typography.labelMedium,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(pagina.titulo, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Preguntas filtradas por show_if
        pagina.preguntas.forEach { pregunta ->
            val visible = pregunta.show_if == null || evaluator.evaluate(pregunta.show_if, respuestas)
            if (visible) {
                DynamicQuestionAdapter(
                    pregunta       = pregunta,
                    respuestas     = respuestas,
                    variableEnFoco = variableEnFoco,
                    onValueChange  = onUpdateAnswer,
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIÁLOGO OBSERVACIÓN
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
                    modifier      = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder   = { Text("Describa la situación observada...") },
                    shape         = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// COMPONENTE AUXILIAR OPCIONAL: CUADRO DE DIÁLOGO DE OBSERVACIONES REGISTRADAS
// ─────────────────────────────────────────────────────────────────────────────


// ─────────────────────────────────────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyTopBar(
    title: String,
    tieneHistorial: Boolean,
    onBackClick: () -> Unit,
    onExitClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        navigationIcon = {
            IconButton(onClick = { if (tieneHistorial) onBackClick() else onExitClick() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  BOTTOM BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SurveyBottomBar(
    progreso: Float,
    mostrarProgreso: Boolean,
    colorResaltadoHex: String,
    tieneHistorial: Boolean,
    isSaving: Boolean,
    isLastPage: Boolean,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
        Column {
            if (mostrarProgreso) {
                val colorBarra = runCatching {
                    Color(android.graphics.Color.parseColor(colorResaltadoHex))
                }.getOrElse { MaterialTheme.colorScheme.primary }

                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth(),
                    color = colorBarra
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onBackClick, enabled = tieneHistorial) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Atrás")
                }

                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }

                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLastPage) {
                        Text("Finalizar")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Check, null)
                    } else {
                        Text("Siguiente")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
//  DISPATCHER PRINCIPAL (ADAPTADOR DINÁMICO)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DynamicQuestionAdapter(
    pregunta: Pregunta,
    respuestas: Map<String, Any?>,
    variableEnFoco: String,
    onValueChange: (String, Any?) -> Unit,
    modifier: Modifier = Modifier
) {
    val valorActual = respuestas[pregunta.variable]
    val estaEnFoco = pregunta.variable == variableEnFoco

    QuestionCard(pregunta = pregunta, estaEnFoco = estaEnFoco, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Renderizador del componente base según el nuevo esquema
            when (pregunta.type.lowercase()) {
                "info" -> InfoQuestion(pregunta)
                "text", "date", "time", "datetime" -> TextOrDateTimeQuestion(pregunta, valorActual as? String ?: "", onValueChange)
                "number" -> NumberQuestion(pregunta, valorActual, onValueChange, isDecimal = false)
                "decimal" -> NumberQuestion(pregunta, valorActual, onValueChange, isDecimal = true)
                "single" -> SingleChoiceQuestion(pregunta, valorActual, respuestas, onValueChange)
                "multiple", "multiple_binary" -> MultipleChoiceQuestion(pregunta, valorActual as? List<*> ?: emptyList<Any>(), respuestas, onValueChange)
                "matrix", "matrix_scale" -> MatrixQuestionAdapter(pregunta, respuestas, onValueChange)
                "slider" -> SliderQuestion(pregunta, valorActual as? Float ?: 0f, onValueChange)
                "likert" -> LikertQuestion(pregunta, valorActual, onValueChange)
                "ranking" -> RankingQuestion(pregunta, valorActual as? String ?: "", onValueChange)
                "gps" -> GpsQuestion(pregunta, valorActual as? String ?: "", onValueChange)
                "photo" -> PhotoQuestion(pregunta, valorActual as? List<*> ?: emptyList<Any>(), onValueChange)
                else -> Text("Componente no soportado: ${pregunta.type}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  COMPONENTES CONTENEDORES Y ATÓMICOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QuestionCard(
    pregunta: Pregunta,
    estaEnFoco: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val borderColor = if (estaEnFoco) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val borderWidth = if (estaEnFoco) 2.dp else 1.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (pregunta.type != "info") {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = pregunta.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (pregunta.required) {
                        Text(" *", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
                pregunta.hint?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            content()
        }
    }
}

@Composable
fun InfoQuestion(pregunta: Pregunta) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
        Column {
            Text(pregunta.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            pregunta.hint?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun TextOrDateTimeQuestion(pregunta: Pregunta, valor: String, onValueChange: (String, Any?) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = { onValueChange(pregunta.variable, it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(pregunta.type.uppercase()) },
        singleLine = pregunta.type != "text"
    )
}

@Composable
fun NumberQuestion(pregunta: Pregunta, valor: Any?, onValueChange: (String, Any?) -> Unit, isDecimal: Boolean) {
    val textoValue = valor?.toString() ?: ""
    OutlinedTextField(
        value = textoValue,
        onValueChange = { stringInput ->
            if (isDecimal) {
                stringInput.toDoubleOrNull()?.let { onValueChange(pregunta.variable, it) } ?: if(stringInput.isEmpty()) onValueChange(pregunta.variable, null) else Unit
            } else {
                stringInput.toIntOrNull()?.let { onValueChange(pregunta.variable, it) } ?: if(stringInput.isEmpty()) onValueChange(pregunta.variable, null) else Unit
            }
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(if (isDecimal) "0.00" else "0") }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  SELECCIÓN ÚNICA CON DETALLE ANIDADO (DETAIL_QUESTIONS)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SingleChoiceQuestion(pregunta: Pregunta, valorActual: Any?, respuestas: Map<String, Any?>, onValueChange: (String, Any?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pregunta.options?.forEach { opcion ->
            val seleccionado = valorActual?.toString() == opcion.value?.toString()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onValueChange(pregunta.variable, opcion.value) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = seleccionado, onClick = { onValueChange(pregunta.variable, opcion.value) })
                Spacer(modifier = Modifier.width(8.dp))
                Text(opcion.label, style = MaterialTheme.typography.bodyMedium)
            }

            // Si la opción está seleccionada y tiene subpreguntas (Especifique), se renderizan en cascada
            if (seleccionado && !opcion.detail_questions.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(somePaddingValuesParaSangria(left = 24.dp)),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    opcion.detail_questions.forEach { subPregunta ->
                        DynamicQuestionAdapter(
                            pregunta = subPregunta,
                            respuestas = respuestas,
                            variableEnFoco = "",
                            onValueChange = onValueChange
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SELECCIÓN MÚLTIPLE CON CONTROL BINARIO Y SUB-PREGUNTAS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MultipleChoiceQuestion(pregunta: Pregunta, listaSeleccionada: List<*>, respuestas: Map<String, Any?>, onValueChange: (String, Any?) -> Unit) {
    val itemsString = listaSeleccionada.map { it.toString() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pregunta.options?.forEach { opcion ->
            val targetValue = opcion.value ?: opcion.variable ?: ""
            val marcado = itemsString.contains(targetValue)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val mLista = itemsString.toMutableList()
                        if (marcado) mLista.remove(targetValue) else {
                            if (opcion.is_none == true) mLista.clear() else {
                                pregunta.options.forEach {
                                    if (it.is_none == true) mLista.remove(
                                        it.value ?: it.variable ?: ""
                                    )
                                }
                            }
                            mLista.add(targetValue)
                        }
                        onValueChange(pregunta.variable, mLista)
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = marcado, onCheckedChange = {  })
                Spacer(modifier = Modifier.width(8.dp))
                Text(opcion.label, style = MaterialTheme.typography.bodyMedium)
            }

            // Renderizado condicional en cascada del "Especifique" para opción Múltiple
            if (marcado && !opcion.detail_questions.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(somePaddingValuesParaSangria(left = 24.dp)),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    opcion.detail_questions.forEach { subPregunta ->
                        DynamicQuestionAdapter(
                            pregunta = subPregunta,
                            respuestas = respuestas,
                            variableEnFoco = "",
                            onValueChange = onValueChange
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ADAPTADOR Y ESTRUCTURA DE MATRICES DINÁMICAS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MatrixQuestionAdapter(pregunta: Pregunta, respuestas: Map<String, Any?>, onValueChange: (String, Any?) -> Unit) {
    val columnas = if (pregunta.type == "matrix_scale") {
        val a = pregunta.scale_min?: 0
        val b = pregunta.scale_max?: 0
        (a..b).map { it.toString() }
    } else {
        listOf("1", "2", "3") // Estándar: SÍ / NO / NO TIENE
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        pregunta.options?.forEach { fila ->
            val subVariableMatriz = "${pregunta.variable}_${fila.variable ?: fila.value ?: ""}"
            val seleccionActual = respuestas[subVariableMatriz]?.toString() ?: ""

            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(8.dp)) {
                Text(fila.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    columnas.forEach { col ->
                        val colDeshabilitada = fila.disabled_if_cols?.contains(col) == true

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = seleccionActual == col,
                                enabled = !colDeshabilitada,
                                onClick = { onValueChange(subVariableMatriz, col) }
                            )
                            Text(text = col, fontSize = 12.sp)
                        }
                    }
                }

                // Evaluar si la fila de la matriz despliega un subformulario condicional
                if (seleccionActual.isNotEmpty() && !fila.detail_questions.isNullOrEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.detail_questions.forEach { subPreguntaMatriz ->
                            DynamicQuestionAdapter(
                                pregunta = subPreguntaMatriz,
                                respuestas = respuestas,
                                variableEnFoco = "",
                                onValueChange = onValueChange
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  COMPONENTES ADICIONALES DEL NUEVO ESQUEMA JSON (SLIDERS, LIKERT, HARDWARE)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SliderQuestion(pregunta: Pregunta, valor: Float, onValueChange: (String, Any?) -> Unit) {
    val min = pregunta.min_value?.toFloat() ?: 0f
    val max = pregunta.max_value?.toFloat() ?: 100f
    Column {
        Slider(
            value = valor.coerceIn(min, max),
            onValueChange = { onValueChange(pregunta.variable, it) },
            valueRange = min..max,
            steps = if (pregunta.step != null) ((max - min) / pregunta.step.toFloat()).toInt() - 1 else 0
        )
        Text("Valor seleccionado: ${String.format("%.1f", valor)}", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun LikertQuestion(pregunta: Pregunta, valor: Any?, onValueChange: (String, Any?) -> Unit) {
    val totalElementos = pregunta.likert_count ?: 5
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        for (i in 1..totalElementos) {
            val activo = valor?.toString() == i.toString()
            OutlinedButton(
                onClick = { onValueChange(pregunta.variable, i.toString()) },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (activo) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Text(text = if (pregunta.likert_type == "emoji") "⭐" else i.toString())
            }
        }
    }
}

@Composable
fun RankingQuestionPlaceholder(pregunta: Pregunta, lista: List<*>, onValueChange: (String, Any?) -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
        .padding(8.dp)) {
        Text("Módulo Ordenamiento (Ranking)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        pregunta.options?.forEachIndexed { index, opt ->
            Text("${index + 1}. ${opt.label}", style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
fun RankingQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    val orden = remember(valorActual) {
        if (valorActual.isBlank()) {
            pregunta.options?.map { it.value ?: it.variable ?: "" }?.toMutableStateList()
                ?: mutableStateListOf()
        } else {
            valorActual.split(",").toMutableStateList()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Arrastra para ordenar de mayor a menor prioridad",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        orden.forEachIndexed { i, valor ->
            val label = pregunta.options?.find { it.value == valor || it.variable == valor }?.label ?: valor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("${i + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(20.dp))
                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Column {
                    IconButton(onClick = {
                        if (i > 0) { orden.swap(i, i - 1); onChange(pregunta.variable, orden.joinToString(",")) }
                    }, modifier = Modifier.size(24.dp), enabled = i > 0) {
                        Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(16.dp))
                    }
                    IconButton(onClick = {
                        if (i < orden.lastIndex) { orden.swap(i, i + 1); onChange(pregunta.variable, orden.joinToString(",")) }
                    }, modifier = Modifier.size(24.dp), enabled = i < orden.lastIndex) {
                        Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

private fun <T> MutableList<T>.swap(a: Int, b: Int) { val t = this[a]; this[a] = this[b]; this[b] = t }


@Composable
fun GpsQuestionPlaceholder(pregunta: Pregunta, coords: String, onValueChange: (String, Any?) -> Unit) {
    Button(onClick = { onValueChange(pregunta.variable, "-12.0664, -77.0428") }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.LocationOn, null)
        Spacer(Modifier.width(8.dp))
        Text(if (coords.isEmpty()) "Capturar Ubicación GPS" else "Coordenadas: $coords")
    }
}

@Composable
fun PhotoQuestionPlaceholder(pregunta: Pregunta, fotos: List<*>, onValueChange: (String, Any?) -> Unit) {
    Button(onClick = { onValueChange(pregunta.variable, fotos + "foto_mock_${System.currentTimeMillis()}.jpg") }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Default.PhotoCamera, null)
        Spacer(Modifier.width(8.dp))
        Text("Añadir Evidencia Fotográfica (${fotos.size}/${pregunta.max_photos ?: 1})")
    }
}

// Helper sintáctico para manejar los PaddingValues de sangría en subpreguntas de Jetpack Compose
private fun somePaddingValuesParaSangria(left: androidx.compose.ui.unit.Dp) =
    PaddingValues(start = left, top = 8.dp, end = 0.dp, bottom = 4.dp)