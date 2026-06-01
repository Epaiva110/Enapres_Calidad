package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.EstadoEncuesta
import com.minedu.gob.pe.enaprescalidad.surveys.models.ResultadoVisita

// ─────────────────────────────────────────────────────────────────────────────
//  MODELOS DE UI  (independientes de las entidades de Room)
// ─────────────────────────────────────────────────────────────────────────────

data class HogarRowUi(
    val id: Int,
    val numeroOrden: Int,
    val nombreJefeHogar: String,
    val totalPersonas: Int,
    val totalVisitas: Int,
    val estadoEncuesta: EstadoEncuesta,
)

data class PersonaRowUi(
    val id: Int,
    val numeroOrden: Int,
    val nombreCompleto: String,
    val edad: String,
    val sexo: String,
    val esJefeHogar: Boolean,
    val estadoEncuesta: EstadoEncuesta,
)

data class VisitaRowUi(
    val id: Int,
    val numeroVisita: Int,
    val fechaHora: String,
    val resultado: ResultadoVisita,
    val encuestador: String,
    val proximaVisita: String,
)

// ─────────────────────────────────────────────────────────────────────────────
//  MÓDULO DE HOGARES  (entity_hogar)
//
//  Se embebe dentro del SurveyPage cuando type == "entity_hogar".
//  Muestra la lista de hogares registrados y permite agregar/editar.
//  onConteoChange notifica al ViewModel cuántos registros hay para que
//  pueda evaluar el required y los constraints de esta "pregunta".
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HogaresModule(
    hogares: List<HogarRowUi>,
    minRegistros: Int = 1,
    maxRegistros: Int? = null,
    soloLectura: Boolean = false,
    hasError: Boolean = false,
    onAgregarHogar: () -> Unit,
    onEditarHogar: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val puedeAgregar = !soloLectura && (maxRegistros == null || hogares.size < maxRegistros)

    EntityModuleContainer(
        titulo       = "Hogares registrados",
        conteo       = hogares.size,
        minRegistros = minRegistros,
        hasError     = hasError,
        iconoTitulo  = Icons.Default.Home,
        modifier     = modifier,
    ) {
        if (hogares.isEmpty()) {
            EntityEmptyState("No hay hogares registrados en esta vivienda.")
        } else {
            LazyColumn(
                modifier        = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding  = PaddingValues(vertical = 4.dp),
            ) {
                items(hogares, key = { it.id }) { hogar ->
                    HogarRow(
                        hogar        = hogar,
                        soloLectura  = soloLectura,
                        onEditar     = { onEditarHogar(hogar.id) },
                    )
                }
            }
        }

        if (puedeAgregar) {
            Spacer(Modifier.height(8.dp))
            EntityAddButton(
                label   = "Agregar Hogar",
                icono   = Icons.Default.AddHome,
                onClick = onAgregarHogar,
            )
        }
    }
}

@Composable
private fun HogarRow(
    hogar: HogarRowUi,
    soloLectura: Boolean,
    onEditar: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = estadoEncuestaColor(hogar.estadoEncuesta),
        animationSpec = tween(300),
        label = "hogar_bg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Número de orden
        EntityBadge(numero = hogar.numeroOrden)

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = hogar.nombreJefeHogar.ifBlank { "Jefe no registrado" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EntityStat(Icons.Default.People, "${hogar.totalPersonas} personas")
                EntityStat(Icons.Default.CalendarToday, "${hogar.totalVisitas} visitas")
            }
        }

        // Estado chip
        EstadoChip(hogar.estadoEncuesta)

        // Botón editar
        if (!soloLectura) {
            FilledTonalIconButton(
                onClick  = onEditar,
                modifier = Modifier.size(36.dp),
                colors   = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (hogar.estadoEncuesta == EstadoEncuesta.INCOMPLETO)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MÓDULO DE PERSONAS  (entity_persona)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PersonasModule(
    personas: List<PersonaRowUi>,
    minRegistros: Int = 1,
    maxRegistros: Int? = null,
    soloLectura: Boolean = false,
    hasError: Boolean = false,
    onAgregarPersona: () -> Unit,
    onEditarPersona: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val puedeAgregar = !soloLectura && (maxRegistros == null || personas.size < maxRegistros)

    EntityModuleContainer(
        titulo       = "Miembros del hogar",
        conteo       = personas.size,
        minRegistros = minRegistros,
        hasError     = hasError,
        iconoTitulo  = Icons.Default.People,
        modifier     = modifier,
    ) {
        if (personas.isEmpty()) {
            EntityEmptyState("No se han registrado miembros en este hogar.")
        } else {
            LazyColumn(
                modifier        = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding  = PaddingValues(vertical = 4.dp),
            ) {
                items(personas, key = { it.id }) { persona ->
                    PersonaRow(
                        persona     = persona,
                        soloLectura = soloLectura,
                        onEditar    = { onEditarPersona(persona.id) },
                    )
                }
            }
        }

        if (puedeAgregar) {
            Spacer(Modifier.height(8.dp))
            EntityAddButton(
                label   = "Agregar Miembro",
                icono   = Icons.Default.PersonAdd,
                onClick = onAgregarPersona,
            )
        }
    }
}

@Composable
private fun PersonaRow(
    persona: PersonaRowUi,
    soloLectura: Boolean,
    onEditar: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = estadoEncuestaColor(persona.estadoEncuesta),
        animationSpec = tween(300),
        label = "persona_bg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EntityBadge(numero = persona.numeroOrden)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = persona.nombreCompleto.ifBlank { "Sin registrar" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (persona.esJefeHogar) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            "JEFE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (persona.edad.isNotBlank()) EntityStat(Icons.Default.Cake, "${persona.edad} años")
                if (persona.sexo.isNotBlank()) EntityStat(Icons.Default.Person, persona.sexo)
            }
        }

        EstadoChip(persona.estadoEncuesta)

        if (!soloLectura) {
            FilledTonalIconButton(
                onClick  = onEditar,
                modifier = Modifier.size(36.dp),
                colors   = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (persona.estadoEncuesta == EstadoEncuesta.INCOMPLETO)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer,
                ),
            ) {
                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MÓDULO DE VISITAS  (entity_visita)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun VisitasModule(
    visitas: List<VisitaRowUi>,
    minRegistros: Int = 1,
    maxRegistros: Int? = null,
    soloLectura: Boolean = false,
    hasError: Boolean = false,
    onRegistrarVisita: () -> Unit,
    onEditarVisita: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val puedeAgregar = !soloLectura && (maxRegistros == null || visitas.size < maxRegistros)

    EntityModuleContainer(
        titulo       = "Visitas registradas",
        conteo       = visitas.size,
        minRegistros = minRegistros,
        hasError     = hasError,
        iconoTitulo  = Icons.Default.CalendarToday,
        modifier     = modifier,
    ) {
        if (visitas.isEmpty()) {
            EntityEmptyState("No se han registrado visitas aún.")
        } else {
            LazyColumn(
                modifier        = Modifier.heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding  = PaddingValues(vertical = 4.dp),
            ) {
                items(visitas, key = { it.id }) { visita ->
                    VisitaRow(
                        visita      = visita,
                        soloLectura = soloLectura,
                        onEditar    = { onEditarVisita(visita.id) },
                    )
                }
            }
        }

        if (puedeAgregar) {
            Spacer(Modifier.height(8.dp))
            EntityAddButton(
                label   = "Registrar Visita",
                icono   = Icons.Default.AddTask,
                onClick = onRegistrarVisita,
            )
        }
    }
}

@Composable
private fun VisitaRow(
    visita: VisitaRowUi,
    soloLectura: Boolean,
    onEditar: () -> Unit,
) {
    val bgColor = resultadoVisitaColor(visita.resultado)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        EntityBadge(numero = visita.numeroVisita)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ResultadoChip(visita.resultado)
            }
            Spacer(Modifier.height(2.dp))
            if (visita.fechaHora.isNotBlank())
                EntityStat(Icons.Default.Schedule, visita.fechaHora)
            if (visita.encuestador.isNotBlank())
                EntityStat(Icons.Default.Person, visita.encuestador)
            if (visita.proximaVisita.isNotBlank())
                EntityStat(Icons.Default.Event, "Próxima: ${visita.proximaVisita}")
        }

        if (!soloLectura) {
            FilledTonalIconButton(
                onClick  = onEditar,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  COMPONENTES COMPARTIDOS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EntityModuleContainer(
    titulo: String,
    conteo: Int,
    minRegistros: Int,
    hasError: Boolean,
    iconoTitulo: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val borderColor = if (hasError) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Encabezado del módulo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(iconoTitulo, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            // Badge con conteo
            Surface(
                shape = CircleShape,
                color = if (hasError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = conteo.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasError) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        // Aviso de mínimo requerido
        if (hasError && conteo < minRegistros) {
            Text(
                text  = "Se requiere al menos $minRegistros registro(s)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))

        content()
    }
}

@Composable
private fun EntityBadge(numero: Int) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        modifier = Modifier.size(30.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "$numero",
                fontWeight = FontWeight.Bold,
                fontSize   = 12.sp,
                color      = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EntityStat(icono: ImageVector, texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(icono, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(texto, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EstadoChip(estado: EstadoEncuesta) {
    val (color, textColor) = estadoChipColors(estado)
    Surface(shape = RoundedCornerShape(6.dp), color = color) {
        Text(
            text     = estado.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color    = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ResultadoChip(resultado: ResultadoVisita) {
    val (color, textColor) = resultadoChipColors(resultado)
    Surface(shape = RoundedCornerShape(6.dp), color = color) {
        Text(
            text     = resultado.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color    = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun EntityEmptyState(mensaje: String) {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text      = mensaje,
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EntityAddButton(label: String, icono: ImageVector, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(8.dp),
    ) {
        Icon(icono, null, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPERS DE COLOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun estadoEncuestaColor(estado: EstadoEncuesta): Color = when (estado) {
    EstadoEncuesta.COMPLETO   -> Color(0xFFE8F5E9)
    EstadoEncuesta.INCOMPLETO -> Color(0xFFFFF8E1)
    EstadoEncuesta.RECHAZO    -> Color(0xFFFFEBEE)
    EstadoEncuesta.AUSENTE    -> Color(0xFFF5F5F5)
    EstadoEncuesta.NUEVO      -> Color.Transparent
}

@Composable
private fun estadoChipColors(estado: EstadoEncuesta): Pair<Color, Color> = when (estado) {
    EstadoEncuesta.COMPLETO   -> Color(0xFFDCFCE7) to Color(0xFF166534)
    EstadoEncuesta.INCOMPLETO -> Color(0xFFFEF9C3) to Color(0xFF854D0E)
    EstadoEncuesta.RECHAZO    -> Color(0xFFFFE4E6) to Color(0xFF9F1239)
    EstadoEncuesta.AUSENTE    -> Color(0xFFF3F4F6) to Color(0xFF374151)
    EstadoEncuesta.NUEVO      -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
}

private fun resultadoVisitaColor(resultado: ResultadoVisita): Color = when (resultado) {
    ResultadoVisita.COMPLETO  -> Color(0xFFE8F5E9)
    ResultadoVisita.AUSENTE   -> Color(0xFFF5F5F5)
    ResultadoVisita.RECHAZO   -> Color(0xFFFFEBEE)
    ResultadoVisita.PENDIENTE -> Color(0xFFFFF8E1)
}

@Composable
private fun resultadoChipColors(resultado: ResultadoVisita): Pair<Color, Color> = when (resultado) {
    ResultadoVisita.COMPLETO  -> Color(0xFFDCFCE7) to Color(0xFF166534)
    ResultadoVisita.AUSENTE   -> Color(0xFFF3F4F6) to Color(0xFF374151)
    ResultadoVisita.RECHAZO   -> Color(0xFFFFE4E6) to Color(0xFF9F1239)
    ResultadoVisita.PENDIENTE -> Color(0xFFFEF9C3) to Color(0xFF854D0E)
}