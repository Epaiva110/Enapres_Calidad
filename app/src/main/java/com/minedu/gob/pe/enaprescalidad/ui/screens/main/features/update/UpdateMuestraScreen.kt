package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SyncStateMuestra
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UpdateScreen(
    state: SyncStateMuestra = SyncStateMuestra(),
    onSyncConglomerado: () -> Unit,
    onSyncViviendas: () -> Unit,
    onSyncReentrevistas: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        SyncItem(
            title = "Actualizar Conglomerado",
            isLoading = state.loadingConglomerado,
            lastSync = state.lastSyncConglomerado,
            onClick = onSyncConglomerado
        )

        SyncItem(
            title = "Actualizar Viviendas",
            isLoading = state.loadingViviendas,
            lastSync = state.lastSyncViviendas,
            onClick = onSyncViviendas
        )

        SyncItem(
            title = "Actualizar Reentrevistas",
            isLoading = state.loadingReentrevistas,
            lastSync = state.lastSyncReentrevistas,
            onClick = onSyncReentrevistas
        )

        Spacer(modifier = Modifier.height(20.dp))

        SyncStatus(state)
    }
}

@Composable
fun SyncStatus(state: SyncStateMuestra) {
    when {
        state.error != null -> {
            Text(
                text = "Error: ${state.error}",
                color = Color.Red
            )
        }

        state.successMessage != null -> {
            Text(
                text = state.successMessage,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun SyncItem(
    title: String,
    isLoading: Boolean,
    lastSync: Long?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(title)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = lastSync?.let { formatDate(it) }
                ?: "Nunca actualizado",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return "Última actualización: ${sdf.format(Date(timestamp))}"
}