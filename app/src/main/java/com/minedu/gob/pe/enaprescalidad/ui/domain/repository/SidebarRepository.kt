package com.minedu.gob.pe.enaprescalidad.ui.domain.repository

import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SidebarItem
import kotlinx.coroutines.flow.Flow

/**
 * Contrato que define cómo se obtiene la lista de ítems del sidebar.
 *
 * Depender de esta interfaz (no de la implementación) permite:
 *  - Cambiar la fuente de datos (local, red, BBDD) sin tocar la UI.
 *  - Mockear fácilmente en tests.
 */
interface SidebarRepository {
    fun getItems(): Flow<List<SidebarItem>>
}