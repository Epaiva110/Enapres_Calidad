package com.minedu.gob.pe.encuestasatisfaccinenapres.surveys.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.minedu.gob.pe.encuestasatisfaccinenapres.surveys.models.Pagina
import com.minedu.gob.pe.encuestasatisfaccinenapres.surveys.models.Survey
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(jsonString: String) {

    val survey = remember(jsonString) {
        Gson().fromJson(jsonString, Survey::class.java)
    }

    val respuestas = remember { mutableStateMapOf<String, String>() }
    val pagerState = rememberPagerState(pageCount = { survey.paginas.size })
    val scope = rememberCoroutineScope()
    val historial = remember { mutableStateListOf<Int>() }

    var showObsDialog by remember { mutableStateOf(false) }

    val paginaActual = survey.paginas[pagerState.currentPage]
    val llaveObs = "OBS_${paginaActual.seccion_id}"
    val obsActual = respuestas[llaveObs] ?: ""

    val variableEnFoco = getVariableEnFoco(paginaActual, respuestas)

    val isValid = isPageValid(
        paginaActual,
        respuestas,
        obsActual,
        survey.config.min_caracteres_observacion
    )

    Scaffold(
        topBar = {
            SurveyTopBar(
                title = survey.title,
                seccion = paginaActual.titulo_seccion,
                obsValida = obsActual.length >= 10,
                onObsClick = { showObsDialog = true }
            )
        },
        bottomBar = {
            SurveyBottomBar(
                isLastPage = pagerState.currentPage == survey.paginas.lastIndex,
                isValid = isValid,
                canGoBack = pagerState.currentPage > 0,
                onBack = {
                    if (historial.isNotEmpty()) {
                        val destino = historial.removeLast()
                        scope.launch { pagerState.animateScrollToPage(destino) }
                    }
                },
                onNext = {
                    scope.launch {
                        if (pagerState.currentPage < survey.paginas.lastIndex) {

                            historial.add(pagerState.currentPage)

                            val jump = getJumpTarget(paginaActual, respuestas)

                            pagerState.animateScrollToPage(
                                jump ?: pagerState.currentPage + 1
                            )
                        } else {
                            // TODO: enviar encuesta
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (showObsDialog) {
            ObservacionDialog(
                titulo = paginaActual.titulo_seccion,
                textoInicial = obsActual,
                minChars = survey.config.min_caracteres_observacion,
                onSave = {
                    respuestas[llaveObs] = it
                    showObsDialog = false
                },
                onDismiss = { showObsDialog = false }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(padding),
            userScrollEnabled = false
        ) { index ->

            SurveyPage(
                pagina = survey.paginas[index],
                respuestas = respuestas,
                variableEnFoco = variableEnFoco
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyTopBar(
    title: String,
    seccion: String,
    obsValida: Boolean,
    onObsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, fontSize = 11.sp, color = Color.Gray)
                Text(seccion, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        actions = {
            IconButton(onClick = onObsClick) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = "Observaciones",
                    tint = if (obsValida) Color(0xFF4CAF50) else Color.Red
                )
            }
        }
    )
}

@Composable
fun SurveyBottomBar(
    isLastPage: Boolean,
    isValid: Boolean,
    canGoBack: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    BottomAppBar {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack, enabled = canGoBack) {
                Text("Anterior")
            }

            Button(
                onClick = onNext,
                enabled = isValid
            ) {
                Text("Guardar")
            }

            Button(
                onClick = onNext,
                enabled = isValid
            ) {
                Text(if (isLastPage) "Finalizar" else "Siguiente")
            }
        }
    }
}

@Composable
fun ObservacionDialog(
    titulo: String,
    textoInicial: String,
    minChars: Int,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempText by remember { mutableStateOf(textoInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Observación: $titulo") },
        text = {
            Column {
                OutlinedTextField(
                    value = tempText,
                    onValueChange = { tempText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Mínimo $minChars caracteres. Actual: ${tempText.length}",
                    color = if (tempText.length >= minChars) Color(0xFF4CAF50) else Color.Red,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(tempText) },
                enabled = tempText.length >= minChars
            ) {
                Text("GUARDAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}

@Composable
fun SurveyPage(
    pagina: Pagina,
    respuestas: MutableMap<String, String>,
    variableEnFoco: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = pagina.titulo,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        pagina.preguntas.forEach { pregunta ->
            DynamicQuestionAdapter(
                pregunta = pregunta,
                respuestas = respuestas,
                variableEnFoco = variableEnFoco,
                onValueChange = { v, valr -> respuestas[v] = valr }
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

fun getVariableEnFoco(
    pagina: Pagina,
    respuestas: Map<String, String>
): String {
    return pagina.preguntas.find { p ->
        if (p.type == "matrix") {
            p.options?.any { respuestas[it.variable].isNullOrEmpty() } == true
        } else {
            respuestas[p.variable].isNullOrEmpty()
        }
    }?.variable ?: ""
}

fun getJumpTarget(
    pagina: Pagina,
    respuestas: Map<String, String>
): Int? {
    pagina.preguntas.forEach { q ->
        val r = respuestas[q.variable]
        q.options?.find { it.value == r }?.jump_to_page?.let {
            return it
        }
    }
    return null
}

fun isPageValid(
    pagina: Pagina,
    respuestas: Map<String, String>,
    obs: String,
    minChars: Int
): Boolean {

    val preguntasOk = pagina.preguntas.all { p ->
        if (!p.required) return@all true

        when (p.type) {
            "matrix" -> p.options?.all {
                !respuestas[it.variable].isNullOrEmpty()
            } ?: true

            "multiple_binary" ->
                respuestas[p.variable]
                    ?.split("|")
                    ?.firstOrNull()
                    ?.isNotBlank() == true

            "gps" -> {
                val gps = respuestas[p.variable].orEmpty()
                gps.contains("OMITIDO") ||
                        gps.split("|").firstOrNull()?.isNotEmpty() == true
            }

            else -> !respuestas[p.variable].isNullOrBlank()
        }
    }

    val obsOk = obs.trim().length >= minChars

    return preguntasOk && obsOk
}