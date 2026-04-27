package com.minedu.gob.pe.enaprescalidad.ui.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modelo de dominio que representa un ítem del sidebar.
 *
 * Es una data class pura (sin @Composable, sin Context) para que sea
 * fácilmente testeable y reutilizable.
 *
 * @param id         Identificador único. Usado como clave de selección.
 * @param label      Texto visible en el sidebar.
 * @param icon       Ícono de Material Icons.
 * @param parentId   Indica de donde será sub menu.
 * @param route      Indica menu que abrira.
 * @param roles      Indica quienes pueden acceder a este menu.
 */

//data class SidebarItem(
//    val id: String = "",
//    val label: String = "",
//    val icon: String = "",
//    val parentId: String? = null,
//    val route: String? = null,
//    //val roles: List<String> = emptyList()
//)

data class SidebarItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val badge: Int? = null,               // para notificaciones futuras
    val children: List<SidebarItem> = emptyList(),
    val titleMenu: String?

) {
    val hasChildren: Boolean get() = children.isNotEmpty()
}
