package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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

import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.unit.dp

import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.Routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PlainTooltip

import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.minedu.gob.pe.encuestasatisfaccinenapres.models.LoginViewModel
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.core.ex.navigateTo
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.core.ex.replace


@Composable
fun SideBar(
    backStack: NavBackStack<NavKey>,
    codsup: String
) {
    val viewModel: LoginViewModel = viewModel()
    var expanded by remember { mutableStateOf(false) }
    val currentRoute = remember(backStack) { backStack.last() }

    val width by animateDpAsState(
        targetValue = if (expanded) 230.dp else 70.dp,
        animationSpec = tween(300),
        label = "sidebarWidth"
    )

    val menuItems = remember {
        listOf(
            SidebarOption(Icons.Default.Home, "Inicio", Routes.Home),
            SidebarOption(
                Icons.AutoMirrored.Filled.FactCheck,
                "Verificación",
                children = listOf(
                    SidebarOption(Icons.Default.LocationCity, "Conglomerado", Routes.Map),
                    SidebarOption(Icons.Default.House, "Viviendas"),
                    SidebarOption(Icons.Default.Quiz, "Reeentrevistas")
                )
            ),
            SidebarOption(Icons.Default.BarChart, "Avance de Campo"),
            SidebarOption(Icons.Default.CloudUpload, "Carga de Marco")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 12.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        // 🔹 Botón menú
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }

        HorizontalDivider(Modifier.padding(horizontal = 8.dp))
        Spacer(Modifier.height(16.dp))

        // 🔹 Header
        SideBarHeader(expanded = expanded, codsup = codsup)

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(Modifier.padding(horizontal = 8.dp))

        // 🔹 Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SidebarList(
                items = menuItems,
                expanded = expanded,
                currentRoute = currentRoute,
                backStack = backStack
            )
        }

        // 🔹 Footer
        HorizontalDivider(Modifier.padding(horizontal = 8.dp))
        Spacer(Modifier.height(8.dp))

        SideBarItem(
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            label = "Cerrar Sesión",
            selected = false,
            expanded = expanded,
            tint = MaterialTheme.colorScheme.error,
            onClick = {
                viewModel.logout()
                backStack.clear()
                backStack.navigateTo(Routes.Login)
                //backStack.replace(Routes.Login)
        }
        )
    }
}

data class SidebarOption(
    val icon: ImageVector,
    val label: String,
    val route: NavKey? = null,
    val children: List<SidebarOption> = emptyList()
)

@Composable
fun SideBarHeader(expanded: Boolean, codsup: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        if (expanded) {
            Spacer(Modifier.width(12.dp))
            Column {
                Text(codsup, fontWeight = FontWeight.Bold)
                Text("Supervisor", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun SidebarList(
    items: List<SidebarOption>,
    expanded: Boolean,
    currentRoute: NavKey,
    backStack: NavBackStack<NavKey>,
    level: Int = 0
) {
    Column {
        items.forEach { item ->

            var expandedItem by remember { mutableStateOf(false) }

            val isSelected = item.route?.let {
                currentRoute::class == it::class
            } == true

            val hasChildren = item.children.isNotEmpty()

            // 🔹 Auto-expand si una ruta hija está activa
            val isChildSelected = item.children.any {
                it.route?.let { r -> currentRoute::class == r::class } == true
            }

            if (isChildSelected) expandedItem = true

            SideBarItem(
                icon = item.icon,
                label = item.label,
                selected = isSelected || (!expanded && isChildSelected),
                expanded = expanded,
                onClick = {
                    when {
                        hasChildren && expanded -> expandedItem = !expandedItem
                        item.route != null -> backStack.navigateTo(item.route)
                    }
                },
                indent = level
            )

            if (expandedItem && expanded && hasChildren) {
                SidebarList(
                    items = item.children,
                    expanded = expanded,
                    currentRoute = currentRoute,
                    backStack = backStack,
                    level = level + 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SideBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    indent: Int = 0
) {

    val (containerColor, contentColor) = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) to MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent to tint
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = {
            if (!expanded) {
                PlainTooltip {
                    Text(label)
                }
            }
        },
        state = rememberTooltipState()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (12 + indent * 12).dp, end = 12.dp, top = 2.dp, bottom = 2.dp)
                .height(50.dp)
                .background(containerColor, shape = MaterialTheme.shapes.medium)
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = contentColor)
            }

            if (expanded) {
                Spacer(Modifier.width(16.dp))
                Text(
                    text = label,
                    color = contentColor,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}