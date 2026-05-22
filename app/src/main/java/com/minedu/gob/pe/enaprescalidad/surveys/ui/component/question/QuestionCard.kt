package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta

@Composable
fun QuestionCard(
    pregunta: Pregunta,
    estaEnFoco: Boolean,
    tieneError: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val targetBorderColor = when {
        tieneError -> MaterialTheme.colorScheme.error
        estaEnFoco -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }
    val borderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(300),
        label = "border_color"
    )
    val targetBg = if (tieneError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.10f)
    else MaterialTheme.colorScheme.surface
    val cardBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(300),
        label = "card_bg"
    )
    val borderWidth = if (estaEnFoco || tieneError) 2.dp else 1.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (pregunta.type != "info") {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = pregunta.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (pregunta.required) {
                        Text(" *", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
                if (tieneError) {
                    Text(
                        text = "Este campo es requerido",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                pregunta.hint?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            content()
        }
    }
}