package com.minedu.gob.pe.enaprescalidad.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SidebarItem

/**
 * Sidebar animado colapsable/expandible.
 *
 * Recibe la lista de ítems y el id seleccionado — no sabe nada
 * del ViewModel ni de la fuente de datos.
 */

@Composable
fun SideBar(
    usuario: String,
    nombre: String,
    role: String,
    items: List<SidebarItem>,
    selectedItemId: String,
    expandedItemIds: Set<String>,
    onItemSelected: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
    ) {

        SidebarUserHeader(usuario = usuario, nombre = nombre, role = role)

        HorizontalDivider(
            Modifier.padding(horizontal = 12.dp),
            thickness = 0.5.dp
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {
            items.forEach { item ->
                SideBarItem(
                    item = item,
                    selectedItemId = selectedItemId,
                    expandedItemIds = expandedItemIds,
                    onItemSelected = onItemSelected,
                    onToggleExpand = onToggleExpand,
                    depth = 0   // nivel de profundidad para el padding
                )
            }
        }

        HorizontalDivider(
            Modifier.padding(horizontal = 12.dp),
            thickness = 0.5.dp
        )

        SidebarLogoutButton(
            onClick = { showLogoutDialog = true }
        )
    }
}

@Composable
fun SidebarBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
fun SidebarIcon(
    icon: ImageVector,
    label: String,
    tint: Color,
    badge: Int? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        if (badge != null && badge > 0) {
            SidebarBadge(
                count = badge,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-4).dp)
            )
        }
    }
}

@Composable
fun SideBarItem(
    item: SidebarItem,
    selectedItemId: String,
    expandedItemIds: Set<String>,
    onItemSelected: (String) -> Unit,
    onToggleExpand: (String) -> Unit,
    depth: Int = 0
) {
    val isSelected = item.id == selectedItemId
    val isExpanded = item.id in expandedItemIds

    // Nueva lógica: ¿Algún hijo de este ítem está seleccionado?
    val hasSelectedChild = remember(item, selectedItemId) {
        item.children.any { it.id == selectedItemId } ||
                item.children.flatMap { it.children }.any { it.id == selectedItemId } // Para niveles más profundos
    }

    // Color de contenido: Se ilumina si está seleccionado O si tiene un hijo seleccionado y está contraído
    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            hasSelectedChild && !isExpanded -> MaterialTheme.colorScheme.primary // Iluminar si el hijo está oculto
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "item_color"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .height(48.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else Color.Transparent
                )
                .clickable {
                    if (item.hasChildren) onToggleExpand(item.id)
                    else onItemSelected(item.id)
                }
                .padding(start = (16 + (depth * 16)).dp, end = 12.dp), // Indentación dinámica
            verticalAlignment = Alignment.CenterVertically
        ) {
            SidebarIcon(
                icon = item.icon,
                label = item.label,
                tint = contentColor,
                badge = item.badge
            )

            Text(
                text = item.label,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )

            if (item.hasChildren) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // INDICADOR VISUAL: Si está contraído y tiene un hijo seleccionado, mostrar un punto
                    if (hasSelectedChild && !isExpanded) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }

                    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp).rotate(rotation)
                    )
                }
            }
        }

        // Animación de entrada para los hijos (opcional pero recomendada)
        AnimatedVisibility(visible = item.hasChildren && isExpanded) {
            Column {
                item.children.forEach { child ->
                    SideBarItem(child, selectedItemId, expandedItemIds, onItemSelected, onToggleExpand, depth + 1)
                }
            }
        }
    }
}

@Composable
fun SidebarUserHeader(usuario: String, nombre: String, role: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp) // Más aire
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = nombre,
                style = MaterialTheme.typography.titleSmall, // Más prominente
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = usuario,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun SidebarLogoutButton(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .requiredHeight(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Cerrar sesión",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(22.dp)
        )

        Text(
            text = "Cerrar Sesión",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

// Dialog extraído — reutilizable para cualquier confirmación destructiva
@Composable
fun LogoutConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Cerrar sesión?") },
        text = { Text("¿Estás seguro que quieres salir?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Salir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}