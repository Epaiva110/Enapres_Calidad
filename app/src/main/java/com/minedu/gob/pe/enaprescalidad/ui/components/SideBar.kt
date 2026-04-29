package com.minedu.gob.pe.enaprescalidad.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    codsup: String,
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

        SidebarUserHeader(codsup = codsup)

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
                    isSelected = item.id == selectedItemId,
                    isSubmenuExpanded = item.id in expandedItemIds,
                    onClick = { onItemSelected(item.id) },
                    onToggleExpand = if (item.hasChildren) {
                        { onToggleExpand(item.id) }
                    } else null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideBarItem(
    item: SidebarItem,
    isSelected: Boolean,
    isSubmenuExpanded: Boolean = false,
    onClick: () -> Unit,
    onToggleExpand: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "item_color"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else
            Color.Transparent,
        label = "item_bg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .height(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SidebarIcon(
            icon = item.icon,
            label = item.label,
            tint = contentColor,
            badge = item.badge
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (item.hasChildren && onToggleExpand != null) {
            val rotation by animateFloatAsState(
                targetValue = if (isSubmenuExpanded) 90f else 0f,
                label = "arrow_rotation"
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(rotation)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onToggleExpand
                    )
            )
        }
    }

    if (item.hasChildren && isSubmenuExpanded) {
        Column(modifier = Modifier.padding(start = 16.dp)) {
            item.children.forEach { child ->
                SideBarItem(
                    item = child,
                    isSelected = false,
                    isSubmenuExpanded = false,
                    onClick = onClick
                )
            }
        }
    }
}


// Header de usuario reutilizable
@Composable
fun SidebarUserHeader(codsup: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(4.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Text(
            text = codsup,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            modifier = Modifier.padding(start = 10.dp)
        )
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