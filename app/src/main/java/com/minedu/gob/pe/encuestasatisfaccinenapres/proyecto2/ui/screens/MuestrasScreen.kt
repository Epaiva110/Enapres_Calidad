package com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.MuestraEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuestrasScreen(
    usuarioId: String,
    onBack: () -> Unit,
    viewModel: UsuarioViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(usuarioId) {
        val usuario = state.usuarios.find { it.usuario == usuarioId }
        usuario?.let { viewModel.addUsuario(usuario.usuario, usuario.password, !usuario.estado) }
    }

    val usuario = state.usuarios.find { it.usuario == usuarioId }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Muestras", fontWeight = FontWeight.Bold)
                        usuario?.let {
                            Text(it.usuario, style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.clearSelection(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Agregar muestra")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.muestras.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Science, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No hay muestras para este usuario",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.muestras, key = { it.usuario }) { muestra ->
                        MuestraCard(
                            muestra = muestra,
                            onDelete = { viewModel.deleteMuestra(muestra) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMuestraDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre ->
                viewModel.addMuestra(usuarioId, nombre)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MuestraCard(muestra: MuestraEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Biotech,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(muestra.idcong, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("ID: ${muestra.idcong}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddMuestraDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Muestra", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; error = false },
                label = { Text("Nombre de muestra") },
                modifier = Modifier.fillMaxWidth(),
                isError = error,
                supportingText = if (error) {{ Text("Campo obligatorio") }} else null,
                leadingIcon = { Icon(Icons.Default.Science, null) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                if (nombre.isBlank()) { error = true; return@Button }
                onConfirm(nombre.trim())
            }) { Text("Agregar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
