package com.example.sidebarapp.ui.main

import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SidebarItem


/**
 * Estado inmutable de la pantalla principal.
 *
 * La UI solo lee este objeto — nunca lo muta directamente.
 * Toda mutación pasa por el ViewModel.
 */
data class MainUiState(
    val codsup: String = "",
    val sidebarItems: List<SidebarItem> = emptyList(),
    val selectedItemId: String = "",
    val isLoading: Boolean = true,
    val expandedItemIds: Set<String> = emptySet(),
) {
    /** Ítem actualmente seleccionado (null mientras carga). */
    val selectedItem: SidebarItem?
        get() = sidebarItems.find { it.id == selectedItemId }
}


