
package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.update


import android.util.Log
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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginViewModel
import com.minedu.gob.pe.enaprescalidad.viewmodel.MarcoTrabajoViewModel
import kotlinx.coroutines.flow.Flow
import kotlin.collections.List
import kotlin.collections.count
import kotlin.collections.plus

//@Preview(
//    showBackground = false,
//    widthDp = 700,
//    heightDp = 1200,
//    name = "Vista de Tabla Extendida"
//)
@Composable
fun UpdateScreenPreview22(
    viewModelLogin : LoginViewModel = hiltViewModel(),
    viewModel: MarcoTrabajoViewModel = hiltViewModel()
) {
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()

    // Datos de prueba
    val mockData_01 = viewModel.getMarcoTrabajoTipo(user?.codsup ?: "", "Conglomerado")
    val mockData_02 = viewModel.getMarcoTrabajoTipo(user?.codsup ?: "", "Reentrevista")
    val mockData_03 = viewModel.getMarcoTrabajoTipo(user?.codsup ?: "", "Vivienda")

    Log.i("Error000001", mockData_01.toString())
    Log.i("Error000002", mockData_01.toString())
    Log.i("Error000003", mockData_01.toString())

    MaterialTheme {
        // Usamos Surface para que el fondo sea el correcto del tema
        Surface(color = MaterialTheme.colorScheme.background) {
            UpdateScreen22(
                conglomerados = emptyList(),
                reentrevistas = emptyList(), // <--- CORREGIDO: se usa emptyList()
                viviendas = emptyList(),
                isSyncing = false,
                onSyncGroup = {},
                onSyncIndividual = {},
                onSyncAllData = {}
            )
        }
    }
}

//
@Composable
fun UpdateScreen22(
    conglomerados: List<CargaTrabajo>,
    reentrevistas: List<CargaTrabajo>,
    viviendas: List<CargaTrabajo>,
    isSyncing: Boolean,
    onSyncAllData: () -> Unit, // Nueva acción para bajar todo el contenido
    onSyncGroup: (String) -> Unit,
    onSyncIndividual: (String) -> Unit,
    viewModelLogin: LoginViewModel = hiltViewModel(),
    viewModel: MarcoTrabajoViewModel = hiltViewModel()
) {
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()

    // Lógica global: Solo está verde si hay datos y todos están al día
    val allLists = listOf(conglomerados, reentrevistas, viviendas)
    val isEverythingReady = allLists.all { list ->
        list.isNotEmpty() && list.all { it.estaAlDia }
    }

    val masterBtnColor = if (isEverythingReady) Color(0xFF2E7D32) else Color(0xFFF9A825)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Marco de Trabajo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = { /* acción */ }) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Sincronizar",
                            tint = MaterialTheme.colorScheme.primary

                        )
                    }
                }

                Text(
                    "Verifica si tienes nuevas cargas asignadas por la oficina central.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Button(
                    onClick = { viewModel.getMarcoTrabajo(user?.codsup ?: "", true) },
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.CloudDownload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("BUSCAR NUEVAS CARGAS")
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- TARJETA 2: DESCARGA MASIVA DE DATOS ---
        val pendientes = (conglomerados + reentrevistas + viviendas).count { !it.estaAlDia }
        val colorSincro = if (pendientes > 0) Color(0xFFF9A825) else Color(0xFF2E7D32)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorSincro),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Descarga de Información",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { /* acción */ }) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Sincronizar",
                            tint = Color.White

                        )
                    }
                }

                Text(
                    if (pendientes > 0) "Tienes $pendientes cargas pendientes de descargar." else "Toda la información está al día.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = onSyncAllData,
                    enabled = !isSyncing && pendientes > 0,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = colorSincro),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DESCARGAR TODO LO PENDIENTE", fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Spacer(Modifier.height(24.dp))

        // --- SECCIONES DE TABLAS ---
        CargaTableSection("Cargas de Conglomerado", conglomerados, isSyncing, { onSyncGroup("CONGLOMERADO") }, onSyncIndividual)
        CargaTableSection("Cargas de Reentrevista", reentrevistas, isSyncing, { onSyncGroup("REENTREVISTA") }, onSyncIndividual)
        CargaTableSection("Cargas de Vivienda", viviendas, isSyncing, { onSyncGroup("VIVIENDA") }, onSyncIndividual)

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun CargaTableSection(
    titulo: String,
    cargas: List<CargaTrabajo>,
    isAnyLoading: Boolean,
    onUpdateGroup: () -> Unit,
    onUpdateIndividual: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val hasData = cargas.isNotEmpty()
    val isSectionReady = hasData && cargas.all { it.estaAlDia }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasData) MaterialTheme.colorScheme.surface else Color(0xFFF5F5F5)
        ),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column {
            // Header de la Sección
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = hasData) { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (!hasData) Icons.Default.Inbox
                    else if (expanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (hasData) MaterialTheme.colorScheme.primary else Color.Gray
                )

                Column(Modifier
                    .weight(1f)
                    .padding(start = 12.dp)) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hasData) Color.Unspecified else Color.Gray
                    )
                    if (!hasData) {
                        Text("No se encontraron registros", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                IconButton (
                    onClick = { onUpdateGroup() },
                    enabled = !isAnyLoading)
                {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = "Sincronizar",
                        tint = MaterialTheme.colorScheme.primary

                    )
                }



                if (hasData) {
                    if (isSectionReady) {
                        Icon(Icons.Default.CheckCircle, "Listo", tint = Color(0xFF4CAF50))
                    } else {
                        Button(
                            onClick = onUpdateGroup,
                            enabled = !isAnyLoading,
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Text("Sincronizar", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Cuerpo de la Tabla (Solo si hay datos)
            AnimatedVisibility(visible = expanded && hasData) {
                Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Column(Modifier.widthIn(min = 600.dp)) {
                        // Header de Columnas
                        Row(Modifier
                            .background(Color.LightGray.copy(alpha = 0.1f))
                            .padding(8.dp)) {
                            val headStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("ID / PERIODO", Modifier.width(100.dp), style = headStyle)
                            Text("PROGRESO ACTUAL", Modifier.width(180.dp), style = headStyle)
                            Text("ESTADO", Modifier.width(80.dp), style = headStyle, textAlign = TextAlign.Center)
                            Text("ÚLT. ACT.", Modifier.width(100.dp), style = headStyle, textAlign = TextAlign.Center)
                            Text("ACCIÓN", Modifier.width(100.dp), style = headStyle, textAlign = TextAlign.Center)
                        }

                        cargas.forEach { carga ->
                            CargaRow(carga, isAnyLoading, onUpdateIndividual)
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CargaRow(
    carga: CargaTrabajo,
    isAnyLoading: Boolean,
    onUpdate: (String) -> Unit
) {
    val statusColor = if (carga.estaAlDia) Color(0xFF4CAF50) else Color(0xFFFF9800)

    Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna 1: ID y Fecha
        Column(Modifier.width(100.dp)) {
            Text("#${carga.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            Text("${carga.mes} ${carga.anio}", fontSize = 10.sp, color = Color.Gray)
        }

        // Columna 2: Barra de Progreso
        Column(Modifier
            .width(180.dp)
            .padding(horizontal = 8.dp)) {
            val progress = if (carga.totalMuestras > 0) carga.totalActualizado.toFloat() / carga.totalMuestras else 0f
            Text("${carga.totalActualizado} de ${carga.totalMuestras}", fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.2f)
            )
        }

        // Columna 3: Chip de Estado
        Box(Modifier.width(80.dp), contentAlignment = Alignment.Center) {
            Surface(
                color = if (carga.actualizado) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (carga.actualizado) "Al día" else "Pendiente",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    color = if (carga.actualizado) Color(0xFF2E7D32) else Color(0xFFE65100),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Columna 4: Fecha
        Text(
            text = carga.fechaActualizacion ?: "--/--/--",
            modifier = Modifier.width(100.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )

        // Columna 5: Botón
        Box(Modifier.width(100.dp), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = { onUpdate(carga.id) },
                enabled = !isAnyLoading && !carga.isSyncing,
                modifier = Modifier.size(32.dp)
            ) {
                if (carga.isSyncing) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (carga.estaAlDia) Color.LightGray else Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

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
//
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

//            Box(modifier = Modifier.fillMaxWidth()) {
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.align(Alignment.CenterStart).height(IntrinsicSize.Min)
//                ) {
//                    Icon(Icons.Default.CloudDownload, null, tint = Color.White)
//                    Spacer(Modifier.width(8.dp))
//                    Text(
//                        "Descarga de Información",
//                        color = Color.White,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//                IconButton(
//                    onClick = { /* acción aquí */ },
//                    modifier = Modifier.align(Alignment.TopEnd)
//                ) {
//                    Icon(
//                        Icons.Default.CloudSync,
//                        contentDescription = "Sincronizar",
//                        tint = Color.White
//                    )
//                }
//            }

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

