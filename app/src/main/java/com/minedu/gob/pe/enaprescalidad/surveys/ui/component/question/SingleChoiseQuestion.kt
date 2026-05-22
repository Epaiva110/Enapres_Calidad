package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta

// ═════════════════════════════════════════════════════════════════════════════
//  SINGLE CHOICE (Radio)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun SingleChoiceQuestion(
    pregunta     : Pregunta,
    valorActual  : Any?,
    respuestas   : Map<String, Any?>,
    onValueChange: (String, Any?) -> Unit,
    editable     : Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        pregunta.options?.forEach { opcion ->
            val seleccionado = valorActual?.toString() == opcion.value?.toString()
            ChoiceOptionRow(
                label       = opcion.label,
                selected    = seleccionado,
                type        = ChoiceType.RADIO,
                disabled    = !editable,
                onClick     = { onValueChange(pregunta.variable, opcion.value) },
            )
            // Subpreguntas en cascada
            AnimatedVisibility(visible = seleccionado && !opcion.detail_questions.isNullOrEmpty()) {
                SubQuestionsBlock(opcion, respuestas, onValueChange, editable)
            }
        }
        // Opción "Otro (especifique)"
        if (pregunta.allow_other == true) {
            val otroVal = valorActual?.toString()
            val selOtro = pregunta.options?.none { it.value?.toString() == otroVal } == true && otroVal != null
            OtroRow(seleccionado = selOtro, variable = pregunta.variable,
                respuestas = respuestas, onValueChange = onValueChange, isRadio = true, editable = editable)
        }
    }
}