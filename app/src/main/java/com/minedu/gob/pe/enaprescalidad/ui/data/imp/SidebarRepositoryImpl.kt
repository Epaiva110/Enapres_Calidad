package com.minedu.gob.pe.enaprescalidad.ui.data.imp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.remember
import com.minedu.gob.pe.enaprescalidad.ui.domain.model.SidebarItem
import com.minedu.gob.pe.enaprescalidad.ui.domain.repository.SidebarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Implementación local del repositorio.
 *
 * Para cambiar a una fuente remota (Retrofit, Firebase, etc.)
 * solo reemplaza esta clase — el dominio y la UI no cambian.
 */

@Singleton
class SidebarRepositoryImpl @Inject constructor() : SidebarRepository {
    override fun getItems(): Flow<List<SidebarItem>> = flowOf(
        listOf(
            SidebarItem(
                id = "home",
                label = "Inicio",
                icon = Icons.Default.Home,
                titleMenu = "ENAPRES - Control de Calidad de Datos"
            ),
            SidebarItem(
                id = "verificacion",
                label = "Verificación",
                icon = Icons.AutoMirrored.Filled.FactCheck,
                titleMenu = "Verificación",
                children = listOf(
                    SidebarItem(
                        id = "verificacionConglomerado",
                        label = "Conglomerado",
                        icon = Icons.Default.LocationCity,
                        titleMenu = "Verificación - Conglomerado"
                    ),
                    SidebarItem(
                        id = "verificacionVivienda",
                        label = "Vivienda",
                        icon = Icons.Default.House,
                        titleMenu = "Verificación - Vivienda"
                    ),
                    SidebarItem(
                        id = "verificacionReeentrevista",
                        label = "Reeentrevista",
                        icon = Icons.Default.Quiz,
                        titleMenu = "Verificación - Reentrevista"
                    )
                )
            ),
            SidebarItem(
                id = "CargaMarco",
                label = "Carga de Marco",
                icon = Icons.Default.CloudUpload,
                titleMenu = "Carga de Marco"
            ),
            SidebarItem(
                id = "AvanceCampo",
                label = "Avance de Campo",
                icon = Icons.Default.BarChart,
                titleMenu = "Avance de Campo"
            )
        )
    )
}
