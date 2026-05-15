package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.enaprescalidad.data.domain.MarcoTrabajo
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncType
import com.minedu.gob.pe.enaprescalidad.ui.screens.login.sesion.SessionManager
import com.minedu.gob.pe.enaprescalidad.utils.formatDate
import com.minedu.gob.pe.enaprescalidad.utils.hasInternet
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginViewModel
import com.minedu.gob.pe.enaprescalidad.viewmodel.UpdateUiState
import com.minedu.gob.pe.enaprescalidad.viewmodel.UpdateViewModel


// ══════════════════════════════════════════════════════════════════════════════
//  ENTRY POINT — conecta ViewModel con UI
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Punto de entrada de la pantalla.
 *
 * Única responsabilidad: obtener el estado del ViewModel y pasarlo
 * como parámetros simples al composable stateless [UpdateScreenContent].
 *
 * No contiene ninguna lógica de negocio ni composición compleja.
 */

@Composable
fun UpdateScreen(
    viewModel: UpdateViewModel = hiltViewModel(),
    viewModelLogin: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val idC = buildList {
        uiState.conglomerados.forEach {
            if (!it.sincronizado) {
                add(it.id)
            }
        }
    }

    val idR= buildList {
        uiState.reentrevistas.forEach {
            if (!it.sincronizado) {
                add(it.id)
            }
        }
    }

    val idV = buildList {
        uiState.viviendas.forEach {
            if (!it.sincronizado) {
                add(it.id)
            }
        }
    }

    // Inicia la observación reactiva una sola vez cuando el usuario esté disponible
    LaunchedEffect(user?.user) {
        user?.user?.let { viewModel.observeMarcos(it) }
    }

    // Muestra snackbars para éxito/error de sync
    LaunchedEffect(uiState.syncSuccess) {
        if (uiState.syncSuccess) {

            if (uiState.lastSyncType  == null) {
                snackbarHostState.showSnackbar("Sincronización completada")
            } else if (uiState.lastSyncType  == SyncType.CONGLOMERADO) {
                snackbarHostState.showSnackbar("Sincronización de Carga de Trabajo de Conglomerados Completada")
            } else if (uiState.lastSyncType  == SyncType.VIVIENDA) {
                snackbarHostState.showSnackbar("Sincronización de Carga de Trabajo de Viviendas Completada")
            } else if (uiState.lastSyncType  == SyncType.REENTREVISTA) {
                snackbarHostState.showSnackbar("Sincronización de Carga de Trabajo de Reentrevistas Completada")
            }
            viewModel.clearSyncSuccess()
        }
    }
    LaunchedEffect(uiState.syncError) {
        uiState.syncError?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearSyncError()
        }
    }
    LaunchedEffect(uiState.marcoSuccess) {
        if (uiState.marcoSuccess) {
            snackbarHostState.showSnackbar("Marcos actualizados")
            viewModel.clearMarcoSuccess()
        }
    }
    LaunchedEffect(uiState.marcoError) {
        uiState.marcoError?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearMarcoError()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        UpdateScreenContent(
            uiState = uiState,
            onFetchMarcos = {
                viewModel.fetchMarcos(user?.user ?: "", hasInternet(context))
            },
            onSyncAll = {
                viewModel.syncTypeT(idC,idR,idV, hasInternet(context))
            },
            onSyncType = { type ->
                viewModel.syncType(type, user?.user ?: "", hasInternet(context))
            },
            onSyncTypeL = { type ->
                viewModel.syncTypeM(type, idC,idR,idV, hasInternet(context))
            },
            onSyncItem = { marco ->
                viewModel.syncItem(
                    type = marco.tipo,
                    idmt = marco.id,
                    userId = user?.user ?: "",
                    isOnline = hasInternet(context)
                )
            },
            modifier = Modifier.padding(padding)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  PANTALLA STATELESS — solo recibe datos y emite eventos
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Pantalla completamente stateless (sin ViewModel, sin efectos secundarios).
 * Facilita previews y testing unitario.
 */
@Composable
fun UpdateScreenContent(
    uiState: UpdateUiState,
    onFetchMarcos: () -> Unit,
    onSyncAll: () -> Unit,
    onSyncType: (SyncType) -> Unit,
    onSyncTypeL: (SyncType) -> Unit,
    onSyncItem: (MarcoTrabajo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1 — Buscar nuevos marcos de trabajo
        MarcoTrabajoCard(
            isLoading = uiState.isLoadingMarcos,
            isAnyLoading = uiState.isAnyLoading,
            onSearch = onFetchMarcos,
        )

        // Card 2 — Descarga masiva de muestras
        DescargaInfoCard(
            pendingCount = uiState.pendingTotal,
            isSyncing = uiState.syncingType != null,
            isAnyLoading = uiState.isAnyLoading,
            onSyncAll = onSyncAll,
        )

        SectionDivider(label = "Cargas asignadas")

        // Sección Conglomerado
        CargaSection(
            title = "Conglomerado",
            cargas = uiState.conglomerados,
            isSyncingThisGroup = uiState.syncingType == SyncType.CONGLOMERADO,
            isAnyLoading = uiState.isAnyLoading,
            onSyncGroup = { onSyncType(SyncType.CONGLOMERADO) },
            onSyncGroupM = { onSyncTypeL(SyncType.CONGLOMERADO) },
            onSyncItem = onSyncItem,
            isSyncItem = uiState.idItem != 0
        )

        // Sección Reentrevista
        CargaSection(
            title = "Reentrevista",
            cargas = uiState.reentrevistas,
            isSyncingThisGroup = uiState.syncingType == SyncType.REENTREVISTA,
            isAnyLoading = uiState.isAnyLoading,
            onSyncGroup = { onSyncType(SyncType.REENTREVISTA) },
            onSyncGroupM = { onSyncTypeL(SyncType.REENTREVISTA) },
            onSyncItem = onSyncItem,
            isSyncItem = uiState.idItem != 0
        )

        // Sección Vivienda
        CargaSection(
            title = "Vivienda",
            cargas = uiState.viviendas,
            isSyncingThisGroup = uiState.syncingType == SyncType.VIVIENDA,
            isAnyLoading = uiState.isAnyLoading,
            onSyncGroup = { onSyncType(SyncType.VIVIENDA) },
            onSyncGroupM = { onSyncTypeL(SyncType.VIVIENDA) },
            onSyncItem = onSyncItem,
            isSyncItem = uiState.idItem != 0
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  COMPONENTES REUTILIZABLES
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun MarcoTrabajoCard(
    isLoading: Boolean,
    isAnyLoading: Boolean,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            CardHeader(
                icon = Icons.Outlined.Dataset,
                title = "Sincronizar Marco de trabajo",
            )

            Text(
                text = "Sincroniza los marcos de trabajo asignados por el equipo de control de calidad de datos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp),
            )

            FilledTonalButton(
                onClick = onSearch,
                enabled = !isAnyLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.CloudSync, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Buscar Marcos de Trabajo", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun DescargaInfoCard(
    pendingCount: Int,
    isSyncing: Boolean,
    isAnyLoading: Boolean,
    onSyncAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasPending = pendingCount > 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(UpdateTokens.ColorSurface, UpdateTokens.ColorSurfaceVariant)
                )
            )
            .padding(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.AddHomeWork,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Descargar Muestras",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.95f),
                    )
                }
                if (hasPending) PendingBadge(count = pendingCount)
            }

            Text(
                text = if (hasPending)
                    "Tienes $pendingCount muestra${if (pendingCount > 1) "s" else ""} sin descargar."
                else
                    "Toda la información está al día.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(vertical = 10.dp),
            )

            FilledTonalButton(
                onClick = onSyncAll,
                enabled = (!isAnyLoading && hasPending),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White.copy(alpha = 0.9f),
                    disabledContainerColor = Color.White.copy(alpha = 0.06f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f),
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Default.CloudSync, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (hasPending)
                            "Descargar todas las muestras pendientes"
                        else
                            "No hay muestras pendientes por descargar",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun CargaSection(
    title: String,
    cargas: List<MarcoTrabajo>,
    isSyncingThisGroup: Boolean,
    isSyncItem: Boolean,
    isAnyLoading: Boolean,
    onSyncGroup: () -> Unit,
    onSyncGroupM: () -> Unit,
    onSyncItem: (MarcoTrabajo) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val hasData = cargas.isNotEmpty()
    val allReady = hasData && cargas.all { it.estaAlDia }
    val pendingCount = cargas.count { !it.estaAlDia }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron_$title",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column {
            // Cabecera colapsable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hasData) { expanded = !expanded }
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusDot(isReady = allReady, hasData = hasData)

                IconButton(
                    onClick = onSyncGroup,
                    enabled = !isAnyLoading,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = "Sincronizar $title",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (hasData) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = when {
                            !hasData -> "Sin registros"
                            allReady -> "Todas al día"
                            else -> "$pendingCount pendiente${if (pendingCount > 1) "s" else ""}"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Botón de sync del grupo — muestra spinner si está sincronizando este grupo
                if (hasData) {
                    if (isSyncingThisGroup) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else if (allReady) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Al día",
                            tint = UpdateTokens.ColorSuccess,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        SyncButton(enabled = !isAnyLoading, onClick = onSyncGroupM)
                    }
                }

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    modifier = Modifier.size(18.dp).rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Tabla colapsable
            AnimatedVisibility(
                visible = expanded && hasData,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    Box(Modifier.horizontalScroll(rememberScrollState())) {
                        Column {
                            CargaTableHeader()
                            cargas.forEachIndexed { index, carga ->
                                CargaTableRow(
                                    carga = carga,
                                    onSyncItem = onSyncItem,
                                    isSyncItem = isSyncItem)
                                if (index < cargas.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CargaTableRow(
    carga: MarcoTrabajo,
    isSyncItem: Boolean,
    onSyncItem: (MarcoTrabajo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isReady = carga.estaAlDia
    val statusColor = if (isReady) UpdateTokens.ColorSuccess else UpdateTokens.ColorWarning

    val progress by animateFloatAsState(
        targetValue = if (carga.meta > 0)
            carga.descargas.toFloat() / carga.meta
        else 0f,
        label = "progress_${carga.id}",
    )

    Row(
        modifier = modifier.padding(horizontal = 0.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // Col 5: Botón acción
        Box(Modifier.width(COL_ACTION)
            , contentAlignment = Alignment.Center) {
            if (isSyncItem) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = UpdateTokens.ColorInfo
                )
            } else {
                IconButton(
                    onClick = {
                        onSyncItem(carga)
                    },
                    enabled = !carga.sincronizado,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        modifier = Modifier.size(16.dp),
                        tint = if (isReady) MaterialTheme.colorScheme.outlineVariant
                        else UpdateTokens.ColorInfo
                    )
                }
            }
        }

        // Col 1: Orden + Periodo
        Column(Modifier.width(COL_ID)) {
            Text(
                "#${carga.orden} · ${carga.anio}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "${carga.mes} · P${carga.periodo}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Col 2: Barra de progreso
        Column(Modifier.width(COL_PROGRESS).padding(horizontal = 8.dp)) {
            Text(
                "${carga.descargas} de ${carga.meta}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f),
            )
        }

        // Col 3: Estado
        Box(Modifier.width(COL_STATUS)) {
            StatusPill(isReady = isReady)
        }

        // Col 4: Fecha
        Text(
            text = carga.fechasincronizacionAlter?.let { formatDate(it) } ?: "--/--/--",
            //text = carga.fecha_sincronizacion?: 0,
            modifier = Modifier.width(COL_DATE),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  COMPONENTES ATÓMICOS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CardHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SyncButton(enabled: Boolean, onClick: () -> Unit) {

    Button(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier.height(30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
    ) {
        Text("Sincronizar", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)

    }
}

@Composable
private fun PendingBadge(count: Int) {
    Surface(
        color = UpdateTokens.ColorWarning.copy(alpha = 0.25f),
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            "$count pendientes",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color = Color(0xFFFCD34D),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SectionDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HorizontalDivider(Modifier.weight(1f), thickness = 0.5.dp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(Modifier.weight(1f), thickness = 0.5.dp)
    }
}

@Composable
private fun StatusDot(isReady: Boolean, hasData: Boolean) {
    val color = when {
        !hasData -> Color.Gray.copy(alpha = 0.3f)
        isReady  -> UpdateTokens.ColorSuccess
        else     -> UpdateTokens.ColorWarning
    }
    Box(Modifier.size(8.dp).background(color, RoundedCornerShape(50)))
}

@Composable
private fun StatusPill(isReady: Boolean) {
    Surface(
        color = if (isReady) UpdateTokens.ColorSuccessLight else UpdateTokens.ColorWarningLight,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = if (isReady) "Al día" else "Pendiente",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            color = if (isReady) UpdateTokens.ColorSuccessText else UpdateTokens.ColorWarningText,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
internal fun CargaTableHeader() {
    val style = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
    )
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 0.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("",         Modifier.width(COL_ACTION),                      style = style)
        Text("ID / Periodo",   Modifier.width(COL_ID),                          style = style)
        Text("Progreso",       Modifier.width(COL_PROGRESS).padding(horizontal = 8.dp), style = style)
        Text("Estado",         Modifier.width(COL_STATUS),                      style = style)
        Text("Últ. act.",      Modifier.width(COL_DATE),                        style = style)

    }
}

// ── Anchos de columna compartidos ─────────────────────────────────────────────
private val COL_ID       = 90.dp
private val COL_PROGRESS = 160.dp
private val COL_STATUS   = 80.dp
private val COL_DATE     = 80.dp
private val COL_ACTION     = 35.dp

// ── Design tokens ─────────────────────────────────────────────────────────────
object UpdateTokens {
    val ColorSuccess      = Color(0xFF22C55E)
    val ColorSuccessLight = Color(0xFFDCFCE7)
    val ColorSuccessText  = Color(0xFF166534)

    val ColorWarning      = Color(0xFFF59E0B)
    val ColorWarningLight = Color(0xFFFEF3C7)
    val ColorWarningText  = Color(0xFF92400E)

    val ColorInfo         = Color(0xFF378ADD)

    val ColorSurface        = Color(0xFF1C1C1E)
    val ColorSurfaceVariant = Color(0xFF2D2D30)
}