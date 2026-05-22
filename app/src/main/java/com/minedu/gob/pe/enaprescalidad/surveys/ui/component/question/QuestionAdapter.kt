package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta

@Composable
fun DynamicQuestionAdapter(
    pregunta: Pregunta,
    respuestas: Map<String, Any?>,
    variableEnFoco: String,
    variablesConError: Set<String> = emptySet(),
    editable: Boolean = true,
    onValueChange: (String, Any?) -> Unit,
    modifier: Modifier = Modifier
) {
    val onChange: (String, Any?) -> Unit = if (editable) onValueChange else { _, _ -> }
    val valorActual = respuestas[pregunta.variable]
    val estaEnFoco = pregunta.variable == variableEnFoco
    val tieneError = pregunta.variable in variablesConError

    QuestionCard(pregunta = pregunta, estaEnFoco = estaEnFoco, tieneError = tieneError, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Renderizador del componente base según el nuevo esquema
            when (pregunta.type.lowercase()) {
                "info" -> InfoQuestion(pregunta)
                "text","date","time", "datetime" -> TextQuestion(pregunta, valorActual as? String ?: "", DateInputMode.PickerOrManual, onChange, editable)
                "decimal" -> NumberQuestionField(pregunta, valorActual, isDecimal = true, onChange, editable)
                "number" -> NumberQuestionField(pregunta, valorActual, isDecimal = false, onChange, editable)
                "single" -> SingleChoiceQuestion(pregunta, valorActual, respuestas, onChange, editable)
                "multiple", "multiple_binary" -> MultipleChoiceQuestion(pregunta, valorActual as? List<*> ?: emptyList<Any>(), respuestas, onChange, editable)
                "matrix", "matrix_scale" -> MatrixQuestionAdapter(pregunta, respuestas, variablesConError, onChange, editable)
                "slider" -> SliderQuestion(pregunta, valorActual as? Float ?: 0f, onChange, editable)
                "likert" -> LikertQuestion(pregunta, valorActual, onChange, editable)
                "ranking" -> RankingQuestion(pregunta, valorActual as? String ?: "", onChange, editable)
                "gps" -> GpsQuestion(pregunta, valorActual as? String ?: "", onChange, editable)
                "photo" -> PhotoQuestion(pregunta, valorActual as? List<*> ?: emptyList<Any>(), onChange, editable)
                else -> Text("Componente no soportado: ${pregunta.type}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}