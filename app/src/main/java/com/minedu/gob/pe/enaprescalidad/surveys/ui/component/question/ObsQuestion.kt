package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
//  DIÁLOGO OBSERVACIÓN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ObservacionDialog(
    titulo: String, textoInicial: String, minChars: Int,
    onSave: (String) -> Unit, onDismiss: () -> Unit,
) {
    var texto by remember { mutableStateOf(textoInicial) }
    val valido = texto.trim().length >= minChars

    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Default.EditNote, null) },
        title = { Text("Observación: $titulo") },
        text  = {
            Column {
                OutlinedTextField(
                    value         = texto,
                    onValueChange = { texto = it },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder   = { Text("Describa la situación observada...") },
                    shape         = RoundedCornerShape(8.dp),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${texto.length}/$minChars caracteres mínimos",
                    fontSize = 12.sp,
                    color    = if (valido) Color(0xFF22C55E) else MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(texto) }, enabled = valido) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    )
}