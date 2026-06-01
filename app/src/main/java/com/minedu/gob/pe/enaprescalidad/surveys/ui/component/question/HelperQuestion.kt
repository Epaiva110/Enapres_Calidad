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

const val OTRO_MIN_CHARS = 3

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
        onClick  = { if (!disabled) onClick() },
        enabled  = !disabled,
        shape    = RoundedCornerShape(8.dp),
        color    = bgColor,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (type == ChoiceType.RADIO) {
                RadioButton(
                    selected = selected,
                    onClick  = { if (!disabled) onClick() },
                    enabled  = !disabled,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Checkbox(
                    checked         = selected,
                    onCheckedChange = { if (!disabled) onClick() },
                    enabled         = !disabled,
                    modifier        = Modifier.size(20.dp),
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
    opcion       : SurveyOption,
    respuestas   : Map<String, Any?>,
    onValueChange: (String, Any?) -> Unit,
    editable     : Boolean = true,
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
                pregunta      = sub,
                respuestas    = respuestas,
                variableEnFoco= "",
                editable      = editable,
                onValueChange = onValueChange,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  OTRO ROW  — FIX: el especifique es requerido (min 3 chars) cuando se
//  selecciona "Otro". Mientras el texto no cumpla el mínimo se muestra error
//  y la variable principal queda incompleta para la validación del ViewModel.
//
//  Contrato de variables en Room:
//   · "${variable}_otro"     → texto del especifique
//   · La variable principal  → "__otro__" cuando Otro está seleccionado
//
//  El ViewModel valida que si variable == "__otro__" entonces
//  "${variable}_otro" tenga al menos OTRO_MIN_CHARS caracteres.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OtroRow(
    seleccionado : Boolean,
    variable     : String,
    respuestas   : Map<String, Any?>,
    onValueChange: (String, Any?) -> Unit,
    isRadio      : Boolean,
    editable     : Boolean = true,
) {
    val otroKey  = "${variable}_otro"
    val texto    = respuestas[otroKey]?.toString() ?: ""
    val tieneError = seleccionado && texto.trim().length < OTRO_MIN_CHARS

    Column {
        ChoiceOptionRow(
            label    = "Otro (especifique)",
            selected = seleccionado,
            type     = if (isRadio) ChoiceType.RADIO else ChoiceType.CHECKBOX,
            disabled = !editable,
            onClick  = {
                // Marcar la variable principal como "__otro__"
                onValueChange(variable, "__otro__")
            },
        )

        AnimatedVisibility(
            visible = seleccionado,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut(),
        ) {
            Column(modifier = Modifier.padding(start = 32.dp, top = 4.dp)) {
                OutlinedTextField(
                    value         = texto,
                    onValueChange = { nuevo ->
                        onValueChange(otroKey, nuevo)
                    },
                    readOnly      = !editable,
                    enabled       = editable,
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Especifique…", fontSize = 12.sp) },
                    singleLine    = true,
                    shape         = RoundedCornerShape(8.dp),
                    isError       = tieneError,
                    supportingText = {
                        if (tieneError) {
                            Text(
                                "Debe especificar al menos $OTRO_MIN_CHARS caracteres",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                            )
                        } else {
                            Text(
                                "${texto.trim().length}/$OTRO_MIN_CHARS mín.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            }
        }
    }
}