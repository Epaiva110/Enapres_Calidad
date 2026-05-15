package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.verification.conglomerado

import com.minedu.gob.pe.enaprescalidad.utils.hasInternet
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.ui.screens.login.sesion.SessionManager
import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoUiState
import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoViewModel
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginViewModel

// ─────────────────────────────────────────────────────────────────────────────
//  ENTRY POINT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ConglomeradoScreen(
    viewModel: ConglomeradoViewModel = hiltViewModel(),
    viewModelLogin: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()
    val userId = user?.user ?: ""
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Inicializa los combos cuando el userId esté disponible
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) viewModel.init(userId)
    }

    // Snackbar de éxito al enviar
    LaunchedEffect(uiState.sendSuccess) {
        if (uiState.sendSuccess) {
            snackbarHostState.showSnackbar("Datos enviados correctamente")
            viewModel.clearSendSuccess()
        }
    }

    // Snackbar de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearError()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ConglomeradoContent(
            uiState = uiState,
            onAnioSelected    = { viewModel.onAnioSelected(userId, it) },
            onMesSelected     = { viewModel.onMesSelected(userId, it) },
            onPeriodoSelected = { viewModel.onPeriodoSelected(userId, it) },
            onProyectoSelected = { viewModel.onProyectoSelected(userId, it) },
            onEnviar          = { viewModel.onEnviar(userId, hasInternet(context)) },
            modifier = Modifier.padding(padding),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PANTALLA STATELESS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ConglomeradoContent(
    uiState: ConglomeradoUiState,
    onAnioSelected: (Int) -> Unit,
    onMesSelected: (Int) -> Unit,
    onPeriodoSelected: (Int) -> Unit,
    onProyectoSelected: (Int) -> Unit,
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showUltimaFecha by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {

        // ── Título ────────────────────────────────────────────────────────────
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Dataset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Conglomerados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // ── Card de filtros ───────────────────────────────────────────────────
        item {
            FiltrosCard(
                uiState = uiState,
                onAnioSelected = onAnioSelected,
                onMesSelected = onMesSelected,
                onPeriodoSelected = onPeriodoSelected,
                onProyectoSelected = onProyectoSelected,
            )
        }

        // ── Conteo + botones ──────────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = uiState.filtroCompleto,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                AccionesRow(
                    muestras = uiState.muestras,
                    pendientes = uiState.pendientesEnvio,
                    isSending = uiState.isSending,
                    onEnviar = onEnviar,
                    onVerMapa = { /* TODO: navegar a pantalla de mapa */ },
                    onVerUltimaFecha = { showUltimaFecha = true },
                )
            }
        }

        // ── Separador ─────────────────────────────────────────────────────────
        if (uiState.muestras.isNotEmpty()) {
            item {
                CongTableHeader()
            }
        }

        // ── Lista de conglomerados ────────────────────────────────────────────
        if (uiState.isLoadingMuestras) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.filtroCompleto && uiState.muestras.isEmpty()) {
            item {
                EmptyState(mensaje = "No hay conglomerados para este periodo.")
            }
        } else {
            itemsIndexed(
                items = uiState.muestras,
                key = { _, item -> item.id },
            ) { index, muestra ->
                CongRow(muestra = muestra, index = index)
                if (index < uiState.muestras.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }

    // ── Dialog: última fecha de envío ─────────────────────────────────────────
    if (showUltimaFecha) {
        UltimaFechaDialog(
            fecha = uiState.ultimaFechaEnvio,
            onDismiss = { showUltimaFecha = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CARD DE FILTROS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FiltrosCard(
    uiState: ConglomeradoUiState,
    onAnioSelected: (Int) -> Unit,
    onMesSelected: (Int) -> Unit,
    onPeriodoSelected: (Int) -> Unit,
    onProyectoSelected: (Int) -> Unit,
) {
    val mesNombre = { m: Int ->
        listOf("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
            .getOrElse(m) { "$m" }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Filtrar por periodo",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Fila 1: Año y Mes
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FiltroCombo(
                    label = "Año",
                    opciones = uiState.anios,
                    seleccion = uiState.anioSel?.toString(),
                    enabled = uiState.anios.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    display = { it.toString() },
                    onSelect = onAnioSelected,
                )
                FiltroCombo(
                    label = "Mes",
                    opciones = uiState.meses,
                    seleccion = uiState.mesSel?.let { mesNombre(it) },
                    enabled = uiState.meses.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    display = mesNombre,
                    onSelect = onMesSelected,
                )
            }

            // Fila 2: Periodo y Proyecto
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FiltroCombo(
                    label = "Periodo",
                    opciones = uiState.periodos,
                    seleccion = uiState.periodoSel?.let { "P$it" },
                    enabled = uiState.periodos.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    display = { "P$it" },
                    onSelect = onPeriodoSelected,
                )
                FiltroCombo(
                    label = "Proyecto",
                    opciones = uiState.proyectos,
                    seleccion = uiState.proyectoSel?.toString(),
                    enabled = uiState.proyectos.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    display = { it.toString() },
                    onSelect = onProyectoSelected,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  COMBO GENÉRICO
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> FiltroCombo(
    label: String,
    opciones: List<T>,
    seleccion: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    display: (T) -> String,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = seleccion ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label, fontSize = 12.sp) },
            placeholder = { Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(display(opcion), style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        onSelect(opcion)
                        expanded = false
                    },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FILA DE ACCIONES (3 botones)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AccionesRow(
    muestras: List<MuestraConglomeradoEntity>,
    pendientes: Int,
    isSending: Boolean,
    onEnviar: () -> Unit,
    onVerMapa: () -> Unit,
    onVerUltimaFecha: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // Resumen
        if (muestras.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "${muestras.size} conglomerado${if (muestras.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (pendientes > 0) {
                    Text(
                        "$pendientes pendiente${if (pendientes != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Medium,
                    )
                } else {
                    Text(
                        "Todo al día ✓",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Botones
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            // Botón 1: Enviar
            Button(
                onClick = onEnviar,
                enabled = !isSending && pendientes > 0,
                modifier = Modifier.weight(1f).height(42.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                AnimatedContent(targetState = isSending, label = "send_btn") { sending ->
                    if (sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Default.CloudUpload, null, Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Enviar", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Botón 2: Ver en mapa
            OutlinedButton(
                onClick = onVerMapa,
                enabled = muestras.isNotEmpty(),
                modifier = Modifier.weight(1f).height(42.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Icon(Icons.Default.Map, null, Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Ver mapa", fontSize = 12.sp)
            }

            // Botón 3: Última fecha
            FilledTonalButton(
                onClick = onVerUltimaFecha,
                enabled = muestras.isNotEmpty(),
                modifier = Modifier.weight(1f).height(42.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Icon(Icons.Default.History, null, Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("Último env.", fontSize = 11.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TABLA DE CONGLOMERADOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CongTableHeader() {
    val style = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("ID Cong.",     Modifier.width(80.dp),             style = style)
        Text("Cong.",        Modifier.weight(1f),               style = style)
        Text("Ubigeo",       Modifier.width(120.dp),            style = style)
        Text("Estado",       Modifier.width(72.dp),             style = style)
    }
}

@Composable
private fun CongRow(muestra: MuestraConglomeradoEntity, index: Int) {
    val bg = if (index % 2 == 0) Color.Transparent
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ID Conglomerado
        Text(
            muestra.idcong,
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Nombre conglomerado
        Column(Modifier.weight(1f)) {
            Text(
                muestra.conglomerado,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                muestra.odeienapres,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Ubigeo
        Column(Modifier.width(120.dp)) {
            Text(
                muestra.departamento,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "${muestra.provincia} / ${muestra.distrito}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        // Estado pill
        Surface(
            modifier = Modifier.width(72.dp),
            color = if (muestra.sincronizado) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
            shape = RoundedCornerShape(999.dp),
        ) {
            Text(
                text = if (muestra.sincronizado) "Enviado" else "Pendiente",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (muestra.sincronizado) Color(0xFF166534) else Color(0xFF92400E),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  EMPTY STATE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(mensaje: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(40.dp),
        )
        Text(
            mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIALOG: ÚLTIMA FECHA DE ENVÍO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun UltimaFechaDialog(fecha: String?, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    "Último envío",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = fecha ?: "Sin registros de envío aún",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (fecha != null) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (fecha != null) FontWeight.Medium else FontWeight.Normal,
                )
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cerrar")
                }
            }
        }
    }
}