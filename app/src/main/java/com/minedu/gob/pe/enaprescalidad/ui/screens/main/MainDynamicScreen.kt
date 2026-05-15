package com.minedu.gob.pe.enaprescalidad.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.minedu.gob.pe.enaprescalidad.ui.components.MainContent
import com.minedu.gob.pe.enaprescalidad.ui.components.SideBar
import com.minedu.gob.pe.enaprescalidad.ui.navigation.Routes
import com.minedu.gob.pe.enaprescalidad.ui.navigation.core.ex.navigateTo
import com.minedu.gob.pe.enaprescalidad.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla raíz del layout principal.
 *
 * Responsabilidades:
 *  - Observar el UiState del ViewModel.
 *  - Pasar datos hacia abajo (SideBar, MainContent).
 *  - Reenviar eventos hacia arriba (onItemSelected).
 *
 * No contiene lógica de negocio.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDynamicScreen(
    viewModelMain: MainViewModel = hiltViewModel(),
    viewModelLogin: LoginViewModel = hiltViewModel(),
    backStack: NavBackStack<NavKey>,
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val uiState by viewModelMain.uiState.collectAsStateWithLifecycle()
    val user by viewModelLogin.currentUser.collectAsStateWithLifecycle()
    val currentTitle by viewModelMain.currentTitle.collectAsStateWithLifecycle()

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Closed) {
            viewModelMain.onDrawerClosed()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Blue.copy(alpha = 0.3f),
        drawerContent = {
            ModalDrawerSheet {
                SideBar(
                    usuario = user?.user ?: "",
                    nombre = user?.user_name ?: "",
                    role = user?.role ?: "",
                    items = uiState.sidebarItems,
                    selectedItemId = uiState.selectedItemId,
                    expandedItemIds = uiState.expandedItemIds,
                    onItemSelected = {
                        viewModelMain.onItemSelected(it)
                        scope.launch { drawerState.close() }
                    },
                    onToggleExpand = viewModelMain::onToggleExpand,
                    onLogout = {
                        viewModelMain.onLogout()
                        viewModelLogin.logout()
                        backStack.clear()
                        backStack.navigateTo(Routes.Login)
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0D47A1), // azul institucional
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically
                        ) {
//                            Icon(
//                                Icons.Default.Assessment,
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp)
//                            )
                            Spacer(Modifier.width(6.dp))

                            Column{
                                Text(
                                    text = currentTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "ENAPRES - 2026",
                                    style = MaterialTheme.typography.labelSmall,
                                    //color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                )
            }
        ) { paddingValues ->
            MainContent(
                selectedItemId = uiState.selectedItemId,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}