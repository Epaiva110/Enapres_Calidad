package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.verification.conglomerado

import com.minedu.gob.pe.enaprescalidad.utils.hasInternet
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity

import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoViewModel
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoActions
import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConglomeradoScreen(
    onNavigateCuestionario: (Int) -> Unit,
    viewModel: ConglomeradoViewModel = hiltViewModel(),
    viewModelLogin: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()
    val userId = remember(user) { user?.user ?: "" }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Init: solo cuando cambia el userId (no en cada recomposición)
    LaunchedEffect(userId) { if (userId.isNotBlank()) viewModel.init(userId) }

    HandleUiEffects(uiState, snackbarHostState, viewModel)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->

        ConglomeradoContent(
            uiState = uiState,
            actions = viewModel,
            onNavigate = onNavigateCuestionario,
            userId = userId,
            modifier = Modifier.padding(padding)
        )
    }

}

@Composable
fun HandleUiEffects(
    uiState: ConglomeradoUiState,
    snackbarHostState: SnackbarHostState,
    viewModel: ConglomeradoActions
) {
    LaunchedEffect(uiState.sendSuccess) {
        if (uiState.sendSuccess) {
            snackbarHostState.showSnackbar("✅ Datos enviados correctamente")
            viewModel.clearSendSuccess()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("❌ $it")
            viewModel.clearError()
        }
    }
}

@Composable
fun ConglomeradoContent(
    uiState: ConglomeradoUiState,
    actions: ConglomeradoActions,
    onNavigate: (Int) -> Unit,
    userId: String,
    modifier: Modifier = Modifier
) {
    var showHistorial by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dataset, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Conglomerados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        item { FiltrosSeccion(uiState, actions, userId) }

        if (uiState.filtroCompleto) {
            item {
                AccionesSection(
                    uiState = uiState,
                    actions = actions,
                    onVerMapa = { /* Navegar mapa */ },
                    onVerHistorial = { showHistorial = true }
                )
            }

            if (uiState.muestras.isNotEmpty()) {
                item { CongTableHeader(uiState.modoSeleccion) }

                itemsIndexed(uiState.muestras, key = { _, m -> m.id }) { index, muestra ->
                    CongRow(
                        muestra = muestra,
                        index = index,
                        modoSeleccion = uiState.modoSeleccion,
                        isSeleccionado = muestra.id in uiState.seleccionados,
                        isSending = uiState.isSending,
                        onToggle = { actions.toggleSeleccion(muestra.id) },
                        onEnviarUna = { actions.onEnviarTodas(false) /* Ajustar a onEnviarUna si existe */ },
                        onClickFila = {
                            if (uiState.modoSeleccion) actions.toggleSeleccion(muestra.id)
                            else onNavigate(muestra.id)
                        }
                    )
                }
            } else if (!uiState.isLoadingMuestras) {
                item { EmptyStateComponent() }
            }
        }

        if (uiState.isLoadingMuestras) {
            item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }
    }

    if (showHistorial) {
        UltimaFechaDialog(fecha = uiState.ultimaFechaEnvio, onDismiss = { showHistorial = false })
    }
}

@Composable
fun EmptyStateComponent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Usamos un icono de "búsqueda sin resultados" con color atenuado
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Text(
            text = "No se encontraron conglomerados",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Intenta cambiar los filtros de búsqueda.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun UltimaFechaDialog(
    fecha: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Text(text = "Historial de Sincronización", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Último envío registrado:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = fecha ?: "No hay envíos previos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (fecha != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}

@Composable
fun FiltrosSeccion(
    uiState: ConglomeradoUiState,
    actions: ConglomeradoActions,
    userId: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Periodo de trabajo",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fila 1: Año y Mes
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroCombo(
                        label = "Año",
                        opciones = uiState.anios,
                        seleccion = uiState.anioSel?.toString(),
                        enabled = uiState.anios.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        display = { it.toString() },
                        onSelect = { actions.onAnioSelected(userId, it) }
                    )
                    FiltroCombo(
                        label = "Mes",
                        opciones = uiState.meses,
                        seleccion = uiState.mesSel?.let { obtenerNombreMes(it) },
                        enabled = uiState.meses.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        display = { obtenerNombreMes(it) },
                        onSelect = { actions.onMesSelected(userId, it) }
                    )
                }

                // Fila 2: Periodo y Proyecto
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FiltroCombo(
                        label = "Periodo",
                        opciones = uiState.periodos,
                        seleccion = uiState.periodoSel?.let { "P$it" },
                        enabled = uiState.periodos.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        display = { "P$it" },
                        onSelect = { actions.onPeriodoSelected(userId, it) }
                    )
                    FiltroCombo(
                        label = "Proyecto",
                        opciones = uiState.proyectos,
                        seleccion = uiState.proyectoSel?.toString(),
                        enabled = uiState.proyectos.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        display = { it.toString() },
                        onSelect = { actions.onProyectoSelected(userId, it) }
                    )
                }
            }
        }
    }
}

// Función auxiliar para nombres de meses
private fun obtenerNombreMes(mes: Int): String {
    return listOf("", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        .getOrElse(mes) { mes.toString() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FiltroCombo(
    label: String,
    opciones: List<T>,
    seleccion: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    display: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        //onExpandedChange = { if (enabled) expanded = it },
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = seleccion ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text    = { Text(display(opcion), style = MaterialTheme.typography.bodySmall) },
                    onClick = { onSelect(opcion); expanded = false },
                )
            }
        }
    }
}


/**/
@Composable
private fun CongTableHeader(modoSeleccion: Boolean) {
    val style = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (modoSeleccion) Spacer(Modifier.width(36.dp))
        Text("ID", Modifier.weight(1f), style = style)
        Text("CONGLOMERADO", Modifier.weight(2.5f), style = style)
        Text("UBIGEO", Modifier.weight(2f), style = style)
        Text("ESTADO", Modifier.weight(1.2f), style = style, textAlign = TextAlign.Center)
        if (!modoSeleccion) Spacer(Modifier.width(40.dp))
    }
}

@Composable
private fun CongRow(
    muestra: MuestraConglomeradoEntity,
    index: Int,
    modoSeleccion: Boolean,
    isSeleccionado: Boolean,
    isSending: Boolean,
    onToggle: () -> Unit,
    onEnviarUna: () -> Unit,
    onClickFila: () -> Unit
) {
    val backgroundColor = when {
        isSeleccionado -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        index % 2 != 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClickFila() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (modoSeleccion) {
            Checkbox(checked = isSeleccionado, onCheckedChange = { onToggle() }, modifier = Modifier.size(36.dp))
        }

        Text(muestra.idcong, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)

        Column(Modifier.weight(2.5f)) {
            Text(muestra.conglomerado, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(muestra.odeienapres, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(Modifier.weight(2f)) {
            Text(muestra.departamento, style = MaterialTheme.typography.labelSmall)
            Text("${muestra.provincia}/${muestra.distrito}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }

        // Estado con el estilo de "Pill" original
        Box(modifier = Modifier.weight(1.2f), contentAlignment = Alignment.Center) {
            val (colorBg, colorTxt, label) = if (muestra.sincronizado) {
                Triple(Color(0xFFDCFCE7), Color(0xFF166534), "Enviado")
            } else {
                Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "Pendiente")
            }
            Surface(color = colorBg, shape = CircleShape) {
                Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colorTxt)
            }
        }

        if (!modoSeleccion) {
            Box(Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                if (!muestra.sincronizado) {
                    IconButton(onClick = onEnviarUna, enabled = !isSending, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AccionesSection(
    uiState: ConglomeradoUiState,
    actions: ConglomeradoActions,
    onVerMapa: () -> Unit,
    onVerHistorial: () -> Unit
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Barra de conteo
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)).padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${uiState.muestras.size} unidades", style = MaterialTheme.typography.labelMedium)
            Text(
                if (uiState.pendientesTotal > 0) "${uiState.pendientesTotal} pendientes" else "Al día ✓",
                color = if (uiState.pendientesTotal > 0) Color(0xFFF59E0B) else Color(0xFF22C55E),
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold
            )
        }

        // Botonera principal
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(42.dp)) {
            // Botón Enviar Dinámico
            Button(
                onClick = {
                    val online = hasInternet(context)
                    if (uiState.modoSeleccion) actions.onEnviarSeleccionadas(online) else actions.onEnviarTodas(online)
                },
                modifier = Modifier.weight(1.5f),
                enabled = !uiState.isSending && (if(uiState.modoSeleccion) uiState.seleccionados.isNotEmpty() else uiState.pendientesTotal > 0),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState.isSending) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else {
                    Icon(Icons.Default.CloudUpload, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (uiState.modoSeleccion) "Enviar (${uiState.seleccionados.size})" else "Enviar Todas", fontSize = 12.sp)
                }
            }

            // Toggle Selección
            FilledTonalButton(
                onClick = { actions.toggleModoSeleccion() },
                modifier = Modifier.weight(1.2f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (uiState.modoSeleccion) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Icon(if (uiState.modoSeleccion) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (uiState.modoSeleccion) "Cancelar" else "Seleccionar", fontSize = 11.sp)
            }

            // Mapa e Historial (Iconos rápidos)
            OutlinedIconButton(onClick = onVerMapa, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.Map, null, Modifier.size(18.dp))
            }
            OutlinedIconButton(onClick = onVerHistorial, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.History, null, Modifier.size(18.dp))
            }
        }

        // Acciones masivas (solo en modo selección)
        AnimatedVisibility(visible = uiState.modoSeleccion) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { actions.seleccionarTodosPendientes() }) { Text("Todo", fontSize = 12.sp) }
                TextButton(onClick = { actions.deseleccionarTodos() }) { Text("Ninguno", fontSize = 12.sp) }
            }
        }
    }
}