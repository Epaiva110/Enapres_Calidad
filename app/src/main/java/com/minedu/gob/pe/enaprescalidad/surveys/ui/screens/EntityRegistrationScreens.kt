package com.minedu.gob.pe.enaprescalidad.surveys.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.data.local.entity.*
import com.minedu.gob.pe.enaprescalidad.surveys.models.EstadoEncuesta
import com.minedu.gob.pe.enaprescalidad.surveys.models.ResultadoVisita
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyContext
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity.*

// ═════════════════════════════════════════════════════════════════════════════
//  PANTALLA DE HOGARES
//
//  Lista los hogares de una vivienda.
//  Permite crear, editar y abrir la encuesta de cada hogar.
//  Desde aquí se navega a PersonasScreen y VisitasHogarScreen.
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HogaresScreen(
    viviendaId: Int,
    viviendaOrden: Int,
    hogares: List<HogarEntity>,
    progresos: Map<String, ContextProgressRow>,   // contextKey → progreso
    onCrearHogar: () -> Unit,
    onAbrirEncuestaHogar: (HogarEntity) -> Unit,
    onAbrirPersonas: (HogarEntity) -> Unit,
    onAbrirVisitas: (HogarEntity) -> Unit,
    onNavigateBack: () -> Unit,
    showFormulario: Boolean = false,
    hogarEditando: HogarEntity? = null,
    onGuardarHogar: (HogarEntity) -> Unit,
    onDismissFormulario: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hogares", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Vivienda #$viviendaOrden", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearHogar,
                icon    = { Icon(Icons.Default.AddHome, null) },
                text    = { Text("Nuevo hogar") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.padding(padding).fillMaxSize(),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (hogares.isEmpty()) {
                item {
                    EntidadEmptyState(
                        icono   = Icons.Default.Home,
                        mensaje = "No hay hogares registrados en esta vivienda.\nAgrega el primero con el botón inferior.",
                    )
                }
            } else {
                items(hogares, key = { it.id }) { hogar ->
                    val contextKey = SurveyContext.Hogar(hogar.id, viviendaId, hogar.numeroOrden).contextKey
                    val progreso   = progresos[contextKey]
                    HogarCard(
                        hogar              = hogar,
                        completada         = progreso?.estaCompletada ?: false,
                        totalRespuestas    = progreso?.totalVariables ?: 0,
                        onAbrirEncuesta    = { onAbrirEncuestaHogar(hogar) },
                        onAbrirPersonas    = { onAbrirPersonas(hogar) },
                        onAbrirVisitas     = { onAbrirVisitas(hogar) },
                        onEditar           = { /* Editar datos básicos del hogar */ },
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showFormulario) {
        HogarFormBottomSheet(
            hogarExistente       = hogarEditando,
            viviendaId           = viviendaId,
            numeroOrdenSiguiente = hogares.size + 1,
            onGuardar            = onGuardarHogar,
            onDismiss            = onDismissFormulario,
        )
    }
}

@Composable
private fun HogarCard(
    hogar: HogarEntity,
    completada: Boolean,
    totalRespuestas: Int,
    onAbrirEncuesta: () -> Unit,
    onAbrirPersonas: () -> Unit,
    onAbrirVisitas: () -> Unit,
    onEditar: () -> Unit,
) {
    val estado = EstadoEncuesta.valueOf(hogar.estadoEncuesta)
    val bgColor by animateColorAsState(estadoColor(estado), tween(300), label = "hogar_bg")

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Encabezado
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NumeroBadge(hogar.numeroOrden)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        hogar.nombreJefeHogar.ifBlank { "Sin nombre registrado" },
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Hogar #${hogar.numeroOrden} · $totalRespuestas respuestas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                EstadoBadge(estado)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.3f))

            // Acciones
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Encuesta del hogar
                AccionButton(
                    modifier = Modifier.weight(2f),
                    icono    = if (completada) Icons.Default.CheckCircle else Icons.Default.Assignment,
                    label    = if (completada) "Ver encuesta" else "Llenar encuesta",
                    color    = if (completada) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error,
                    onClick  = onAbrirEncuesta,
                )
                // Personas
                AccionButtonCompacto(Icons.Default.People, "Personas", onAbrirPersonas)
                // Visitas
                AccionButtonCompacto(Icons.Default.CalendarToday, "Visitas", onAbrirVisitas)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  PANTALLA DE PERSONAS
//
//  Lista las personas de un hogar.
//  Permite crear, editar y abrir la encuesta individual de cada persona.
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonasScreen(
    hogarId: Int,
    hogarOrden: Int,
    nombreJefe: String,
    personas: List<PersonaEntity>,
    progresos: Map<String, ContextProgressRow>,
    onCrearPersona: () -> Unit,
    onAbrirEncuestaPersona: (PersonaEntity) -> Unit,
    onNavigateBack: () -> Unit,
    showFormulario: Boolean = false,
    personaEditando: PersonaEntity? = null,
    onGuardarPersona: (PersonaEntity) -> Unit,
    onDismissFormulario: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Miembros del hogar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (nombreJefe.isNotBlank()) "Hogar #$hogarOrden — $nombreJefe"
                            else "Hogar #$hogarOrden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Volver") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCrearPersona,
                icon    = { Icon(Icons.Default.PersonAdd, null) },
                text    = { Text("Agregar persona") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.padding(padding).fillMaxSize(),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (personas.isEmpty()) {
                item {
                    EntidadEmptyState(
                        icono   = Icons.Default.People,
                        mensaje = "No hay miembros registrados.\nAgrega al jefe del hogar primero.",
                    )
                }
            } else {
                items(personas, key = { it.id }) { persona ->
                    val contextKey = SurveyContext.Persona(persona.id, hogarId, persona.numeroOrden).contextKey
                    val progreso   = progresos[contextKey]
                    PersonaCard(
                        persona          = persona,
                        completada       = progreso?.estaCompletada ?: false,
                        totalRespuestas  = progreso?.totalVariables ?: 0,
                        onAbrirEncuesta  = { onAbrirEncuestaPersona(persona) },
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showFormulario) {
        PersonaFormBottomSheet(
            personaExistente     = personaEditando,
            hogarId              = hogarId,
            numeroOrdenSiguiente = personas.size + 1,
            onGuardar            = onGuardarPersona,
            onDismiss            = onDismissFormulario,
        )
    }
}

@Composable
private fun PersonaCard(
    persona: PersonaEntity,
    completada: Boolean,
    totalRespuestas: Int,
    onAbrirEncuesta: () -> Unit,
) {
    val estado = EstadoEncuesta.valueOf(persona.estadoEncuesta)
    val bgColor by animateColorAsState(estadoColor(estado), tween(300), label = "persona_bg")

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumeroBadge(persona.numeroOrden)

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        persona.nombreCompleto.ifBlank { "Sin nombre" },
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (persona.esJefeHogar) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                "JEFE",
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier   = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
                Text(
                    buildString {
                        if (persona.edad.isNotBlank()) append("${persona.edad} años")
                        if (persona.edad.isNotBlank() && persona.sexo.isNotBlank()) append(" · ")
                        if (persona.sexo.isNotBlank()) append(if (persona.sexo == "M") "Masculino" else "Femenino")
                        if (isNotEmpty()) append(" · ")
                        append("$totalRespuestas respuestas")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            EstadoBadge(estado)

            FilledTonalIconButton(
                onClick = onAbrirEncuesta,
                colors  = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (completada) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    if (completada) Icons.Default.CheckCircle else Icons.Default.Assignment,
                    null,
                    Modifier.size(20.dp),
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  PANTALLA DE VISITAS
//
//  Lista las visitas de un hogar o conglomerado.
//  Permite registrar y editar visitas.
// ═════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitasHogarScreen(
    hogarId: Int,
    hogarOrden: Int,
    nombreJefe: String,
    visitas: List<VisitaHogarEntity>,
    onRegistrarVisita: () -> Unit,
    onEditarVisita: (VisitaHogarEntity) -> Unit,
    onNavigateBack: () -> Unit,
    showFormulario: Boolean = false,
    visitaEditando: VisitaHogarEntity? = null,
    encuestadorActual: String = "",
    onGuardarVisita: (VisitaHogarEntity) -> Unit,
    onDismissFormulario: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Visitas al hogar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            if (nombreJefe.isNotBlank()) "Hogar #$hogarOrden — $nombreJefe"
                            else "Hogar #$hogarOrden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Volver") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRegistrarVisita,
                icon    = { Icon(Icons.Default.AddTask, null) },
                text    = { Text("Registrar visita") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.padding(padding).fillMaxSize(),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Resumen de visitas
            item {
                VisitasResumen(visitas)
            }

            if (visitas.isEmpty()) {
                item {
                    EntidadEmptyState(
                        icono   = Icons.Default.CalendarToday,
                        mensaje = "No hay visitas registradas.\nRegistra la primera visita con el botón inferior.",
                    )
                }
            } else {
                items(visitas, key = { it.id }) { visita ->
                    VisitaCard(
                        visita   = visita,
                        onEditar = { onEditarVisita(visita) },
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showFormulario) {
        VisitaFormBottomSheet(
            visitaExistente       = visitaEditando,
            hogarId               = hogarId,
            numeroVisitaSiguiente = visitas.size + 1,
            encuestadorActual     = encuestadorActual,
            onGuardar             = onGuardarVisita,
            onDismiss             = onDismissFormulario,
        )
    }
}

@Composable
private fun VisitasResumen(visitas: List<VisitaHogarEntity>) {
    val completas  = visitas.count { it.resultado == ResultadoVisita.COMPLETO.name }
    val pendientes = visitas.count { it.resultado == ResultadoVisita.PENDIENTE.name }
    val rechazos   = visitas.count { it.resultado == ResultadoVisita.RECHAZO.name }

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ResumenChip("Completas",  completas,  Color(0xFF166534), Modifier.weight(1f))
        ResumenChip("Pendientes", pendientes, Color(0xFF854D0E), Modifier.weight(1f))
        ResumenChip("Rechazos",   rechazos,   Color(0xFF9F1239), Modifier.weight(1f))
    }
}

@Composable
private fun ResumenChip(label: String, count: Int, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(8.dp),
        color    = color.copy(alpha = 0.10f),
        border   = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    ) {
        Column(
            modifier          = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = color.copy(0.8f))
        }
    }
}

@Composable
private fun VisitaCard(
    visita: VisitaHogarEntity,
    onEditar: () -> Unit,
) {
    val resultado  = runCatching { ResultadoVisita.valueOf(visita.resultado) }.getOrElse { ResultadoVisita.PENDIENTE }
    val bgColor    = resultadoColor(resultado)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
    ) {
        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumeroBadge(visita.numeroVisita)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Visita #${visita.numeroVisita}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    ResultadoBadge(resultado)
                }
                if (visita.fechaHoraVisita.isNotBlank())
                    InfoRow(Icons.Default.Schedule, visita.fechaHoraVisita)
                if (visita.encuestador.isNotBlank())
                    InfoRow(Icons.Default.Person, visita.encuestador)
                if (visita.ubicacion.isNotBlank())
                    InfoRow(Icons.Default.LocationOn, visita.ubicacion)
                if (visita.proximaVisita.isNotBlank())
                    InfoRow(Icons.Default.Event, "Próxima: ${visita.proximaVisita}")
                if (visita.observaciones.isNotBlank())
                    InfoRow(Icons.Default.Notes, visita.observaciones)
            }

            FilledTonalIconButton(onClick = onEditar, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  COMPONENTES COMPARTIDOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NumeroBadge(numero: Int) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(0.12f), modifier = Modifier.size(36.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Text("$numero", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun EstadoBadge(estado: EstadoEncuesta) {
    val (bg, fg) = when (estado) {
        EstadoEncuesta.COMPLETO   -> Color(0xFFDCFCE7) to Color(0xFF166534)
        EstadoEncuesta.INCOMPLETO -> Color(0xFFFEF9C3) to Color(0xFF854D0E)
        EstadoEncuesta.RECHAZO    -> Color(0xFFFFE4E6) to Color(0xFF9F1239)
        EstadoEncuesta.AUSENTE    -> Color(0xFFF3F4F6) to Color(0xFF374151)
        EstadoEncuesta.NUEVO      -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(estado.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

@Composable
private fun ResultadoBadge(resultado: ResultadoVisita) {
    val (bg, fg) = when (resultado) {
        ResultadoVisita.COMPLETO  -> Color(0xFFDCFCE7) to Color(0xFF166534)
        ResultadoVisita.AUSENTE   -> Color(0xFFF3F4F6) to Color(0xFF374151)
        ResultadoVisita.RECHAZO   -> Color(0xFFFFE4E6) to Color(0xFF9F1239)
        ResultadoVisita.PENDIENTE -> Color(0xFFFEF9C3) to Color(0xFF854D0E)
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(resultado.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

@Composable
private fun InfoRow(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icono, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(texto, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AccionButton(modifier: Modifier, icono: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = color),
        shape    = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
    ) {
        Icon(icono, null, Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AccionButtonCompacto(icono: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape    = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        Icon(icono, null, Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun EntidadEmptyState(icono: androidx.compose.ui.graphics.vector.ImageVector, mensaje: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icono, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outlineVariant)
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPERS DE COLOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun estadoColor(estado: EstadoEncuesta): Color = when (estado) {
    EstadoEncuesta.COMPLETO   -> Color(0xFFE8F5E9)
    EstadoEncuesta.INCOMPLETO -> Color(0xFFFFF8E1)
    EstadoEncuesta.RECHAZO    -> Color(0xFFFFEBEE)
    EstadoEncuesta.AUSENTE    -> Color(0xFFF5F5F5)
    EstadoEncuesta.NUEVO      -> MaterialTheme.colorScheme.surface
}

private fun resultadoColor(resultado: ResultadoVisita): Color = when (resultado) {
    ResultadoVisita.COMPLETO  -> Color(0xFFE8F5E9)
    ResultadoVisita.AUSENTE   -> Color(0xFFF5F5F5)
    ResultadoVisita.RECHAZO   -> Color(0xFFFFEBEE)
    ResultadoVisita.PENDIENTE -> Color(0xFFFFF8E1)
}
