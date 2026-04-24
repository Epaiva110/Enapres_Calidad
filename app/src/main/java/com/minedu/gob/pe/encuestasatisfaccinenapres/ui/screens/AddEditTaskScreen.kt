package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.entity.Priority
import com.minedu.gob.pe.encuestasatisfaccinenapres.viewmodel.TaskViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    viewModel: TaskViewModel,
    taskId: Int?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val existingTask = remember(taskId, uiState.tasks) {
        taskId?.let { id -> uiState.tasks.find { it.id == id } }
    }

    var title by remember(existingTask) { mutableStateOf(existingTask?.title ?: "") }
    var description by remember(existingTask) { mutableStateOf(existingTask?.description ?: "") }
    var priority by remember(existingTask) { mutableStateOf(existingTask?.priority ?: Priority.MEDIUM) }
    var titleError by remember { mutableStateOf(false) }

    val isEditing = existingTask != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Tarea" else "Nueva Tarea", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@ExtendedFloatingActionButton
                    }
                    if (isEditing) {
                        viewModel.updateTask(existingTask!!.copy(title = title.trim(), description = description.trim(), priority = priority))
                    } else {
                        viewModel.addTask(title, description, priority)
                    }
                    onNavigateBack()
                },
                icon = { Icon(Icons.Default.Save, null) },
                text = { Text(if (isEditing) "Actualizar" else "Guardar") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Título *") },
                modifier = Modifier.fillMaxWidth(),
                isError = titleError,
                supportingText = if (titleError) {{ Text("El título es obligatorio") }} else null,
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Text("Prioridad", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    val (containerColor, label) = when (p) {
                        Priority.HIGH -> MaterialTheme.colorScheme.errorContainer to "Alta"
                        Priority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer to "Media"
                        Priority.LOW -> MaterialTheme.colorScheme.secondaryContainer to "Baja"
                    }
                    FilterChip(
                        selected = priority == p,
                        onClick = { priority = p },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}
