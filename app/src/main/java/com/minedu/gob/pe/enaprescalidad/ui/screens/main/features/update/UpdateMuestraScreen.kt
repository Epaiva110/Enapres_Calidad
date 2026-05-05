
package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update

//
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*

import androidx.compose.runtime.*

import androidx.compose.ui.draw.rotate
//

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginViewModel
import com.minedu.gob.pe.enaprescalidad.viewmodel.MarcoTrabajoViewModel
import kotlin.collections.List
import kotlin.collections.count
import kotlin.collections.plus

@Immutable
data class CargaTrabajo(
    val id: String,
    val fechaProgramacion: String,
    val anio: Int,
    val mes: String,
    val periodo: Int,
    val totalMuestras: Int,
    val actualizado: Boolean, // "Si" o "No" en tu tabla
    val fechaActualizacion: String?,
    val totalActualizado: Int,
    val isSyncing: Boolean = false
) {
    // REGLA: Está realmente al día solo si dice "Si" Y el total coincide
    val estaAlDia: Boolean
        get() = actualizado && totalActualizado >= totalMuestras && totalMuestras > 0
}

//
//@Composable
//fun UpdateScreen(
//    conglomerados: List<CargaTrabajo>,
//    reentrevistas: List<CargaTrabajo>,
//    viviendas: List<CargaTrabajo>,
//    isSyncing: Boolean,
//    onSyncAllData: () -> Unit, // Nueva acción para bajar todo el contenido
//    onSyncGroup: (String) -> Unit,
//    onSyncIndividual: (String) -> Unit,
//    viewModelLogin: LoginViewModel = hiltViewModel(),
//    viewModel: MarcoTrabajoViewModel = hiltViewModel()
//) {
//    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()
//
//    // Lógica global: Solo está verde si hay datos y todos están al día
//    val allLists = listOf(conglomerados, reentrevistas, viviendas)
//    val isEverythingReady = allLists.all { list ->
//        list.isNotEmpty() && list.all { it.estaAlDia }
//    }
//
//    val masterBtnColor = if (isEverythingReady) Color(0xFF2E7D32) else Color(0xFFF9A825)
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.background)
//            .padding(16.dp)
//            .verticalScroll(rememberScrollState())
//    ) {
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
//            elevation = CardDefaults.cardElevation(4.dp)
//        ) {
//            Column(Modifier.padding(16.dp)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            Icons.Default.ListAlt,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                        Spacer(Modifier.width(8.dp))
//                        Text(
//                            "Marco de Trabajo",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.onPrimaryContainer
//                        )
//                    }
//                    IconButton(onClick = { /* acción */ }) {
//                        Icon(
//                            Icons.Default.CloudSync,
//                            contentDescription = "Sincronizar",
//                            tint = MaterialTheme.colorScheme.primary
//
//                        )
//                    }
//                }
//
//                Text(
//                    "Verifica si tienes nuevas cargas asignadas por la oficina central.",
//                    style = MaterialTheme.typography.bodySmall,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//
//                Button(
//                    onClick = { viewModel.getMarcoTrabajo(user?.codsup ?: "", true) },
//                    enabled = !isSyncing,
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary
//                    )
//                ) {
//                    if (isSyncing) {
//                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
//                    } else {
//                        Icon(Icons.Default.CloudDownload, null)
//                        Spacer(Modifier.width(8.dp))
//                        Text("BUSCAR NUEVAS CARGAS")
//                    }
//                }
//            }
//        }
//
//        Spacer(Modifier.height(12.dp))
//
//        // --- TARJETA 2: DESCARGA MASIVA DE DATOS ---
//        val pendientes = (conglomerados + reentrevistas + viviendas).count { !it.estaAlDia }
//        val colorSincro = if (pendientes > 0) Color(0xFFF9A825) else Color(0xFF2E7D32)
//
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = colorSincro),
//            elevation = CardDefaults.cardElevation(4.dp)
//        ) {
//            Column(Modifier.padding(16.dp)) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(Icons.Default.CloudDownload, null, tint = Color.White)
//                        Spacer(Modifier.width(8.dp))
//                        Text(
//                            "Descarga de Información",
//                            color = Color.White,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//
//                    IconButton(onClick = { /* acción */ }) {
//                        Icon(
//                            Icons.Default.CloudSync,
//                            contentDescription = "Sincronizar",
//                            tint = Color.White
//
//                        )
//                    }
//                }
//
//                Text(
//                    if (pendientes > 0) "Tienes $pendientes cargas pendientes de descargar." else "Toda la información está al día.",
//                    color = Color.White.copy(alpha = 0.8f),
//                    style = MaterialTheme.typography.bodySmall,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//                Button(
//                    onClick = onSyncAllData,
//                    enabled = !isSyncing && pendientes > 0,
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = colorSincro),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text("DESCARGAR TODO LO PENDIENTE", fontWeight = FontWeight.Black)
//                }
//            }
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        Spacer(Modifier.height(24.dp))
//
//        // --- SECCIONES DE TABLAS ---
//        CargaTableSection("Cargas de Conglomerado", conglomerados, isSyncing, { onSyncGroup("CONGLOMERADO") }, onSyncIndividual)
//        CargaTableSection("Cargas de Reentrevista", reentrevistas, isSyncing, { onSyncGroup("REENTREVISTA") }, onSyncIndividual)
//        CargaTableSection("Cargas de Vivienda", viviendas, isSyncing, { onSyncGroup("VIVIENDA") }, onSyncIndividual)
//
//        Spacer(Modifier.height(32.dp))
//    }
//}
//
//@Composable
//fun CargaTableSection(
//    titulo: String,
//    cargas: List<CargaTrabajo>,
//    isAnyLoading: Boolean,
//    onUpdateGroup: () -> Unit,
//    onUpdateIndividual: (String) -> Unit
//) {
//    var expanded by rememberSaveable { mutableStateOf(false) }
//    val hasData = cargas.isNotEmpty()
//    val isSectionReady = hasData && cargas.all { it.estaAlDia }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 12.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (hasData) MaterialTheme.colorScheme.surface else Color(0xFFF5F5F5)
//        ),
//        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
//    ) {
//        Column {
//            // Header de la Sección
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable(enabled = hasData) { expanded = !expanded }
//                    .padding(12.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    imageVector = if (!hasData) Icons.Default.Inbox
//                    else if (expanded) Icons.Default.ExpandLess
//                    else Icons.Default.ExpandMore,
//                    contentDescription = null,
//                    tint = if (hasData) MaterialTheme.colorScheme.primary else Color.Gray
//                )
//
//                Column(Modifier
//                    .weight(1f)
//                    .padding(start = 12.dp)) {
//                    Text(
//                        titulo,
//                        style = MaterialTheme.typography.titleSmall,
//                        fontWeight = FontWeight.Bold,
//                        color = if (hasData) Color.Unspecified else Color.Gray
//                    )
//                    if (!hasData) {
//                        Text("No se encontraron registros", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                    }
//                }
//
//                IconButton (
//                    onClick = { onUpdateGroup() },
//                    enabled = !isAnyLoading)
//                {
//                    Icon(
//                        Icons.Default.CloudSync,
//                        contentDescription = "Sincronizar",
//                        tint = MaterialTheme.colorScheme.primary
//
//                    )
//                }
//
//
//
//                if (hasData) {
//                    if (isSectionReady) {
//                        Icon(Icons.Default.CheckCircle, "Listo", tint = Color(0xFF4CAF50))
//                    } else {
//                        Button(
//                            onClick = onUpdateGroup,
//                            enabled = !isAnyLoading,
//                            contentPadding = PaddingValues(horizontal = 12.dp),
//                            modifier = Modifier.height(30.dp),
//                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
//                        ) {
//                            Text("Sincronizar", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
//                        }
//                    }
//                }
//            }
//
//            // Cuerpo de la Tabla (Solo si hay datos)
//            AnimatedVisibility(visible = expanded && hasData) {
//                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
//                    Column(Modifier.widthIn(min = 600.dp)) {
//                        // Header de Columnas
//                        Row(Modifier
//                            .background(Color.LightGray.copy(alpha = 0.1f))
//                            .padding(8.dp)) {
//                            val headStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
//                            Text("ID / PERIODO", Modifier.width(100.dp), style = headStyle)
//                            Text("PROGRESO ACTUAL", Modifier.width(180.dp), style = headStyle)
//                            Text("ESTADO", Modifier.width(80.dp), style = headStyle, textAlign = TextAlign.Center)
//                            Text("ÚLT. ACT.", Modifier.width(100.dp), style = headStyle, textAlign = TextAlign.Center)
//                            Text("ACCIÓN", Modifier.width(100.dp), style = headStyle, textAlign = TextAlign.Center)
//                        }
//
//                        cargas.forEach { carga ->
//                            CargaRow(carga, isAnyLoading, onUpdateIndividual)
//                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CargaRow(
//    carga: CargaTrabajo,
//    isAnyLoading: Boolean,
//    onUpdate: (String) -> Unit
//) {
//    val statusColor = if (carga.estaAlDia) Color(0xFF4CAF50) else Color(0xFFFF9800)
//
//    Row(
//        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Columna 1: ID y Fecha
//        Column(Modifier.width(100.dp)) {
//            Text("#${carga.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
//            Text("${carga.mes} ${carga.anio}", fontSize = 10.sp, color = Color.Gray)
//        }
//
//        // Columna 2: Barra de Progreso
//        Column(Modifier
//            .width(180.dp)
//            .padding(horizontal = 8.dp)) {
//            val progress = if (carga.totalMuestras > 0) carga.totalActualizado.toFloat() / carga.totalMuestras else 0f
//            Text("${carga.totalActualizado} de ${carga.totalMuestras}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
//            Spacer(Modifier.height(4.dp))
//            LinearProgressIndicator(
//                progress = { progress },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(6.dp)
//                    .clip(RoundedCornerShape(3.dp)),
//                color = statusColor,
//                trackColor = statusColor.copy(alpha = 0.2f)
//            )
//        }
//
//        // Columna 3: Chip de Estado
//        Box(Modifier.width(80.dp), contentAlignment = Alignment.Center) {
//            Surface(
//                color = if (carga.actualizado) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Text(
//                    text = if (carga.actualizado) "Al día" else "Pendiente",
//                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
//                    fontSize = 9.sp,
//                    color = if (carga.actualizado) Color(0xFF2E7D32) else Color(0xFFE65100),
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//
//        // Columna 4: Fecha
//        Text(
//            text = carga.fechaActualizacion ?: "--/--/--",
//            modifier = Modifier.width(100.dp),
//            textAlign = TextAlign.Center,
//            style = MaterialTheme.typography.bodySmall,
//            color = Color.DarkGray
//        )
//
//        // Columna 5: Botón
//        Box(Modifier.width(100.dp), contentAlignment = Alignment.Center) {
//            IconButton(
//                onClick = { onUpdate(carga.id) },
//                enabled = !isAnyLoading && !carga.isSyncing,
//                modifier = Modifier.size(32.dp)
//            ) {
//                if (carga.isSyncing) {
//                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
//                } else {
//                    Icon(
//                        Icons.Default.Refresh,
//                        contentDescription = null,
//                        tint = if (carga.estaAlDia) Color.LightGray else Color(0xFF2196F3)
//                    )
//                }
//            }
//        }
//    }
//}
//
//22222
//@Composable
//fun StatusChip(actualizado: Boolean) {
//    val bgColor = if (actualizado) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
//    val textColor = if (actualizado) Color(0xFF2E7D32) else Color(0xFFE65100)
//    val text = if (actualizado) "Al día" else "Pendiente"
//
//    Surface(
//        color = bgColor,
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Text(
//            text = text,
//            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
//            fontSize = 9.sp,
//            color = textColor,
//            fontWeight = FontWeight.Bold
//        )
//    }
//}
//
//@Composable
//fun CargaRow(
//    carga: CargaTrabajo,
//    isAnyLoading: Boolean,
//    onUpdate: (String) -> Unit
//) {
//    val progress = if (carga.totalMuestras > 0) carga.totalActualizado.toFloat() / carga.totalMuestras else 0f
//    val statusColor = if (carga.estaAlDia) Color(0xFF4CAF50) else Color(0xFFFF9800)
//
//    Row(
//        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Column(Modifier.width(100.dp)) {
//            Text("#${carga.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
//            Text("${carga.mes} ${carga.anio}", fontSize = 10.sp, color = Color.Gray)
//        }
//
//        Column(Modifier.width(180.dp).padding(horizontal = 8.dp)) {
//            Text("${carga.totalActualizado} de ${carga.totalMuestras}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
//            Spacer(Modifier.height(4.dp))
//            LinearProgressIndicator(
//                progress = { progress },
//                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
//                color = statusColor,
//                trackColor = statusColor.copy(alpha = 0.2f)
//            )
//        }
//
//        Box(Modifier.width(80.dp), contentAlignment = Alignment.Center) {
//            StatusChip(carga.actualizado)
//        }
//
//        Text(
//            text = carga.fechaActualizacion ?: "--/--/--",
//            modifier = Modifier.width(100.dp),
//            textAlign = TextAlign.Center,
//            style = MaterialTheme.typography.bodySmall,
//            color = Color.DarkGray
//        )
//
//        Box(Modifier.width(100.dp), contentAlignment = Alignment.Center) {
//            if (carga.isSyncing) {
//                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
//            } else {
//                IconButton(
//                    onClick = { onUpdate(carga.id) },
//                    enabled = !isAnyLoading,
//                    modifier = Modifier.size(32.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Refresh,
//                        contentDescription = null,
//                        tint = if (carga.estaAlDia) Color.LightGray else Color(0xFF2196F3)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun CargaTableSection(
//    titulo: String,
//    cargas: List<CargaTrabajo>,
//    isAnyLoading: Boolean,
//    onUpdateGroup: () -> Unit,
//    onUpdateIndividual: (String) -> Unit
//) {
//    var expanded by rememberSaveable { mutableStateOf(false) }
//    val hasData = cargas.isNotEmpty()
//    val isSectionReady = hasData && cargas.all { it.estaAlDia }
//
//    Card(
//        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
//        colors = CardDefaults.cardColors(containerColor = if (hasData) MaterialTheme.colorScheme.surface else Color(0xFFF5F5F5)),
//        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
//    ) {
//        Column {
//            ListItem(
//                modifier = Modifier.clickable(enabled = hasData) { expanded = !expanded },
//                headlineContent = { Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) },
//                supportingContent = { if (!hasData) Text("No se encontraron registros", fontSize = 11.sp) },
//                leadingContent = {
//                    Icon(
//                        imageVector = if (!hasData) Icons.Default.Inbox else if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
//                        contentDescription = null,
//                        tint = if (hasData) MaterialTheme.colorScheme.primary else Color.Gray
//                    )
//                },
//                trailingContent = {
//                    if (hasData) {
//                        if (isSectionReady) {
//                            Icon(Icons.Default.CheckCircle, "Listo", tint = Color(0xFF4CAF50))
//                        } else {
//                            IconButton(onClick = onUpdateGroup, enabled = !isAnyLoading) {
//                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
//                            }
//                        }
//                    }
//                }
//            )
//
//            AnimatedVisibility(visible = expanded && hasData) {
//                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
//                    Column(Modifier.widthIn(min = 600.dp)) {
//                        TableHeader()
//                        cargas.forEach { carga ->
//                            CargaRow(carga, isAnyLoading, onUpdateIndividual)
//                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TableHeader() {
//    Row(Modifier.background(Color.LightGray.copy(alpha = 0.1f)).padding(8.dp)) {
//        val headStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
//        Text("ID / PERIODO", Modifier.width(100.dp), style = headStyle)
//        Text("PROGRESO ACTUAL", Modifier.width(180.dp), style = headStyle)
//        Text("ESTADO", Modifier.width(80.dp), style = headStyle, textAlign = TextAlign.Center)
//        Text("ÚLT. ACT.", Modifier.width(100.dp), style = headStyle, textAlign = TextAlign.Center)
//        Text("ACCIÓN", Modifier.width(100.dp), style = headStyle, textAlign = TextAlign.Center)
//    }
//}
//
@Preview(
    showBackground = false,
    widthDp = 700,
    heightDp = 1200,
    name = "Vista de Tabla Extendida"
)
@Composable
fun UpdateScreenPreview() {
    // Datos de prueba
    val mockData = listOf(
        CargaTrabajo("1", "01/01/2026", 2026, "Enero", 1, 15, true, "20/01/2026", 15),
        CargaTrabajo("2", "02/01/2026", 2026, "Enero", 2, 20, true, "21/01/2026", 10),
        CargaTrabajo("3", "03/01/2026", 2026, "Enero", 3, 35, false, null, 0)
    )

    MaterialTheme {
        // Usamos Surface para que el fondo sea el correcto del tema
        Surface(color = MaterialTheme.colorScheme.background) {
            UpdateScreen(
                conglomerados = mockData,
                reentrevistas = emptyList(), // <--- CORREGIDO: se usa emptyList()
                viviendas = mockData.take(1),
                isSyncing = false,
                onSyncGroup = {},
                onSyncIndividual = {},
                onSyncAllData = {}
            )
        }
    }
}
//
//@Composable
//fun UpdateScreen(
//    conglomerados: List<CargaTrabajo>,
//    reentrevistas: List<CargaTrabajo>,
//    viviendas: List<CargaTrabajo>,
//    isSyncing: Boolean,
//    onSyncAllData: () -> Unit,
//    onSyncGroup: (String) -> Unit,
//    onSyncIndividual: (String) -> Unit,
//    viewModelLogin: LoginViewModel = hiltViewModel(),
//    viewModel: MarcoTrabajoViewModel = hiltViewModel()
//) {
//    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()
//    val pendientes = (conglomerados + reentrevistas + viviendas).count { !it.estaAlDia }
//    val colorSincro = if (pendientes > 0) Color(0xFFF9A825) else Color(0xFF2E7D32)
//
//    Column(
//        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp).verticalScroll(rememberScrollState())
//    ) {
//        // TARJETA 1: MARCO DE TRABAJO
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
//            elevation = CardDefaults.cardElevation(4.dp)
//        ) {
//            Column(Modifier.padding(16.dp)) {
//                Text("Marco de Trabajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//                Text("Verifica si tienes nuevas cargas asignadas.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
//
//                Button(
//                    onClick = { viewModel.getMarcoTrabajo(user?.codsup ?: "", true) },
//                    enabled = !isSyncing,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    if (isSyncing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
//                    else {
//                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
//                        Spacer(Modifier.width(8.dp))
//                        Text("BUSCAR NUEVAS CARGAS")
//                    }
//                }
//            }
//        }
//
//        Spacer(Modifier.height(12.dp))
//
//        // TARJETA 2: DESCARGA MASIVA
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(containerColor = colorSincro),
//            elevation = CardDefaults.cardElevation(4.dp)
//        ) {
//            Column(Modifier.padding(16.dp)) {
//                Text("Descarga de Información", color = Color.White, fontWeight = FontWeight.Bold)
//                Text(
//                    if (pendientes > 0) "Tienes $pendientes cargas pendientes." else "Información al día.",
//                    color = Color.White.copy(alpha = 0.8f),
//                    style = MaterialTheme.typography.bodySmall,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//                Button(
//                    onClick = onSyncAllData,
//                    enabled = !isSyncing && pendientes > 0,
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = colorSincro)
//                ) {
//                    Text("DESCARGAR TODO LO PENDIENTE", fontWeight = FontWeight.Black)
//                }
//            }
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        // SECCIONES DE TABLAS ATOMIZADAS
//        CargaTableSection("Cargas de Conglomerado", conglomerados, isSyncing, { onSyncGroup("CONGLOMERADO") }, onSyncIndividual)
//        CargaTableSection("Cargas de Reentrevista", reentrevistas, isSyncing, { onSyncGroup("REENTREVISTA") }, onSyncIndividual)
//        CargaTableSection("Cargas de Vivienda", viviendas, isSyncing, { onSyncGroup("VIVIENDA") }, onSyncIndividual)
//    }
//}

//@Preview
//@Composable
//fun prueba () {
//
//    val pendientes = 0
//    val colorSincro = if (pendientes > 0) Color(0xFFF9A825) else Color(0xFF2E7D32)
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(containerColor = colorSincro),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(Modifier.padding(16.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(Icons.Default.CloudDownload, null, tint = Color.White)
//                Spacer(Modifier.width(8.dp))
//                Text("Descarga de Información", color = Color.White, fontWeight = FontWeight.Bold)
//                Spacer(Modifier.width(8.dp))
//                Icon(Icons.Default.CloudSync, null, tint = Color.White)
//                IconButton(onClick = { /* acción */ }) {
//                    Icon(
//                        Icons.Default.CloudSync,
//                        contentDescription = "Sincronizar",
//                        tint = Color.White
//
//                    )
//                }
//            }
//
//
////            Box(modifier = Modifier.fillMaxWidth()) {
////
////                Row(
////                    verticalAlignment = Alignment.CenterVertically,
////                    modifier = Modifier.align(Alignment.CenterStart).height(IntrinsicSize.Min)
////                ) {
////                    Icon(Icons.Default.CloudDownload, null, tint = Color.White)
////                    Spacer(Modifier.width(8.dp))
////                    Text(
////                        "Descarga de Información",
////                        color = Color.White,
////                        fontWeight = FontWeight.Bold
////                    )
////                }
////
////                IconButton(
////                    onClick = { /* acción aquí */ },
////                    modifier = Modifier.align(Alignment.TopEnd)
////                ) {
////                    Icon(
////                        Icons.Default.CloudSync,
////                        contentDescription = "Sincronizar",
////                        tint = Color.White
////                    )
////                }
////            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(Icons.Default.CloudDownload, null, tint = Color.White)
//                    Spacer(Modifier.width(8.dp))
//                    Text(
//                        "Descarga de Información",
//                        color = Color.White,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//                IconButton(onClick = { /* acción */ }) {
//                    Icon(
//                        Icons.Default.CloudSync,
//                        contentDescription = "Sincronizar",
//                        tint = Color.White
//
//                    )
//                }
//            }
//
//            Text(
//                if (pendientes > 0) "Tienes $pendientes cargas pendientes de descargar." else "Toda la información está al día.",
//                color = Color.White.copy(alpha = 0.8f),
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
//            Button(
//                onClick = { },
//                enabled = true && pendientes > 0,
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = colorSincro),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text("DESCARGAR TODO LO PENDIENTE", fontWeight = FontWeight.Black)
//            }
//
//        }
//    }
//}

@Composable
fun UpdateScreen(
    conglomerados: List<CargaTrabajo>,
    reentrevistas: List<CargaTrabajo>,
    viviendas: List<CargaTrabajo>,
    isSyncing: Boolean,
    onSyncAllData: () -> Unit,
    onSyncGroup: (String) -> Unit,
    onSyncIndividual: (String) -> Unit,
    viewModelLogin: LoginViewModel = hiltViewModel(),
    viewModel: MarcoTrabajoViewModel = hiltViewModel()
) {
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()

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
            onSearch = { viewModel.getMarcoTrabajo(user?.codsup ?: "", true) }
        )

        // — Card 2: Descarga masiva
        DescargaInfoCard(
            pendingCount = pendingTotal,
            isSyncing = isSyncing,
            onSyncAll = onSyncAllData
        )

        // — Divider con etiqueta
        SectionDivider(label = "Cargas asignadas")

        // — Secciones de tabla
        CargaSection(
            title = "Cargas de conglomerado",
            cargas = conglomerados,
            isAnyLoading = isSyncing,
            onSyncGroup = { onSyncGroup("CONGLOMERADO") },
            onSyncIndividual = onSyncIndividual
        )
        CargaSection(
            title = "Cargas de reentrevista",
            cargas = reentrevistas,
            isAnyLoading = isSyncing,
            onSyncGroup = { onSyncGroup("REENTREVISTA") },
            onSyncIndividual = onSyncIndividual
        )
        CargaSection(
            title = "Cargas de vivienda",
            cargas = viviendas,
            isAnyLoading = isSyncing,
            onSyncGroup = { onSyncGroup("VIVIENDA") },
            onSyncIndividual = onSyncIndividual
        )

        Spacer(Modifier.height(32.dp))
    }
}

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
                icon = Icons.Outlined.CalendarMonth,
                title = "Marco de trabajo",
                onSync = onSearch
            )

            Text(
                text = "Verifica si tienes nuevas cargas asignadas por la oficina central.",
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
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Buscar nuevas cargas",
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

        IconButton(
            onClick = onSync,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = "Sincronizar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
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
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Descarga de información",
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
                    "Tienes cargas sin descargar. Sincroniza todo de una sola vez."
                else
                    "Toda la información está al día.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Button(
                onClick = onSyncAll,
                enabled = !isSyncing && hasPending,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = Color.White.copy(alpha = 0.9f),
                    disabledContainerColor = Color.White.copy(alpha = 0.06f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    "Descargar todo lo pendiente",
                    style = MaterialTheme.typography.labelMedium
                )
            }
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
// — Subcomponentes privados de la sección





@Composable
fun CargaTableRow(
    carga: CargaTrabajo,
    isAnyLoading: Boolean,
    onUpdate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isReady = carga.estaAlDia
    val statusColor = if (isReady) UpdateTokens.ColorSuccess else UpdateTokens.ColorWarning

    val progress by animateFloatAsState(
        targetValue = if (carga.totalMuestras > 0)
            carga.totalActualizado.toFloat() / carga.totalMuestras
        else 0f,
        label = "progress_${carga.id}"
    )

    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Col 1: ID + Periodo
        Column(Modifier.width(COL_ID)) {
            Text(
                "#${carga.id}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${carga.mes} ${carga.anio}",
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
                "${carga.totalActualizado} de ${carga.totalMuestras}",
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
        Box(Modifier.width(COL_STATUS)) {
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
        Box(Modifier.width(COL_ACTION), contentAlignment = Alignment.Center) {
            if (carga.isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = UpdateTokens.ColorInfo
                )
            } else {
                IconButton(
                    onClick = { onUpdate(carga.id) },
                    enabled = !isAnyLoading && !carga.isSyncing,
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
    cargas: List<CargaTrabajo>,
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

                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Contraer" else "Expandir",
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(chevronRotation),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // — Fila 2: acciones del grupo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                // CheckCircle o SyncButton según estado
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

// — Subcomponentes privados

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
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = UpdateTokens.ColorWarningLight,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, UpdateTokens.ColorWarning.copy(alpha = 0.4f))
    ) {
        Text(
            text = "Sincronizar",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = UpdateTokens.ColorWarningText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
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
        Text("Progreso actual",   Modifier.width(COL_PROGRESS).padding(horizontal = 8.dp), style = style)
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