package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta

// ═════════════════════════════════════════════════════════════════════════════
//  SLIDER
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun SliderQuestion(
    pregunta    : Pregunta,
    valor       : Float,
    onValueChange: (String, Any?) -> Unit,
) {
    val min   = pregunta.min_value?.toFloat() ?: 0f
    val max   = pregunta.max_value?.toFloat() ?: 100f
    val step  = pregunta.step?.toFloat() ?: 1f
    val steps = if (step > 0) ((max - min) / step).toInt() - 1 else 0
    val current = valor.coerceIn(min, max)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(min.toInt().toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Valor actual destacado
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text      = if (step < 1f) "%.1f".format(current) else current.toInt().toString(),
                    modifier  = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color     = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize  = 14.sp,
                )
            }

            Text(max.toInt().toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Slider(
            value        = current,
            onValueChange = { onValueChange(pregunta.variable, it) },
            valueRange   = min..max,
            steps        = steps.coerceAtLeast(0),
            modifier     = Modifier.fillMaxWidth(),
        )

        // Labels de escala si los hay
        pregunta.scale_labels?.let { labels ->
            if (labels.size >= 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(labels.first(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic)
                    Text(labels.last(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}
