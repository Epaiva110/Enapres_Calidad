package com.minedu.gob.pe.enaprescalidad.ui.domain.usecase

import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SidebarItem
import com.minedu.gob.pe.enaprescalidad.ui.domain.repository.SidebarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso: obtener la lista de ítems del sidebar.
 *
 * Si en el futuro necesitas filtrar ítems por rol de usuario,
 * ordenarlos, o combinar varias fuentes, la lógica va aquí,
 * sin tocar el ViewModel ni el repositorio.
 */
class GetSidebarItemsUseCase @Inject constructor(
    private val repository: SidebarRepository
) {
    operator fun invoke(): Flow<List<SidebarItem>> = repository.getItems()
}
