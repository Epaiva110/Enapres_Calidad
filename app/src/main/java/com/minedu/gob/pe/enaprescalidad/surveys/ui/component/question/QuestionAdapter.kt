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
                "text","date","time", "datetime" -> TextQuestion(pregunta, valorActual as? String ?: "", DateInputMode.PickerOrManual, onValueChange)
                "decimal" -> NumberQuestionField(pregunta, valorActual, isDecimal = true, onValueChange)
                "number" -> NumberQuestionField(pregunta, valorActual, isDecimal = false, onValueChange)
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
