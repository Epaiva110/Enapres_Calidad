package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.verification.conglomerado

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import com.minedu.gob.pe.enaprescalidad.utils.hasInternet
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.minedu.gob.pe.enaprescalidad.ui.components.NavIds
import com.minedu.gob.pe.enaprescalidad.utils.obtenerNombreMes
import com.minedu.gob.pe.enaprescalidad.utils.obtenerNombreProyecto
import com.minedu.gob.pe.enaprescalidad.surveys.SurveyEncuestaProgress
import com.minedu.gob.pe.enaprescalidad.surveys.SurveyEncuestaStatus
import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoActions
import com.minedu.gob.pe.enaprescalidad.viewmodel.ConglomeradoUiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConglomeradoScreen(
    onNavigateCuestionario: (Int) -> Unit,
    onLeerCuestionario: (Int) -> Unit = {},
    viewModel: ConglomeradoViewModel = hiltViewModel(),
    viewModelLogin: LoginViewModel = hiltViewModel()
) {

    //var change by remember { mutableStateOf(change) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()
    val userId = remember(user) { user?.user ?: "" }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.init(userId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshProgresoEncuestas()
    }

    HandleUiEffects(uiState, snackbarHostState, viewModel)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ConglomeradoContent(
            uiState = uiState,
            actions = viewModel,
            onNavigate = onNavigateCuestionario,
            onLeer = onLeerCuestionario,
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
    onLeer: (Int) -> Unit = {},
    userId: String,
    modifier: Modifier = Modifier
) {
    var showHistorial by remember { mutableStateOf(false) }
    var muestraParaAccion by remember { mutableStateOf<MuestraConglomeradoEntity?>(null) }

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
                        encuestaProgreso = uiState.progresoEncuestas[muestra.id],
                        isSeleccionado = muestra.id in uiState.seleccionados,
                        isSending = uiState.isSending,
                        onToggle = { actions.toggleSeleccion(muestra.id) },
                        onEnviarUna = { actions.onEnviarTodas(false) /* Ajustar a onEnviarUna si existe */ },
                        onClickFila = {
                            if (uiState.modoSeleccion) actions.toggleSeleccion(muestra.id)
                            else muestraParaAccion = muestra
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

    muestraParaAccion?.let { muestra ->
        AccionConglomeradoDialog(
            muestra = muestra,
            onIniciarEncuesta = {
                muestraParaAccion = null
                onNavigate(muestra.id)
            },
            onLeerEncuesta = {
                muestraParaAccion = null
                onLeer(muestra.id)
            },
            onDismiss = { muestraParaAccion = null }
        )
    }
}
//
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

        // Se eliminó la Card vieja. Usamos una columna directa con el fondo nativo.
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                    enabled = uiState.anioSel !=null && uiState.meses.isNotEmpty(),
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
                    seleccion = uiState.periodoSel?.toString(),
                    enabled = uiState.mesSel !=null && uiState.periodos.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    display = { it.toString() },
                    onSelect = { actions.onPeriodoSelected(userId, it) }
                )
                FiltroCombo(
                    label = "Proyecto",
                    opciones = uiState.proyectos,
                    seleccion = uiState.proyectoSel?.let{ obtenerNombreProyecto(it) },
                    enabled = uiState.periodoSel !=null && uiState.proyectos.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    display = { obtenerNombreProyecto(it) },
                    onSelect = { actions.onProyectoSelected(userId, it) }
                )
            }
        }
    }
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
        onExpandedChange = { if (enabled) {expanded = !expanded} },
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

@Composable
private fun CongTableHeader(modoSeleccion: Boolean) {
    val style = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            //.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (modoSeleccion) Spacer(Modifier.width(COL_CHECK))
        Text("Id", Modifier.weight(COL_ID), style = style)
        Text("Conglomerado", Modifier.weight(COL_CONG), style = style)
        Text("Encuesta", Modifier.weight(COL_STATUS), style = style)
        if (!modoSeleccion) Spacer(Modifier.width(COL_ACTION))
    }
}

val COL_CHECK = 36.dp
const val COL_ID = 1.0f
const val COL_CONG = 2.5f
const val COL_UBI = 2.0f
const val COL_STATUS = 1.2f
val COL_ACTION = 40.dp

@Composable
fun CongRow(
    muestra: MuestraConglomeradoEntity,
    index: Int,
    modoSeleccion: Boolean,
    encuestaProgreso: SurveyEncuestaProgress? = null,
    isSeleccionado: Boolean,
    isSending: Boolean,
    onToggle: () -> Unit,
    onEnviarUna: () -> Unit,
    onClickFila: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val rowBackground by animateColorAsState(
        targetValue = when {
            isSeleccionado ->
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)

            index % 2 != 0 ->
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.10f)

            else ->
                Color.Transparent
        },
        label = "row_background"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = rowBackground,
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickFila() }
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // =====================================
            // CHECKBOX
            // =====================================

            if (modoSeleccion) {

                Box(
                    modifier = Modifier.width(COL_CHECK),
                    contentAlignment = Alignment.Center,
                ) {

                    Checkbox(
                        checked = isSeleccionado,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // =====================================
            // ID
            // =====================================

            Column(
                modifier = Modifier.weight(COL_ID)
            ) {

                Text(
                    text = muestra.id.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

            }

            // =====================================
            // CONGLOMERADO
            // =====================================

            Column(
                modifier = Modifier
                    .weight(COL_CONG)
                //.padding(horizontal = 6.dp)
            ) {

                Text(
                    text = muestra.conglomerado,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = muestra.odeienapres,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // =====================================
            // STATUS
            // =====================================

            Box(
                modifier = Modifier.weight(COL_STATUS),
                contentAlignment = Alignment.Center,
            ) {

                EncuestaStatusPill(progreso = encuestaProgreso)
            }

            // =====================================
            // ACTION
            // =====================================

            if (!modoSeleccion) {

                Box(
                    modifier = Modifier.width(COL_ACTION),
                    contentAlignment = Alignment.Center,
                ) {

                    when {

                        isSending -> {

                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        !muestra.sincronizado -> {

                            IconButton(
                                onClick = onEnviarUna,
                                modifier = Modifier.size(32.dp),
                            ) {

                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Enviar",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        else -> {

                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EncuestaStatusPill(
    progreso: SurveyEncuestaProgress?,
    modifier: Modifier = Modifier,
) {
    val status = progreso?.status ?: SurveyEncuestaStatus.NOT_STARTED
    val percent = progreso?.percent ?: 0

    val (backgroundColor, contentColor, label) = when (status) {
        SurveyEncuestaStatus.COMPLETED -> Triple(
            Color(0xFFDCFCE7),
            Color(0xFF166534),
            "Completada"
        )
        SurveyEncuestaStatus.IN_PROGRESS -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFF92400E),
            "En curso $percent%"
        )
        SurveyEncuestaStatus.NOT_STARTED -> Triple(
            Color.Transparent,
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            "Sin iniciar"
        )
    }

    if (status == SurveyEncuestaStatus.NOT_STARTED) {
        Text(
            text = label,
            modifier = modifier,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
        return
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = backgroundColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SyncStatusPill(
    sincronizado: Boolean,
    modifier: Modifier = Modifier,
) {

    val backgroundColor =
        if (sincronizado)
            Color(0xFFDCFCE7)
        else
            Color(0xFFFEF3C7)

    val contentColor =
        if (sincronizado)
            Color(0xFF166534)
        else
            Color(0xFF92400E)

    val label =
        if (sincronizado)
            "Enviado"
        else
            "Pendiente"

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = backgroundColor,
    ) {

        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 4.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
        )
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

@Composable
fun AccionConglomeradoDialog(
    muestra: MuestraConglomeradoEntity,
    onIniciarEncuesta: () -> Unit,
    onLeerEncuesta: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Dataset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = muestra.conglomerado,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = muestra.odeienapres,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "¿Qué deseas hacer con este conglomerado?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = onIniciarEncuesta,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Iniciar encuesta", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onLeerEncuesta,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Leer encuesta", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}