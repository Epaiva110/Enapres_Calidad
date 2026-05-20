package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta


// ═════════════════════════════════════════════════════════════════════════════
//  LIKERT  (estrellas / emojis / botones numéricos)
// ═════════════════════════════════════════════════════════════════════════════

@Composable
fun LikertQuestion(
    pregunta    : Pregunta,
    valor       : Any?,
    onValueChange: (String, Any?) -> Unit,
) {
    val total    = pregunta.likert_count ?: 5
    val tipo     = pregunta.likert_type ?: "stars"
    val seleccion = valor?.toString()?.toIntOrNull()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        when (tipo) {
            "stars" -> LikertEstrellas(total, seleccion) { onValueChange(pregunta.variable, it.toString()) }
            "smiley" -> LikertSmiley(total, seleccion) { onValueChange(pregunta.variable, it.toString()) }
            else -> LikertNumericos(total, seleccion) { onValueChange(pregunta.variable, it.toString()) }
        }

        // Labels extremos
        pregunta.scale_labels?.let { labels ->
            if (labels.size >= 2) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(labels.first(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(labels.last(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun LikertEstrellas(total: Int, seleccion: Int?, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..total) {
            val activa = seleccion != null && i <= seleccion
            IconButton(onClick = { onSelect(i) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (activa) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "$i",
                    tint = if (activa) Color(0xFFFBBF24) else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Composable
private fun LikertSmiley(total: Int, seleccion: Int?, onSelect: (Int) -> Unit) {
    val emojis = listOf("😡","😞","😐","😊","😄")
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        for (i in 1..total) {
            val activa = seleccion == i
            Surface(
                onClick = { onSelect(i) },
                shape   = CircleShape,
                color   = if (activa) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(emojis.getOrElse(i - 1) { "😐" }, fontSize = if (activa) 26.sp else 22.sp)
                }
            }
        }
    }
}

@Composable
private fun LikertNumericos(total: Int, seleccion: Int?, onSelect: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth(),
    ) {
        for (i in 1..total) {
            val activo = seleccion == i
            Surface(
                onClick = { onSelect(i) },
                shape   = RoundedCornerShape(8.dp),
                color   = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border  = if (!activo) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text       = i.toString(),
                        color      = if (activo) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                        fontSize   = 14.sp,
                    )
                }
            }
        }
    }
}