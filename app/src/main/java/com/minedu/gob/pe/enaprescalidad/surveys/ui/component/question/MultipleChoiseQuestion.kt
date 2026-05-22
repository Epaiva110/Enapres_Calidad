package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta

// ═════════════════════════════════════════════════════════════════════════════
//  MULTIPLE CHOICE (Checkbox)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun MultipleChoiceQuestion(
    pregunta        : Pregunta,
    listaSeleccionada: List<*>,
    respuestas      : Map<String, Any?>,
    onValueChange   : (String, Any?) -> Unit,
    editable        : Boolean = true,
) {
    val items = listaSeleccionada.map { it.toString() }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        pregunta.options?.forEach { opcion ->
            val target  = opcion.value ?: opcion.variable ?: ""
            val marcado = items.contains(target)

            ChoiceOptionRow(
                label    = opcion.label,
                selected = marcado,
                type     = ChoiceType.CHECKBOX,
                disabled = !editable || opcion.disabled_if_cols?.let { cols -> items.any { it in cols } } == true,
                onClick  = {
                    val lista = items.toMutableList()
                    if (marcado) {
                        lista.remove(target)
                    } else {
                        if (opcion.is_none == true) lista.clear()
                        else pregunta.options.forEach { if (it.is_none == true) lista.remove(it.value ?: it.variable ?: "") }
                        lista.add(target)
                    }
                    onValueChange(pregunta.variable, lista)
                },
            )
            AnimatedVisibility(visible = marcado && !opcion.detail_questions.isNullOrEmpty()) {
                SubQuestionsBlock(opcion, respuestas, onValueChange, editable)
            }
        }
        if (pregunta.allow_other == true) {
            val otroKey = "${pregunta.variable}_otro"
            val otroVal = respuestas[otroKey]?.toString() ?: ""
            val selOtro = items.contains("__otro__")
            OtroRow(seleccionado = selOtro, variable = pregunta.variable,
                respuestas = respuestas, onValueChange = onValueChange, isRadio = false, editable = editable)
        }
    }
}