package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens
import android.Manifest
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.CameraPositionState
import com.minedu.gob.pe.encuestasatisfaccinenapres.models.LocationData
import com.minedu.gob.pe.encuestasatisfaccinenapres.models.LocationViewModel
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.core.ex.back
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.utils.SetupMapSystemUI
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable

fun MapVisor(backStack: NavBackStack<NavKey>) {

    val viewModel: LocationViewModel = viewModel()
    ModernMapScreen(
        viewModel = viewModel,
        onAccept = { backStack.back() } // Usa tu extensión .back()
    )
}

// --- 1. ESTADO DE UI ENCAPSULADO (State Holder) ---
class MapStateHolder(
    val cameraPosition: CameraPositionState,
    val scope: kotlinx.coroutines.CoroutineScope
) {
    fun animateTo(lat: Double, lng: Double) {
        scope.launch {
            cameraPosition.animate(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 16f))
        }
    }
}

@Composable
fun rememberMapStateHolder(
    cameraPosition: CameraPositionState = rememberCameraPositionState()
): MapStateHolder {
    val scope = rememberCoroutineScope()
    return remember { MapStateHolder(cameraPosition, scope) }
}

// --- 2. ÁTOMOS (Componentes Reutilizables) ---

@Composable
fun ModernSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            placeholder = { Text("Buscar conglomerado...", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize(),
            singleLine = true
        )
    }
}

@Composable
fun ModernFilterPills(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Conglomerados", "Viviendas")
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            val backgroundColor by animateColorAsState(
                if (isSelected) Color(0xFF00AA55) else Color.Transparent, label = ""
            )
            val contentColor by animateColorAsState(
                if (isSelected) Color.White else Color.Gray, label = ""
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun RoundedActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    modifier: Modifier = Modifier
) {
    LargeFloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
    }
}

// --- 3. ORGANISMO (La Pantalla Principal) ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ModernMapScreen(
    viewModel: LocationViewModel = viewModel(),
    onAccept: () -> Unit
) {
    val uiState = rememberMapStateHolder()
    val locationData by viewModel.locationState.collectAsState()
    val permissionState = rememberMultiplePermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var hasInitialCentered by remember { mutableStateOf(false) }

    // Solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    // Centrado automático solo la primera vez que hay datos
    LaunchedEffect(locationData) {
        if (locationData != null && !hasInitialCentered) {
            uiState.animateTo(locationData!!.latitude, locationData!!.longitude)
            hasInitialCentered = true
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 110.dp,
        sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        sheetContainerColor = Color.White,
        sheetContent = {
            // Contenido del BottomSheet atomizado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "Detalles de Zona",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF003366)
                )
                Spacer(Modifier.height(8.dp))
                Text("Desliza para ver más información del sector actual.", color = Color.Gray)
                Spacer(Modifier.height(100.dp)) // Espacio para contenido extra
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // --- CAPA 1: EL MAPA ---
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = uiState.cameraPosition,
                properties = MapProperties(
                    isMyLocationEnabled = permissionState.allPermissionsGranted,
                    mapStyleOptions = null // Aquí podrías poner un estilo Dark o Silver
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
            ) {
                locationData?.let { data ->
                    Marker(state = MarkerState(LatLng(data.latitude, data.longitude)))
                }
            }

            // --- CAPA 2: CABECERA FLOTANTE (Search + Tabs) ---
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ModernSearchBar(
                    value = searchQuery,
                    onValueChange = { searchQuery = it }
                )
                Spacer(Modifier.height(12.dp))
                ModernFilterPills(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // --- CAPA 3: ACCIONES LATERALES ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 140.dp, end = 16.dp), // Separación para que no lo tape el Sheet
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón mi ubicación minimalista
                SmallFloatingActionButton(
                    onClick = { locationData?.let { uiState.animateTo(it.latitude, it.longitude) } },
                    containerColor = Color.White,
                    contentColor = Color(0xFF2196F3),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null)
                }

                // Botón de acción principal (Modern FAB)
                RoundedActionButton(
                    icon = Icons.Default.Check,
                    containerColor = Color(0xFF003366),
                    onClick = onAccept
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreenSafeAntiguo(
    viewModel: LocationViewModel = viewModel(),
    onAccept: () -> Unit
) {
    // Configuración automática de UI
    SetupMapSystemUI()
    val scope = rememberCoroutineScope()

    // --- 2. ESTADOS ---
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    )
    val locationData by viewModel.locationState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    var hasInitialCentered by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    // Centrado único inicial
    LaunchedEffect(locationData) {
        if (locationData != null && !hasInitialCentered) {
            locationData?.let { data ->
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(data.latitude, data.longitude), 16f
                )
                hasInitialCentered = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            // El FAB usa padding del sistema para la barra de navegación (Gestos/Botones)
            FloatingActionButton(
                onClick = { onAccept() },
                containerColor = Color(0xFF003366),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Default.Check, contentDescription = "Aceptar")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {

            // --- MAPA ---
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = permissionState.allPermissionsGranted,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
            ) {
                locationData?.let { data ->
                    Marker(
                        state = MarkerState(LatLng(data.latitude, data.longitude)),
                        title = "Mi Ubicación"
                    )
                }
            }

            // --- CABECERA DINÁMICA ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding() // Padding adaptativo según el notch/cámara de cada cel
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "Visor de Campo - ENAPRES",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF003366),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Buscar conglomerado...", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedIndicatorColor = Color(0xFF003366),
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        val tabs = listOf("Conglomerados", "Viviendas")
                        tabs.forEachIndexed { index, title ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = index }
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    color = if (selectedTab == index) Color(0xFF00AA55) else Color.Gray,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                                if (selectedTab == index) {
                                    Spacer(Modifier.height(4.dp))
                                    Box(Modifier.fillMaxWidth(0.4f).height(3.dp).background(Color(0xFF00AA55), CircleShape))
                                }
                            }
                        }
                    }
                }
            }

            // --- BOTÓN CENTRAR CON POSICIONAMIENTO DINÁMICO ---
            // Usamos un Column para apilar el espacio dinámico y evitar que choque con el FAB
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding() // Respeta la barra de gestos de Android
                    .padding(16.dp)
                    .padding(bottom = 72.dp) // Este padding es relativo al FAB, no a la pantalla
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        locationData?.let { data ->
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(data.latitude, data.longitude), 16f)
                                )
                            }
                        }
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF2196F3),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Centrar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreenGoogleStyle(
    viewModel: LocationViewModel = viewModel(),
    onAccept: () -> Unit
) {
    SetupMapSystemUI()

    val scope = rememberCoroutineScope()

    // Permisos
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val locationData by viewModel.locationState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    var hasCentered by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    // Centrado inicial
    LaunchedEffect(locationData) {
        if (locationData != null && !hasCentered) {
            locationData?.let {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(it.latitude, it.longitude),
                    16f
                )
                hasCentered = true
            }
        }
    }

    val sheetState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetPeekHeight = 90.dp,
        sheetContainerColor = Color.White,
        sheetShadowElevation = 16.dp,
        sheetContent = {
            Column(modifier = Modifier.padding(16.dp)) {

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.Gray, RoundedCornerShape(50))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Opciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Text("📍 Conglomerados")
                Text("🏠 Viviendas")

                Spacer(Modifier.height(100.dp))
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // 🗺️ MAPA
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = permissionState.allPermissionsGranted,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true
                )
            ) {
                locationData?.let {
                    Marker(
                        state = MarkerState(
                            LatLng(it.latitude, it.longitude)
                        ),
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        ),
                        title = "Mi ubicación"
                    )
                }
            }

            // 🔍 SEARCH BAR (flotante)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    placeholder = { Text("Buscar en el mapa") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            // 🎯 BOTONES DERECHA
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Centrar ubicación
                SmallFloatingActionButton(
                    onClick = {
                        locationData?.let {
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(it.latitude, it.longitude),
                                        16f
                                    )
                                )
                            }
                        }
                    },
                    containerColor = Color.White
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Ubicación")
                }

                // Tipo mapa (placeholder)
                SmallFloatingActionButton(
                    onClick = { /* cambiar tipo */ },
                    containerColor = Color.White
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Capas")
                }
            }

            // ✅ BOTÓN CONFIRMAR (opcional estilo Maps)
            FloatingActionButton(
                onClick = { onAccept() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                containerColor = Color(0xFF1A73E8)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Aceptar")
            }
        }
    }
}