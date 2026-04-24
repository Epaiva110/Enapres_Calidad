package com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioListScreen(
    onVerMuestras: (Int) -> Unit,
    viewModel: UsuarioViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<UsuarioEntity?>(null) }

    // Snackbar
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let { snackbarHost.showSnackbar(it); viewModel.clearMessage() }
        state.successMessage?.let { snackbarHost.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = { Text("Usuarios", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Refresh, "Sincronizar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.PersonAdd, "Agregar usuario")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.usuarios.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Group, null, modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No hay usuarios", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.usuarios, key = { it.usuario }) { usuario ->
                        UsuarioCard(
                            usuario = usuario,
                            onEdit = { editTarget = usuario },
                            onDelete = { viewModel.addUsuario(usuario.usuario, usuario.password, !usuario.estado) },
                            onVerMuestras = { onVerMuestras(usuario.usuario.toInt()) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        UsuarioDialog(
            title = "Nuevo Usuario",
            onDismiss = { showAddDialog = false },
            onConfirm = { u, p, e ->
                viewModel.addUsuario(u, p, e)
                showAddDialog = false
            }
        )
    }

    editTarget?.let { target ->
        UsuarioDialog(
            title = "Editar Usuario",
            initialUsuario = target.usuario,
            initialPassword = target.password,
            initialEstado = target.estado,
            onDismiss = { editTarget = null },
            onConfirm = { u, p, e ->
                viewModel.updateUsuario(target.copy(usuario = u, password = p, estado = e))
                editTarget = null
            }
        )
    }
}

@Composable
fun UsuarioCard(
    usuario: UsuarioEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onVerMuestras: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario.usuario.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.usuario, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    val (estadoColor, estadoLabel) = if (usuario.estado)
                        MaterialTheme.colorScheme.primary to "Activo"
                    else
                        MaterialTheme.colorScheme.error to "Inactivo"
                    Surface(
                        color = estadoColor.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            estadoLabel,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = estadoColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Muestras button
            IconButton(onClick = onVerMuestras) {
                Icon(Icons.Default.Science, "Ver muestras", tint = MaterialTheme.colorScheme.primary)
            }

            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Opciones")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { showMenu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
fun UsuarioDialog(
    title: String,
    initialUsuario: String = "",
    initialPassword: String = "",
    initialEstado: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean) -> Unit
) {
    var usuario by remember { mutableStateOf(initialUsuario) }
    var password by remember { mutableStateOf(initialPassword) }
    var estado by remember { mutableStateOf(initialEstado) }
    var passwordVisible by remember { mutableStateOf(false) }
    var usuarioError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it; usuarioError = false },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = usuarioError,
                    supportingText = if (usuarioError) {{ Text("Campo obligatorio") }} else null,
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = false },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError,
                    supportingText = if (passwordError) {{ Text("Campo obligatorio") }} else null,
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = estado, onCheckedChange = { estado = it })
                    Spacer(Modifier.width(8.dp))
                    Text(if (estado) "Activo" else "Inactivo")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                usuarioError = usuario.isBlank()
                passwordError = password.isBlank()
                if (!usuarioError && !passwordError) onConfirm(usuario.trim(), password.trim(), estado)
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
