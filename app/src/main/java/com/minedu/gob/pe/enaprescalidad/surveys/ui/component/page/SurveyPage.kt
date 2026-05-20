package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question.DynamicQuestionAdapter


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