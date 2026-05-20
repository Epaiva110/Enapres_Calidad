package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta


// ═════════════════════════════════════════════════════════════════════════════
//  RANKING  (ordenamiento con botones ↑ ↓ — sin drag que no funciona en LazyColumn)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun RankingQuestion(
    pregunta    : Pregunta,
    valorActual : String,
    onValueChange: (String, Any?) -> Unit,
) {
    val orden = remember(valorActual) {
        if (valorActual.isBlank()) {
            pregunta.options?.map { it.value ?: it.variable ?: "" }?.toMutableStateList()
                ?: mutableStateListOf()
        } else {
            valorActual.split(",").toMutableStateList()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Ordena de mayor a menor prioridad usando las flechas",
            fontSize = 11.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        orden.forEachIndexed { i, value ->
            val label = pregunta.options?.find { it.value == value || it.variable == value }?.label ?: value

            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
                border   = BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f)
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier             = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Número de posición
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(26.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                "${i + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 12.sp,
                                color      = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)

                    // Controles
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        IconButton(
                            onClick  = {
                                if (i > 0) { orden.swap(i, i - 1); onValueChange(pregunta.variable, orden.joinToString(",")) }
                            },
                            enabled  = i > 0,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick  = {
                                if (i < orden.lastIndex) { orden.swap(i, i + 1); onValueChange(pregunta.variable, orden.joinToString(",")) }
                            },
                            enabled  = i < orden.lastIndex,
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun <T> MutableList<T>.swap(a: Int, b: Int) { val t = this[a]; this[a] = this[b]; this[b] = t }