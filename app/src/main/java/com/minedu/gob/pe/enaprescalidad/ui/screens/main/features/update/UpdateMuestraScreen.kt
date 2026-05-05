package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHomeWork
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.enaprescalidad.data.domain.MarcoTrabajo
import com.minedu.gob.pe.enaprescalidad.data.repository.MarcoTrabajoResultLocal
import com.minedu.gob.pe.enaprescalidad.utils.hasInternet
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginViewModel
import com.minedu.gob.pe.enaprescalidad.viewmodel.MarcoTrabajoViewModel
import kotlin.collections.List
import kotlin.collections.count
import kotlin.collections.plus

@Composable
fun UpdateScreen(
    isSyncing: Boolean,
    onSyncAllData: () -> Unit,
    onSyncGroup: (String) -> Unit,
    onSyncIndividual: (String) -> Unit,
    viewModelLogin: LoginViewModel = hiltViewModel(),
    viewModel: MarcoTrabajoViewModel = hiltViewModel()
) {
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val result by viewModel.marcoTrabajo.collectAsStateWithLifecycle()

    LaunchedEffect(user?.codsup) {
        user?.codsup?.let {
            viewModel.loadMarcoTrabajo(it)
        }
    }

    val data = (result as? MarcoTrabajoResultLocal.Success)?.data ?: emptyList()

    val conglomerados = data.filter { it.tipo == "Conglomerado" }
    val reentrevistas = data.filter { it.tipo == "Reentrevista" }
    val viviendas = data.filter { it.tipo == "Vivienda" }

    val pendingTotal = (conglomerados + reentrevistas + viviendas).count { !it.estaAlDia }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // — Card 1: Búsqueda de nuevas cargas
        MarcoTrabajoCard(
            isSyncing = isSyncing,
            onSearch = { viewModel.getMarcoTrabajo(user?.codsup ?: "", hasInternet(context)) }
        )

        // — Card 2: Descarga masiva
        DescargaInfoCard(
            pendingCount = pendingTotal,
            isSyncing = isSyncing,
            onSyncAll = onSyncAllData
        )

        SectionDivider(label = "Cargas asignadas")

        // — Secciones de tabla
        CargaSection(
            title = "Conglomerado",
            cargas = conglomerados,
            isAnyLoading = isSyncing,
            onSyncGroup = { onSyncGroup("CONGLOMERADO") },
            onSyncIndividual = onSyncIndividual
        )
        CargaSection(
            title = "Reentrevista",
            cargas = reentrevistas,
            isAnyLoading = isSyncing,
            onSyncGroup = { onSyncGroup("REENTREVISTA") },
            onSyncIndividual = onSyncIndividual
        )
        CargaSection(
            title = "Vivienda",
            cargas = viviendas,
            isAnyLoading = isSyncing,
            onSyncGroup = { onSyncGroup("VIVIENDA") },
            onSyncIndividual = onSyncIndividual
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun MarcoTrabajoCard(
    isSyncing: Boolean,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            CardHeader(
                icon = Icons.Outlined.Dataset,
                title = "Syncronizar Marco de trabajo",
                onSync = onSearch
            )

            Text(
                text = "Sincroniza los marcos de trabajo asignadas por el equipo de control de calidad de datos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            FilledTonalButton(
                onClick = onSearch,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Buscar Marcos de Trabajo",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun DescargaInfoCard(
    pendingCount: Int,
    isSyncing: Boolean,
    onSyncAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPending = pendingCount > 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        UpdateTokens.ColorSurface,
                        UpdateTokens.ColorSurfaceVariant
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        Icons.Default.AddHomeWork,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Descargar Muestras",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }

                if (hasPending) {
                    PendingBadge(count = pendingCount)
                }
            }

            Text(
                text = if (hasPending)
                    "Tienes muestras sin descargar. Sincroniza todo de una sola vez."
                else
                    "Toda la información está al día.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(vertical = 10.dp)
            )

            FilledTonalButton(
                onClick = onSyncAll,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White.copy(alpha = 0.9f),
                    disabledContainerColor = Color.White.copy(alpha = 0.06f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Descargar todas las muestras pendientes",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
fun CardHeader(
    icon: ImageVector,
    title: String,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PendingBadge(count: Int) {
    Surface(
        color = UpdateTokens.ColorWarning.copy(alpha = 0.25f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = "$count pendientes",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            color = Color(0xFFFCD34D),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

//Esta funcion genera una linea divisora ejemplo : ------Cargas Asignadas------
@Composable
private fun SectionDivider(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(Modifier.weight(1f), thickness = 0.5.dp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(Modifier.weight(1f), thickness = 0.5.dp)
    }
}

@Composable
fun CargaTableRow(
    carga: MarcoTrabajo,
    isAnyLoading: Boolean,
    onUpdate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isReady = carga.estaAlDia
    val statusColor = if (isReady) UpdateTokens.ColorSuccess else UpdateTokens.ColorWarning

    val progress by animateFloatAsState(
        targetValue = if (carga.totalMuestra > 0)
            carga.totalActualizado.toFloat() / carga.totalMuestra
        else 0f,
        label = "progress_${carga.id}"
    )

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Col 1: ID + Periodo
        Column(Modifier.width(COL_ID)){
            Text(
                "#${carga.orden}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${carga.anio} - ${carga.mes} - ${carga.periodo}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Col 2: Barra de progreso
        Column(
            Modifier
                .width(COL_PROGRESS)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                "${carga.totalActualizado} de ${carga.totalMuestra}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.15f)
            )
        }

        // Col 3: Estado pill
        Box(Modifier.width(COL_STATUS)
        ) {
            StatusPill(isReady = isReady)
        }

        // Col 4: Fecha
        Text(
            text = carga.fechaActualizacion ?: "--/--/--",
            modifier = Modifier.width(COL_DATE),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Col 5: Botón acción
        Box(Modifier.width(COL_ACTION)
            , contentAlignment = Alignment.Center) {
            if (carga.actualizado) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = UpdateTokens.ColorInfo
                )
            } else {
                IconButton(
                    onClick = { onUpdate(carga.id.toString()) },
                    enabled = !isAnyLoading && !carga.actualizado,
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
    }
}

@Composable
private fun StatusPill(isReady: Boolean) {
    Surface(
        color = if (isReady) UpdateTokens.ColorSuccessLight else UpdateTokens.ColorWarningLight,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = if (isReady) "Al día" else "Pendiente",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            color = if (isReady) UpdateTokens.ColorSuccessText else UpdateTokens.ColorWarningText,
            fontWeight = FontWeight.Medium
        )
    }
}

// Anchos fijos compartidos entre header y filas para alineación perfecta
private val COL_ID = 90.dp
private val COL_PROGRESS = 160.dp
private val COL_STATUS = 80.dp
private val COL_DATE = 80.dp
private val COL_ACTION = 48.dp

@Composable
fun CargaSection(
    title: String,
    cargas: List<MarcoTrabajo>,
    isAnyLoading: Boolean,
    onSyncGroup: () -> Unit,
    onSyncIndividual: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val hasData = cargas.isNotEmpty()
    val allReady = hasData && cargas.all { it.estaAlDia }
    val pendingCount = cargas.count { !it.estaAlDia }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron_$title"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // — Fila 1: título + chevron (toca para expandir)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hasData) { expanded = !expanded }
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusDot(isReady = allReady, hasData = hasData)

                // IconButton CloudSync — siempre visible
                IconButton(
                    onClick = onSyncGroup,
                    enabled = !isAnyLoading,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = "Sincronizar grupo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (hasData) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when {
                            !hasData -> "Sin registros"
                            allReady -> "Todas al día"
                            else -> "$pendingCount pendiente${if (pendingCount > 1) "s" else ""}"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasData) {
                    if (allReady) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Al día",
                            tint = UpdateTokens.ColorSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        SyncButton(enabled = !isAnyLoading, onClick = onSyncGroup)
                    }
                }

                //

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // — Tabla colapsable
            AnimatedVisibility(
                visible = expanded && hasData,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Box(Modifier.horizontalScroll(rememberScrollState())) {
                        Column {
                            CargaTableHeader()
                            cargas.forEachIndexed { index, carga ->
                                CargaTableRow(
                                    carga = carga,
                                    isAnyLoading = isAnyLoading,
                                    onUpdate = onSyncIndividual
                                )
                                if (index < cargas.lastIndex) {
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp)
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
private fun StatusDot(isReady: Boolean, hasData: Boolean) {
    val color = when {
        !hasData -> Color.Gray.copy(alpha = 0.3f)
        isReady -> UpdateTokens.ColorSuccess
        else -> UpdateTokens.ColorWarning
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(color, RoundedCornerShape(50))
    )
}

@Composable
private fun SyncButton(enabled: Boolean, onClick: () -> Unit) {

    Button(
        onClick = {},
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier.height(30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
    ) {
        Text("Sincronizar", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)

    }
}

@Composable
internal fun CargaTableHeader() {
    val style = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("ID / Periodo",      Modifier.width(COL_ID),       style = style)
        Text("Progreso actual",   Modifier
            .width(COL_PROGRESS)
            .padding(horizontal = 8.dp), style = style)
        Text("Estado",            Modifier.width(COL_STATUS),   style = style)
        Text("Últ. act.",         Modifier.width(COL_DATE),     style = style)
        Spacer(Modifier.width(COL_ACTION))

    }
}

object UpdateTokens {
    val ColorSuccess = Color(0xFF22C55E)
    val ColorSuccessLight = Color(0xFFDCFCE7)
    val ColorSuccessText = Color(0xFF166534)

    val ColorWarning = Color(0xFFF59E0B)
    val ColorWarningLight = Color(0xFFFEF3C7)
    val ColorWarningText = Color(0xFF92400E)

    val ColorInfo = Color(0xFF378ADD)

    val ColorSurface = Color(0xFF1C1C1E)
    val ColorSurfaceVariant = Color(0xFF2D2D30)

    val ColorDivider = Color.Black.copy(alpha = 0.08f)
}