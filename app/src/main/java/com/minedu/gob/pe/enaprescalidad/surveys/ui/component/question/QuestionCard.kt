package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta

@Composable
fun QuestionCard(
    pregunta: Pregunta,
    estaEnFoco: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val borderColor = if (estaEnFoco) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val borderWidth = if (estaEnFoco) 2.dp else 1.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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