package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyOption

// ═════════════════════════════════════════════════════════════════════════════
//  MATRIX DETAIL  (type = "matrix_detail")
//
//  Matriz de filas con columnas Sí / No (o personalizadas).
//  Cuando la respuesta de una fila es "Sí" (o el valor trigger configurado),
//  aparece un botón de acción que abre un diálogo con sub-preguntas de detalle.
//
//  DIFERENCIA con matrix_scale / matrix:
//   · matrix / matrix_scale → columnas de radio sin sub-preguntas.
//   · matrix_detail          → columnas de radio + botón que abre Dialog
//                              con sub-preguntas definidas en detail_questions
//                              de la opción (fila).
//
//  Ejemplo de JSON:
//  {
//    "variable": "delitos",
//    "type": "matrix_detail",
//    "label": "¿Usted fue víctima de alguno de los siguientes delitos?",
//    "required": true,
//    "options": [
//      {
//        "value": "robo",
//        "label": "Robo",
//        "detail_trigger_value": "1",   ← valor que activa el botón (default "1" = Sí)
//        "detail_questions": [
//          { "variable": "robo_fecha",   "type": "date",   "label": "¿Cuándo ocurrió?",       "required": true },
//          { "variable": "robo_veces",   "type": "number", "label": "¿Cuántas veces?",         "required": true },
//          { "variable": "robo_denuncia","type": "single", "label": "¿Denunció el hecho?",     "required": true,
//            "options": [{"value":"1","label":"Sí"},{"value":"2","label":"No"}] }
//        ]
//      },
//      {
//        "value": "hurto",
//        "label": "Hurto",
//        "detail_trigger_value": "1",
//        "detail_questions": [ ... ]
//      }
//    ]
//  }
//
//  Las columnas por defecto son Sí (value="1") y No (value="2").
//  Se pueden personalizar con scale_labels en la pregunta padre:
//    "scale_labels": ["Sí", "No", "No sabe"]
//  y los valores se generan como "1", "2", "3"...
// ═════════════════════════════════════════════════════════════════════════════

// Columnas por defecto para matrix_detail
private val COLUMNAS_DEFAULT = listOf("1" to "Sí", "2" to "No")

@Composable
fun MatrixDetailQuestion(
    pregunta         : Pregunta,
    respuestas       : Map<String, Any?>,
    variablesConError: Set<String> = emptySet(),
    onValueChange    : (String, Any?) -> Unit,
    editable         : Boolean = true,
) {
    // Construir columnas desde scale_labels o usar default Sí/No
    val columnas: List<Pair<String, String>> = remember(pregunta.scale_labels) {
        val labels = pregunta.scale_labels
        if (!labels.isNullOrEmpty()) {
            labels.mapIndexed { i, label -> (i + 1).toString() to label }
        } else {
            COLUMNAS_DEFAULT
        }
    }

    // Estado del diálogo de detalle
    var detalleAbierto by remember { mutableStateOf<DetalleDialogState?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

        // ── Encabezado de columnas ─────────────────────────────────────────
        MatrixDetailHeader(
            columnas    = columnas,
            scaleLabels = pregunta.scale_labels,
        )

        // ── Filas ─────────────────────────────────────────────────────────
        pregunta.options?.forEachIndexed { index, fila ->
            val subVar     = "${pregunta.variable}_${fila.variable ?: fila.value ?: ""}"
            val seleccion  = respuestas[subVar]?.toString() ?: ""
            val tieneError = subVar in variablesConError
            val esImpar    = index % 2 != 0

            // Valor que activa el botón de detalle (por defecto "1" = Sí)
            val triggerValue = fila.detail_trigger_value ?: "1"
            val tieneDetalle = !fila.detail_questions.isNullOrEmpty()
            val detalleActivo= tieneDetalle && seleccion == triggerValue

            // Contar sub-preguntas respondidas para el badge del botón
            val subRespondidas = if (tieneDetalle) {
                fila.detail_questions!!.count { sub ->
                    val v = respuestas[sub.variable]
                    when (v) {
                        null -> false; is String -> v.isNotBlank()
                        is List<*> -> v.isNotEmpty(); else -> true
                    }
                }
            } else 0
            val totalSubRequeridas = fila.detail_questions?.count { it.required } ?: 0

            MatrixDetailRow(
                fila           = fila,
                subVar         = subVar,
                columnas       = columnas,
                seleccion      = seleccion,
                tieneError     = tieneError,
                esImpar        = esImpar,
                editable       = editable,
                detalleActivo  = detalleActivo,
                tieneDetalle   = tieneDetalle,
                subRespondidas = subRespondidas,
                totalSub       = totalSubRequeridas,
                onSelect       = { valor -> onValueChange(subVar, valor) },
                onAbrirDetalle = {
                    detalleAbierto = DetalleDialogState(
                        tituloFila      = fila.label,
                        subPreguntas    = fila.detail_questions ?: emptyList(),
                        prefijo         = "${pregunta.variable}_${fila.variable ?: fila.value ?: ""}",
                    )
                },
            )
        }
    }

    // ── Diálogo de detalle ─────────────────────────────────────────────────
    val estado = detalleAbierto
    if (estado != null) {
        DetalleDialog(
            estado        = estado,
            respuestas    = respuestas,
            editable      = editable,
            onValueChange = onValueChange,
            onDismiss     = { detalleAbierto = null },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ESTADO DEL DIÁLOGO
// ─────────────────────────────────────────────────────────────────────────────

private data class DetalleDialogState(
    val tituloFila  : String,
    val subPreguntas: List<Pregunta>,
    val prefijo     : String,   // usado para construir variables si fuera necesario
)

// ─────────────────────────────────────────────────────────────────────────────
//  ENCABEZADO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatrixDetailHeader(
    columnas   : List<Pair<String, String>>,
    scaleLabels: List<String>?,
) {
    val colWeight = 1f / columnas.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Espacio para label de fila
        Spacer(Modifier.width(140.dp))
        columnas.forEach { (_, label) ->
            Text(
                text       = label,
                modifier   = Modifier.weight(colWeight),
                textAlign  = TextAlign.Center,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
        }
        // Espacio para columna del botón de acción
        Spacer(Modifier.width(72.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FILA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MatrixDetailRow(
    fila          : SurveyOption,
    subVar        : String,
    columnas      : List<Pair<String, String>>,
    seleccion     : String,
    tieneError    : Boolean,
    esImpar       : Boolean,
    editable      : Boolean,
    detalleActivo : Boolean,
    tieneDetalle  : Boolean,
    subRespondidas: Int,
    totalSub      : Int,
    onSelect      : (String) -> Unit,
    onAbrirDetalle: () -> Unit,
) {
    val colWeight = 1f / columnas.size

    val targetBg = when {
        tieneError    -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f)
        detalleActivo -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        esImpar       -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f)
        else          -> Color.Transparent
    }
    val bg by animateColorAsState(targetValue = targetBg, animationSpec = tween(250), label = "row_bg")

    val borderMod = if (tieneError)
        Modifier.border(1.dp, MaterialTheme.colorScheme.error.copy(0.5f))
    else Modifier

    Column {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .then(borderMod)
                .background(bg)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Label de fila
            Text(
                text      = fila.label,
                modifier  = Modifier.width(140.dp).padding(horizontal = 8.dp),
                fontSize  = 12.sp,
                lineHeight= 16.sp,
                color     = if (tieneError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
            )

            // Radio buttons por columna
            columnas.forEach { (value, _) ->
                val deshabilitada = !editable
                Box(
                    modifier         = Modifier.weight(colWeight),
                    contentAlignment = Alignment.Center,
                ) {
                    RadioButton(
                        selected = seleccion == value,
                        onClick  = { if (!deshabilitada) onSelect(value) },
                        enabled  = !deshabilitada,
                        colors   = RadioButtonDefaults.colors(
                            selectedColor         = MaterialTheme.colorScheme.primary,
                            disabledSelectedColor = MaterialTheme.colorScheme.outline.copy(0.38f),
                        ),
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            // Botón de acción de detalle (solo si hay sub-preguntas Y la respuesta es el trigger)
            Box(
                modifier         = Modifier.width(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (tieneDetalle) {
                    Row(){
                        AnimatedVisibility(visible = detalleActivo) {
                            val todasRespondidas = subRespondidas >= totalSub && totalSub > 0
                            FilledTonalIconButton(
                                onClick  = onAbrirDetalle,
                                enabled  = editable || !editable,  // siempre visible, lectura también
                                modifier = Modifier.size(36.dp),
                                colors   = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = if (todasRespondidas)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.errorContainer,
                                ),
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.OpenInNew,
                                    contentDescription = "Ver detalle de ${fila.label}",
                                    modifier           = Modifier.size(18.dp),
                                    tint               = if (todasRespondidas)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Badge de progreso de sub-preguntas (visible cuando detalleActivo)
        AnimatedVisibility(visible = detalleActivo && tieneDetalle && totalSub > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 148.dp, end = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val completo = subRespondidas >= totalSub
                Icon(
                    imageVector = if (completo) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(11.dp),
                    tint = if (completo) Color(0xFF166534) else MaterialTheme.colorScheme.error,
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    text  = "$subRespondidas/$totalSub completado",
                    fontSize = 10.sp,
                    color = if (completo) Color(0xFF166534) else MaterialTheme.colorScheme.error,
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.25f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIÁLOGO DE DETALLE
//
//  Se abre cuando el usuario pulsa el botón de acción en una fila con Sí.
//  Contiene las sub-preguntas de detalle usando DynamicQuestionAdapter.
//  El diálogo es scrollable para acomodar cualquier número de preguntas.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DetalleDialog(
    estado       : DetalleDialogState,
    respuestas   : Map<String, Any?>,
    editable     : Boolean,
    onValueChange: (String, Any?) -> Unit,
    onDismiss    : () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier         = Modifier.fillMaxWidth(),
        shape            = RoundedCornerShape(16.dp),
        icon = {
            Icon(
                imageVector        = Icons.Default.Article,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Column {
                Text(
                    text       = "Detalle",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text  = estado.tituloFila,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            // Scroll interno para acomodar muchas sub-preguntas
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HorizontalDivider()

                estado.subPreguntas.forEach { sub ->
                    DynamicQuestionAdapter(
                        pregunta      = sub,
                        respuestas    = respuestas,
                        variableEnFoco= "",
                        editable      = editable,
                        onValueChange = onValueChange,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape   = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Listo")
            }
        },
        dismissButton = if (!editable) ({
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }) else null,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  EXTENSIÓN EN SurveyOption — campo adicional para matrix_detail
//
//  Agrega esto a SurveyOption en SurveyModels.kt:
//
//    val detail_trigger_value: String? = "1"
//      → valor de la columna que activa el botón de detalle (default "1" = Sí)
//
//  Y en QuestionAdapter, agrega el case:
//    TipoPregunta.MATRIX_DETAIL ->
//        MatrixDetailQuestion(pregunta, respuestas, variablesConError, onChange, editable)
// ─────────────────────────────────────────────────────────────────────────────