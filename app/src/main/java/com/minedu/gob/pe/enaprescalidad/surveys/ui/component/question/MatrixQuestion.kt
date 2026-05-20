package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta

// ═════════════════════════════════════════════════════════════════════════════
//  MATRIX  (tabla real con encabezados de columna)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun MatrixQuestionAdapter(
    pregunta    : Pregunta,
    respuestas  : Map<String, Any?>,
    onValueChange: (String, Any?) -> Unit,
) {
    // Columnas: para matrix_scale se generan numéricamente; para matrix se usan labels fijos
    val isScale = pregunta.type == "matrix_scale"
    val columnas: List<Pair<String, String>> = if (isScale) {
        val a = pregunta.scale_min ?: 1
        val b = pregunta.scale_max ?: 5
        (a..b).map { it.toString() to it.toString() }
    } else {
        listOf("1" to "Sí", "2" to "No", "3" to "N/A")
    }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

        // ── Encabezado de columnas ─────────────────────────────────────────
        MatrixHeader(columnas = columnas, isScale = isScale,
            scaleLabels = pregunta.scale_labels)

        // ── Filas ─────────────────────────────────────────────────────────
        pregunta.options?.forEachIndexed { index, fila ->
            val subVar     = "${pregunta.variable}_${fila.variable ?: fila.value ?: ""}"
            val seleccion  = respuestas[subVar]?.toString() ?: ""
            val esImpar    = index % 2 != 0

            MatrixRow(
                label        = fila.label,
                columnas     = columnas,
                seleccion    = seleccion,
                disabledCols = fila.disabled_if_cols ?: emptyList(),
                isImpar      = esImpar,
                onSelect     = { col -> onValueChange(subVar, col) },
            )

            // Subpreguntas de fila (si aplica)
            if (seleccion.isNotEmpty() && !fila.detail_questions.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp, bottom = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.3f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    fila.detail_questions.forEach { sub ->
                        DynamicQuestionAdapter(sub, respuestas, "", onValueChange)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixHeader(
    columnas   : List<Pair<String, String>>,
    isScale    : Boolean,
    scaleLabels: List<String>?,
) {
    val colWeight = 1f / columnas.size

    Column {
        // Etiquetas extremos de escala (si hay)
        if (isScale && !scaleLabels.isNullOrEmpty() && scaleLabels.size >= 2) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 140.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(scaleLabels.first(), fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = FontStyle.Italic)
                Text(scaleLabels.last(), fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = FontStyle.Italic)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Celda vacía para el label de fila
            Spacer(Modifier.width(140.dp))
            columnas.forEach { (_, label) ->
                Text(
                    text      = label,
                    modifier  = Modifier.weight(colWeight),
                    textAlign = TextAlign.Center,
                    fontSize  = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color     = MaterialTheme.colorScheme.primary,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MatrixRow(
    label       : String,
    columnas    : List<Pair<String, String>>,
    seleccion   : String,
    disabledCols: List<String>,
    isImpar     : Boolean,
    onSelect    : (String) -> Unit,
) {
    val colWeight = 1f / columnas.size
    val bg = if (isImpar) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f) else Color.Transparent

    Row(
        modifier          = Modifier.fillMaxWidth().background(bg).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Label de fila
        Text(
            text     = label,
            modifier = Modifier.width(140.dp).padding(horizontal = 8.dp),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color    = MaterialTheme.colorScheme.onSurface,
        )

        // Botones de columna
        columnas.forEach { (value, _) ->
            val deshabilitada = disabledCols.contains(value)
            val seleccionada  = seleccion == value

            Box(
                modifier          = Modifier.weight(colWeight),
                contentAlignment  = Alignment.Center,
            ) {
                RadioButton(
                    selected  = seleccionada,
                    onClick   = { if (!deshabilitada) onSelect(value) },
                    enabled   = !deshabilitada,
                    colors    = RadioButtonDefaults.colors(
                        selectedColor   = MaterialTheme.colorScheme.primary,
                        disabledSelectedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
                    ),
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
}
