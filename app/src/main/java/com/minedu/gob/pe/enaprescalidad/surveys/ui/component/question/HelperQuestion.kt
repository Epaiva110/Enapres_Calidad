package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyOption


// ─────────────────────────────────────────────────────────────────────────────
// Helpers Choice
// ─────────────────────────────────────────────────────────────────────────────

enum class ChoiceType { RADIO, CHECKBOX }

@Composable
fun ChoiceOptionRow(
    label   : String,
    selected: Boolean,
    type    : ChoiceType,
    onClick : () -> Unit,
    disabled: Boolean = false,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f)
        else Color.Transparent,
        label = "choice_bg",
    )

    Surface(
        onClick   = { if (!disabled) onClick() },
        enabled   = !disabled,
        shape     = RoundedCornerShape(8.dp),
        color     = bgColor,
        modifier  = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier             = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (type == ChoiceType.RADIO) {
                RadioButton(
                    selected  = selected,
                    onClick   = { if (!disabled) onClick() },
                    enabled   = !disabled,
                    modifier  = Modifier.size(20.dp),
                )
            } else {
                Checkbox(
                    checked   = selected,
                    onCheckedChange = { if (!disabled) onClick() },
                    enabled   = !disabled,
                    modifier  = Modifier.size(20.dp),
                )
            }
            Text(
                label,
                style    = MaterialTheme.typography.bodyMedium,
                color    = if (disabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun SubQuestionsBlock(
    opcion      : SurveyOption,
    respuestas  : Map<String, Any?>,
    onValueChange: (String, Any?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        opcion.detail_questions?.forEach { sub ->
            DynamicQuestionAdapter(
                pregunta       = sub,
                respuestas     = respuestas,
                variableEnFoco = "",
                onValueChange  = onValueChange,
            )
        }
    }
}

@Composable
fun OtroRow(
    seleccionado: Boolean,
    variable    : String,
    respuestas  : Map<String, Any?>,
    onValueChange: (String, Any?) -> Unit,
    isRadio     : Boolean,
) {
    val otroKey = "${variable}_otro"
    val texto   = respuestas[otroKey]?.toString() ?: ""

    Column {
        ChoiceOptionRow(
            label    = "Otro (especifique)",
            selected = seleccionado,
            type     = if (isRadio) ChoiceType.RADIO else ChoiceType.CHECKBOX,
            onClick  = { onValueChange("${variable}_otro_sel", "__otro__") },
        )
        AnimatedVisibility(visible = seleccionado) {
            OutlinedTextField(
                value         = texto,
                onValueChange = { onValueChange(otroKey, it) },
                modifier      = Modifier.fillMaxWidth().padding(start = 32.dp, top = 4.dp),
                placeholder   = { Text("Especifique…", fontSize = 12.sp) },
                singleLine    = true,
                shape         = RoundedCornerShape(8.dp),
            )
        }
    }
}







//// Helper de padding para subpreguntas
//fun somePaddingValuesParaSangria(left: androidx.compose.ui.unit.Dp) =
//    PaddingValues(start = left, top = 8.dp, end = 0.dp, bottom = 4.dp)