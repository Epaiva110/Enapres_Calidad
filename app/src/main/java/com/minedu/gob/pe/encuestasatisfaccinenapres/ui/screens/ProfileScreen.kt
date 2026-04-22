package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.Routes
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.components.MainTopBar
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.components.SideBar
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.utils.SetupMapSystemUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    id: String,
    backStack: NavBackStack<NavKey>,
    navigateBack: () -> Unit
) {
    val currentRoute = backStack.last()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {

        // 🔹 TOP BAR (arriba correctamente)
        //MainTopBar(currentRoute = currentRoute)

        // 🔹 CONTENIDO (sidebar + pantalla)
        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            // Sidebar
            SideBar(
                backStack = backStack,
                codsup = id
            )

            // Contenido dinámico
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                when (currentRoute) {
                    is Routes.Home -> {
                        ConglomeradoScreen(backStack = backStack)
                    }

                    is Routes.Main -> {
                        ConglomeradoScreen(backStack = backStack)
                    }

                    is Routes.Map -> {
                        MapVisor(backStack = backStack)
                    }

                    else -> {
                        ConglomeradoScreen(backStack = backStack)
                    }
                }
            }
        }
    }
}