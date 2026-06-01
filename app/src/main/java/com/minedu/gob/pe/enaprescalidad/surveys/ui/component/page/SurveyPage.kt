package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.page

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConstraintResult
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.models.ValidationSeverity
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question.DynamicQuestionAdapter
import com.minedu.gob.pe.enaprescalidad.surveys.ui.util.ConstraintHelper

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY PAGE
//
//  Renderiza todas las preguntas visibles de una página usando
//  DynamicQuestionAdapter (el dispatcher real de componentes).
//
//  Sistema de constraints:
//   · variablesConError       → required sin responder → borde rojo en QuestionCard
//   · variablesBlockedByError → constraint ERROR activo → editable = false
//   · constraintResults       → lista completa para mostrar mensajes WARNING
//                               inline debajo de cada pregunta afectada
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SurveyPage(
    pagina: Pagina,
    respuestas: Map<String, Any?>,
    variableEnFoco: String,
    variablesConError: Set<String>,
    variablesBlockedByError: Set<String> = emptySet(),
    constraintResults: List<ConstraintResult> = emptyList(),
    evaluator: ConditionEvaluator,
    minObsCaracteres: Int,
    soloLectura: Boolean,
    onUpdateAnswer: (String, Any?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Preguntas que pasan su show_if (o no tienen)
    val preguntasVisibles = pagina.preguntas.filter { preg ->
        preg.show_if == null || evaluator.evaluate(preg.show_if, respuestas)
    }

    LazyColumn(
        modifier        = modifier.fillMaxSize(),
        contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Encabezado de página ─────────────────────────────────────────
        item(key = "__header__") {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = pagina.titulo_seccion,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text       = pagina.titulo,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // ── Preguntas ─────────────────────────────────────────────────────
        items(
            items = preguntasVisibles,
            key   = { it.variable },
        ) { pregunta ->

            // Una pregunta está bloqueada para edición si:
            //   · soloLectura, O
            //   · tiene un constraint ERROR activo en esta variable
            val bloqueada = soloLectura ||
                    ConstraintHelper.isBlocked(pregunta.variable, variablesBlockedByError)

            // Mensaje de WARNING activo para esta variable (null si no hay)
            val mensajeWarning = ConstraintHelper.warningMessage(
                variable          = pregunta.variable,
                constraintResults = constraintResults,
            )

            // Mensajes de ERROR de constraint (bloqueante, además del required)
            val mensajesError = ConstraintHelper.errorMessages(
                variable          = pregunta.variable,
                constraintResults = constraintResults,
            )

            Column {
                // Componente de pregunta real
                DynamicQuestionAdapter(
                    pregunta          = pregunta,
                    respuestas        = respuestas,
                    variableEnFoco    = variableEnFoco,
                    variablesConError = variablesConError,
                    editable          = !bloqueada,
                    onValueChange     = onUpdateAnswer,
                )

                // ── Mensajes de ERROR de constraint (bajo la tarjeta) ─────
                // Solo se muestran si la variable está bloqueada por ERROR;
                // el QuestionCard ya pone el borde rojo por variablesConError.
                mensajesError.forEach { msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = expandVertically(),
                        exit    = shrinkVertically(),
                    ) {
                        ConstraintMessageRow(
                            message  = msg,
                            severity = ValidationSeverity.ERROR,
                        )
                    }
                }

                // ── Mensaje de WARNING de constraint (bajo la tarjeta) ────
                // Informativo: no bloquea ni la selección ni el avance.
                AnimatedVisibility(
                    visible = mensajeWarning != null,
                    enter   = expandVertically(),
                    exit    = shrinkVertically(),
                ) {
                    mensajeWarning?.let { msg ->
                        ConstraintMessageRow(
                            message  = msg,
                            severity = ValidationSeverity.WARNING,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CONSTRAINT MESSAGE ROW
//
//  Fila de mensaje que aparece debajo de la pregunta cuando hay un constraint
//  activo. El color y el ícono cambian según la severidad:
//   · ERROR   → rojo  (la pregunta ya está deshabilitada por isBlocked)
//   · WARNING → ámbar (informativo, no bloquea nada)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConstraintMessageRow(
    message: String,
    severity: ValidationSeverity,
    modifier: Modifier = Modifier,
) {
    val color = when (severity) {
        ValidationSeverity.ERROR   -> MaterialTheme.colorScheme.error
        ValidationSeverity.WARNING -> MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
    }

    Row(
        modifier             = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp, end = 8.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector        = Icons.Default.Warning,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(14.dp),
        )
        Text(
            text  = message,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}