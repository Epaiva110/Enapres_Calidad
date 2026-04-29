package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update

import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SyncStateMuestra
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncType
import com.minedu.gob.pe.enaprescalidad.utils.hasInternet
import com.minedu.gob.pe.enaprescalidad.viewmodel.MuestraConglomeradoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// -------------------- STATE --------------------

data class SyncStateMuestra(
    val loadingConglomerado: Boolean = false,
    val loadingViviendas: Boolean = false,
    val loadingReentrevistas: Boolean = false,

    val lastSyncConglomerado: Long? = null,
    val lastSyncViviendas: Long? = null,
    val lastSyncReentrevistas: Long? = null,

    val successMessage: String? = null,
    val error: String? = null
)

// -------------------- SCREEN --------------------

//@Composable
//fun UpdateScreen(
//    state: SyncStateMuestra = SyncStateMuestra(),
//    onSyncConglomerado: () -> Unit,
//    onSyncViviendas: () -> Unit,
//    onSyncReentrevistas: () -> Unit,
//    onSyncAll: () -> Unit,
//    viewModel: MuestraConglomeradoViewModel = hiltViewModel()
//
//) {
//    val isAnyLoading = state.loadingConglomerado ||
//            state.loadingViviendas ||
//            state.loadingReentrevistas
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//
//        // 🔥 BOTÓN PRINCIPAL
//        Button(
//            onClick = onSyncAll,
//            enabled = !isAnyLoading,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            if (isAnyLoading) {
//                CircularProgressIndicator(
//                    modifier = Modifier.size(16.dp),
//                    strokeWidth = 2.dp
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//            }
//            Text("Actualizar todo")
//        }
//
//        SyncItem(
//            "Actualizar Conglomerado",
//            state.loadingConglomerado,
//            state.lastSyncConglomerado,
//            onSyncConglomerado {
//                viewModel.SyncConglomerado(hasInternet(context))
//            },
//            !isAnyLoading)
//        SyncItem("Actualizar Viviendas", state.loadingViviendas, state.lastSyncViviendas, onSyncViviendas, !isAnyLoading)
//        SyncItem("Actualizar Reentrevistas", state.loadingReentrevistas, state.lastSyncReentrevistas, onSyncReentrevistas, !isAnyLoading)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        SyncStatus(state)
//    }
//}


@Composable
fun UpdateScreen(
    userId: String,
    viewModel: MuestraConglomeradoViewModel = hiltViewModel()
) {
    val state by viewModel.observe(userId).collectAsState()

    val isAnyLoading = state.any { it.isSyncing }

    fun get(type: SyncType) =
        state.find { it.type == type.name }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // 🔥 SYNC ALL
        Button(
            onClick = { viewModel.syncAll(userId, true) },
            enabled = !isAnyLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isAnyLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Actualizar todo")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🧩 CONGLOMERADO
        SyncItem(
            title = "Actualizar Conglomerado",
            lastSync = get(SyncType.CONGLOMERADO)?.lastSync,
            isLoading = get(SyncType.CONGLOMERADO)?.isSyncing == true,
            enabled = !isAnyLoading,
            onClick = {
                viewModel.sync(SyncType.CONGLOMERADO, userId, true)
            }
        )

        // 🧩 VIVIENDA
        SyncItem(
            title = "Actualizar Viviendas",
            lastSync = get(SyncType.VIVIENDA)?.lastSync,
            isLoading = get(SyncType.VIVIENDA)?.isSyncing == true,
            enabled = !isAnyLoading,
            onClick = {
                viewModel.sync(SyncType.VIVIENDA, userId, true)
            }
        )

        // 🧩 REENTREVISTA
        SyncItem(
            title = "Actualizar Reentrevistas",
            lastSync = get(SyncType.REENTREVISTA)?.lastSync,
            isLoading = get(SyncType.REENTREVISTA)?.isSyncing == true,
            enabled = !isAnyLoading,
            onClick = {
                viewModel.sync(SyncType.REENTREVISTA, userId, true)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 📊 STATUS GENERAL
        SyncStatus(state)
    }
}


@Composable
fun SyncItem(
    title: String,
    lastSync: Long?,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column {

        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(title)
        }

        Text(
            text = lastSync?.let { formatDate(it) }
                ?: "Nunca actualizado",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SyncStatus(state: List<SyncEntity>) {

    val errors = state.filter { it.lastError != null }

    Column {
        errors.forEach {
            Text(
                text = "${it.type}: ${it.lastError}",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

//@Composable
//fun SyncItem(
//    title: String,
//    isLoading: Boolean,
//    lastSync: Long?,
//    onClick: () -> Unit,
//    enabled: Boolean
//) {
//    Column {
//        Button(
//            onClick = onClick,
//            enabled = enabled && !isLoading,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                if (isLoading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(16.dp),
//                        strokeWidth = 2.dp
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                }
//                Text(title)
//            }
//        }
//
//        Text(
//            text = lastSync?.let { formatDate(it) } ?: "Nunca actualizado",
//            style = MaterialTheme.typography.bodySmall
//        )
//    }
//}

//@Composable
//fun SyncStatus(state: SyncStateMuestra) {
//    when {
//        state.error != null -> Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
//        state.successMessage != null -> Text(state.successMessage, color = MaterialTheme.colorScheme.primary)
//    }
//}

// -------------------- DATE --------------------

private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

fun formatDate(timestamp: Long): String {
    return "Última actualización: ${sdf.format(Date(timestamp))}"
}


//
//@Preview(showBackground = true)
//@Composable
//fun PreviewFull() {
//    MaterialTheme {
//        UpdateScreen(
//            state = SyncStateMuestra(
//                lastSyncConglomerado = System.currentTimeMillis(),
//                lastSyncViviendas = System.currentTimeMillis() - 500000,
//                successMessage = "Todo actualizado"
//            ),
//            onSyncConglomerado = {},
//            onSyncViviendas = {},
//            onSyncReentrevistas = {},
//            onSyncAll = {}
//        )
//    }
//}