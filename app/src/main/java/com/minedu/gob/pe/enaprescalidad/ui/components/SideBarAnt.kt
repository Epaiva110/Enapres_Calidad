package com.minedu.gob.pe.enaprescalidad.ui.components

import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding


import androidx.compose.material.icons.filled.Menu

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.ui.unit.dp

import com.minedu.gob.pe.enaprescalidad.ui.navigation.Routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface

import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation3.runtime.NavKey


@Composable
fun SideBarAnt(
    currentRoute: NavKey, // Se mantiene como NavKey
    codsup: String,
    onNavigate: (NavKey) -> Unit, // CAMBIO: Ahora acepta NavKey para ser compatible con el Stack
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val menuItems = rememberMenuItems(codsup)

    val width by animateDpAsState(
        targetValue = if (isExpanded) 250.dp else 72.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "width"
    )

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(width),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            // Toggle Button
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(if (isExpanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Default.Menu, null)
            }

            SidebarUserHeader(isExpanded = isExpanded, userId = codsup)

            HorizontalDivider(Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp)

            // Menu List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp)
            ) {
                menuItems.forEach { option ->
                    SidebarRecursiveItem(
                        option = option,
                        isExpanded = isExpanded,
                        currentRoute = currentRoute,
                        onNavigate = onNavigate
                    )
                }
            }

            // Footer
            NavigationItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                label = "Cerrar Sesión",
                isSelected = false,
                isExpanded = isExpanded,
                onClick = onLogout,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun SidebarRecursiveItem(
    option: SidebarOption,
    isExpanded: Boolean,
    currentRoute: NavKey,
    onNavigate: (NavKey) -> Unit, // CAMBIO: NavKey
    level: Int = 0
) {
    // Comparación mejorada para detectar si la ruta actual coincide con la opción
    val isSelected = option.route != null && currentRoute::class == option.route::class
    val hasChildren = option.children.isNotEmpty()

    var isSubMenuOpen by remember {
        mutableStateOf(option.children.any { it.route != null && currentRoute::class == it.route::class })
    }

    Column {
        NavigationItem(
            icon = option.icon,
            label = option.label,
            isSelected = isSelected || (hasChildren && !isExpanded && option.children.any { it.route != null && currentRoute::class == it.route::class }),
            isExpanded = isExpanded,
            indentation = level,
            onClick = {
                if (hasChildren && isExpanded) {
                    isSubMenuOpen = !isSubMenuOpen
                } else if (option.route != null) {
                    onNavigate(option.route) // Navega usando la ruta definida
                }
            }
        )

        if (isSubMenuOpen && isExpanded && hasChildren) {
            option.children.forEach { child ->
                SidebarRecursiveItem(
                    option = child,
                    isExpanded = isExpanded,
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    level = level + 1
                )
            }
        }
    }
}

@Composable
fun SidebarUserHeader(
    isExpanded: Boolean,
    userId: String,
    role: String = "Supervisor"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        if (isExpanded) {
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = userId,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    indentation: Int = 0,
    badgeCount: Int? = null // Funcionalidad extra para el futuro
) {
    val colorBySelection = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = { if (!isExpanded) PlainTooltip { Text(label) } },
        state = rememberTooltipState()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = (8 + indentation * 16).dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
                .height(48.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(containerColor)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorBySelection,
                modifier = Modifier.size(24.dp)
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Row(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = colorBySelection,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


@Composable
fun rememberMenuItems(codsup: String): List<SidebarOption> {
    return remember(codsup) {
        listOf(
            SidebarOption(
                icon = Icons.Default.Home,
                label = "Inicio",
                //route = Routes.Welcome(codsup)
            ),
            SidebarOption(
                icon = Icons.AutoMirrored.Filled.FactCheck,
                label = "Verificación",
                // Aquí defines los sub-niveles de forma anidada
                children = listOf(
                    SidebarOption(
                        icon = Icons.Default.LocationCity,
                        label = "Conglomerado",
                        //route = Routes.Map
                    ),
                    SidebarOption(
                        icon = Icons.Default.House,
                        label = "Viviendas"
                        // Puedes dejar route = null si aún no está implementada
                    ),
                    SidebarOption(
                        icon = Icons.Default.Quiz,
                        label = "Reentrevistas"
                    )
                )
            ),
            SidebarOption(
                icon = Icons.Default.BarChart,
                label = "Avance de Campo"
            ),
            SidebarOption(
                icon = Icons.Default.CloudUpload,
                label = "Carga de Marco"
            )
        )
    }
}

/**
 * Representa un elemento individual en la barra lateral.
 * @param icon El icono a mostrar.
 * @param label El texto descriptivo.
 * @param route La ruta de navegación (opcional si tiene hijos).
 * @param children Lista de sub-elementos para menús desplegables.
 * @param badge El número de alertas o tareas (ej. "5 viviendas pendientes").
 */
data class SidebarOption(
    val icon: ImageVector,
    val label: String,
    val route: Routes? = null, // Cambiado de Companion a Routes?
    val children: List<SidebarOption> = emptyList(),
    val badge: String? = null
)


