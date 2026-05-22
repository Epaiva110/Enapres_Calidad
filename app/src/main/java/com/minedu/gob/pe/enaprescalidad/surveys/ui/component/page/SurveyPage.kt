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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.fillMaxWidth
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
    variablesConError: Set<String> = emptySet(),
    evaluator: ConditionEvaluator,
    minObsCaracteres: Int = 0,
    soloLectura: Boolean = false,
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
                    pregunta          = pregunta,
                    respuestas        = respuestas,
                    variableEnFoco    = variableEnFoco,
                    variablesConError = variablesConError,
                    editable          = !soloLectura,
                    onValueChange     = onUpdateAnswer,
                )
            }
        }

        if (minObsCaracteres > 0) {
            SectionObservationField(
                obsKey = "OBS_${pagina.seccion_id}",
                texto = respuestas["OBS_${pagina.seccion_id}"]?.toString().orEmpty(),
                minCaracteres = minObsCaracteres,
                tieneError = "OBS_${pagina.seccion_id}" in variablesConError,
                soloLectura = soloLectura,
                onValueChange = onUpdateAnswer,
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SectionObservationField(
    obsKey: String,
    texto: String,
    minCaracteres: Int,
    tieneError: Boolean,
    soloLectura: Boolean,
    onValueChange: (String, Any?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (tieneError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                MaterialTheme.shapes.medium
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Observación de sección *",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (tieneError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Mínimo $minCaracteres caracteres. También editable desde el ícono de notas en la barra superior.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = texto,
            onValueChange = { if (!soloLectura) onValueChange(obsKey, it) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = soloLectura,
            enabled = !soloLectura,
            minLines = 3,
            isError = tieneError,
            supportingText = {
                Text("${texto.trim().length}/$minCaracteres")
            },
            placeholder = { Text("Escriba la observación de esta sección…") },
        )
    }
}