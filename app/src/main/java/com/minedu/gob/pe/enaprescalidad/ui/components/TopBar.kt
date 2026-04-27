package com.minedu.gob.pe.enaprescalidad.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation3.runtime.NavKey
import com.minedu.gob.pe.enaprescalidad.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(currentRoute: NavKey) {
    val title = remember(currentRoute) {
        getTitleFromRoute(currentRoute)
    }

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    )
}

fun getTitleFromRoute(route: NavKey): String {
    return when (route::class) {
        Routes.Welcome::class -> "Inicio"
        //Routes.Map::class -> "Conglomerado"
        Routes.Login::class -> "Login"
        else -> "Supervisión de Control de Calidad"
    }
}