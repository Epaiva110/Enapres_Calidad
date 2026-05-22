package com.minedu.gob.pe.enaprescalidad.ui.components.maps


import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.*
import com.minedu.gob.pe.enaprescalidad.utils.openAppSettings
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Modo de captura
// ─────────────────────────────────────────────────────────────────────────────

enum class MapMode { AUTO, MANUAL }

// ─────────────────────────────────────────────────────────────────────────────
// GoogleMapBase — mapa puro + overlays de hardware compartidos
// Usado por MapScreen (auto) y ManualMarkerMap (manual)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GoogleMapBase(
    isAuto: Boolean,
    state: MapState,
    modifier: Modifier = Modifier,
    // FAB de centrar: el caller decide si lo muestra y con qué offset
    showCenterButton: Boolean = false,
    centerButtonBottomPadding: Dp = 16.dp,
    // Clicks sobre el mapa (solo modo manual los usa)
    onMapClick: ((LatLng) -> Unit)? = null,
    // En la firma de GoogleMapBase:
    mapContent: (@Composable () -> Unit)? = null,
    //mapContent: (@Composable @GoogleMapComposable () -> Unit)? = null,
    // Contenido extra superpuesto al mapa (markers, paneles, etc.)
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val context = LocalContext.current
    val uiState = state.uiState

    Box(modifier = modifier) {

        // ── Mapa Google ───────────────────────────────────────────────────────
        GoogleMap(
            modifier            = Modifier.fillMaxSize(),
            properties          = MapProperties(isMyLocationEnabled = state.permisosOtorgados),
            uiSettings          = MapUiSettings(myLocationButtonEnabled = false),
            cameraPositionState = state.cameraPositionState,
            onMapClick          = { latLng -> onMapClick?.invoke(latLng) },

            content             = {
                // CORRECCIÓN: Invocar el contenido del mapa pasando el scope correcto
                mapContent?.invoke()
            }

//            content = {
//                Marker()
//            }
        )

        // ── FAB centrar ubicación ─────────────────────────────────────────────
        if (showCenterButton && uiState.locationData.tieneCoordenadasValidas && !isAuto) {
            FloatingActionButton(
                onClick        = state.onCenterCamera,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier       = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp, bottom = centerButtonBottomPadding)
                    .size(52.dp),
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar ubicación")
            }
        }

        // ── Overlay de bloqueo GPS / Permisos ─────────────────────────────────
        AnimatedVisibility(
            visible = uiState.step != MapFlowStep.MAP_OK && uiState.step != MapFlowStep.CHECKING,
            enter   = fadeIn(tween(300)),
            exit    = fadeOut(tween(200)),
        ) {
            when (uiState.step) {
                MapFlowStep.GPS_REQUIRED -> MapOverlay(
                    icon        = Icons.Default.LocationOff,
                    title       = "GPS desactivado",
                    message     = "El GPS está apagado. Actívalo para usar el mapa y capturar coordenadas.",
                    buttonLabel = "Activar GPS",
                    onAction    = state.onRequestGps,
                )
                MapFlowStep.BLOCKED_SETTINGS -> MapOverlay(
                    icon        = Icons.Default.LocationDisabled,
                    title       = "Permiso de ubicación requerido",
                    message     = "Denegaste los permisos de ubicación. Ve a Ajustes para habilitarlos.",
                    buttonLabel = "Ir a Ajustes",
                    onAction    = { openAppSettings(context) },
                    isError     = true,
                )
                else -> Unit
            }
        }

        // ── Banner modo avión (no bloqueante) ─────────────────────────────────
        AnimatedVisibility(
            visible  = uiState.isAirplaneMode,
            enter    = slideInVertically { -it } + fadeIn(),
            exit     = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            AirplaneBanner(onGoToSettings = state.onGoToSettingsAirplane)
        }

        // ── Loading verificación inicial ──────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.step == MapFlowStep.CHECKING,
            enter   = fadeIn(),
            exit    = fadeOut(tween(200)),
        ) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Verificando ubicación...", color = Color.White)
                }
            }
        }

        // ── Contenido extra del caller (paneles, markers, etc.) ───────────────
        overlay()
    }

    // ── Diálogos de flujo de hardware ─────────────────────────────────────────
    // Fuera del Box para no quedar bajo ningún overlay
    if (uiState.showRationaleDialog) {
        RationaleDialog(
            canShowNativeDialog = state.canShowNativePermissionDialog,
            onAllowNative       = state.onAllowPermission,
            onGoToSettings      = state.onGoToSettingsPermission,
            onDeny              = state.onDenyPermission,
        )
    }

    if (uiState.showAirplaneDialog && !uiState.rpShowAirplaneDialog) {
        AirplaneDialog(
            onGoToSettings = state.onGoToSettingsAirplane,
            onContinue     = state.onDismissAirplane,
        )
    }

    if (uiState.showTimeoutDialog) {
        TimeoutDialog(
            onRetry        = state.onRetryTimeout,
            onAcceptManual = state.onAcceptManualTimeout,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MapScreen — captura automática por GPS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    showCoordenadas: Boolean = true,
    focusOnCurrentLocation: Boolean = true,
    onLocationAccepted: ((LocationData) -> Unit)? = null,
) {
    val state = rememberMapState(
        viewModel             = viewModel,
        calculateLocation     = true,
        focusOnCurrentLocation = focusOnCurrentLocation,
    )

    GoogleMapBase(
        isAuto                   = true,
        state                    = state,
        modifier                 = Modifier.fillMaxSize(),
        showCenterButton         = state.uiState.locationData.tieneCoordenadasValidas,
        centerButtonBottomPadding = if (showCoordenadas) 230.dp else 16.dp,
    ) {
        // Panel de georeferenciación automática
        if (showCoordenadas && state.uiState.step == MapFlowStep.MAP_OK) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            ) {
                CalculatedDataPanel(
                    locationData     = state.uiState.locationData,
                    isCalculating    = state.uiState.isCalculatingLocation,
                    remainingSeconds = state.uiState.remainingSeconds,
                    onRecalculate    = state.onRecalculate,
                    onAccept         = { data -> onLocationAccepted?.invoke(data) },
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ManualMarkerMap — captura manual tocando el mapa
// Reutiliza GoogleMapBase; el GPS sigue activo solo para centrar cámara
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ManualMarkerMap(
    viewModel: MapViewModel = viewModel(),
    focusOnCurrentLocation: Boolean = true,
    onLocationAccepted: ((LocationData) -> Unit)? = null,
) {
    val state = rememberMapState(
        viewModel              = viewModel,
        calculateLocation      = false,   // GPS activo pero sin georeferenciación
        focusOnCurrentLocation = focusOnCurrentLocation,
    )

    val scope = rememberCoroutineScope() // CORRECCIÓN: Necesario para animar la cámar
    var marker by remember { mutableStateOf<LatLng?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    fun onMapTap(latLng: LatLng) {
        marker    = latLng
        isEditing = false
    }

    GoogleMapBase(
        isAuto           = false,
        state            = state,
        modifier         = Modifier.fillMaxSize(),
        showCenterButton = state.uiState.locationData.tieneCoordenadasValidas,
        centerButtonBottomPadding = if (marker != null) 230.dp else 16.dp,
        onMapClick       = { latLng -> marker = latLng },
        mapContent                = {
            marker?.let { pos ->
                // 1. Creamos el estado inicial del marcador una sola vez
                val markerState = rememberMarkerState(position = pos)

                // 2. SOLUCIÓN REAL: Cada vez que 'marker' cambie (por toque o edición manual),
                // forzamos al estado interno del mapa a moverse a la nueva posición.
                LaunchedEffect(pos) {
                    markerState.position = pos
                }

                Marker(
                    state     = markerState,
                    title     = "Ubicación seleccionada",
                    snippet   = "${"%.6f".format(pos.latitude)}, ${"%.6f".format(pos.longitude)}",
                    icon      = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                )
            }
        },
    ) {
//        // Marker en el mapa
//        marker?.let { pos ->
//            // Nota: el contenido @Composable dentro de overlay() corre en un
//            // GoogleMapScope si lo necesitas para Markers nativos del mapa.
//            // Como overlay() es BoxScope, el Marker va dentro del GoogleMap
//            // directamente — ver nota al pie.
//        }

        // Instrucción flotante mientras no hay marker
        AnimatedVisibility(
            visible  = marker == null && state.uiState.step == MapFlowStep.MAP_OK,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.70f),
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        "Toca el mapa para marcar la ubicación",
                        color      = Color.White,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

//        // Panel de confirmación manual
//        AnimatedVisibility(
//            visible  = marker != null,
//            enter    = slideInVertically { it } + fadeIn(tween(200)),
//            exit     = slideOutVertically { it } + fadeOut(tween(150)),
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(16.dp),
//        ) {
//            marker?.let { pos ->
//                ManualLocationPanel(
//                    pos        = pos,
//                    onClear    = { marker = null },
//                    onAccepted = { latLng ->
//                        onLocationAccepted?.invoke(
//                            LocationData(
//                                latitude            = latLng.latitude,
//                                longitude           = latLng.longitude,
//                                altitude            = 0.0,
//                                accuracy            = 0f,
//                                quality             = PrecisionQuality.DESCONOCIDA,
//                                calculationFinished = true,
//                            )
//                        )
//                    },
//                )
//            }
//        }

        // Panel inferior — lectura o edición
        AnimatedVisibility(
            visible  = marker != null || isEditing,
            enter    = slideInVertically { it } + fadeIn(tween(200)),
            exit     = slideOutVertically { it } + fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) {
            ManualLocationPanel(
                pos       = marker,
                isEditing = isEditing,
                onEdit    = { isEditing = true },
                onConfirmEdit = { newLatLng ->
                    marker    = newLatLng
                    isEditing = false
                    // CORRECCIÓN: Mueve la cámara hacia las coordenadas que el usuario digitó
                    scope.launch {
                        state.cameraPositionState.animate(
                            CameraUpdateFactory.newLatLng(newLatLng)
                        )
                    }
                },
                onCancelEdit = { isEditing = false },
                onClear   = {
                    marker    = null
                    isEditing = false
                },
                onAccepted = { latLng ->
                    onLocationAccepted?.invoke(
                        LocationData(
                            latitude            = latLng.latitude,
                            longitude           = latLng.longitude,
                            altitude            = 0.0,
                            accuracy            = 0f,
                            quality             = PrecisionQuality.DESCONOCIDA,
                            calculationFinished = true,
                        )
                    )
                },
            )
        }

        AnimatedVisibility(
            visible  = marker == null && state.uiState.step == MapFlowStep.MAP_OK,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.Black.copy(alpha = 0.70f)) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Toca el mapa para marcar la ubicación", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        AnimatedVisibility(
            visible  = marker != null || isEditing,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) {
            ManualLocationPanel(
                pos       = marker,
                isEditing = isEditing,
                onEdit    = { isEditing = true },
                onConfirmEdit = { newLatLng ->
                    marker    = newLatLng
                    isEditing = false
                    scope.launch {
                        state.cameraPositionState.animate(CameraUpdateFactory.newLatLng(newLatLng))
                    }
                },
                onCancelEdit = { isEditing = false },
                onClear   = { marker = null; isEditing = false },
                onAccepted = { latLng ->
                    onLocationAccepted?.invoke(
                        LocationData(
                            latitude            = latLng.latitude,
                            longitude           = latLng.longitude,
                            altitude            = 0.0,
                            accuracy            = 0f,
                            quality             = PrecisionQuality.DESCONOCIDA,
                            calculationFinished = true,
                        )
                    )
                },
            )
        }
    }
}

// Nota: los Markers nativos de Google Maps Compose deben ir dentro del
// bloque content { } del GoogleMap, no en un Box superpuesto.
// Para soportar markers en el overlay necesitarías cambiar la firma de
// GoogleMapBase a recibir también un content: @Composable GoogleMapScope.() -> Unit
// Si lo necesitas avísame y lo extendemos.

// ─────────────────────────────────────────────────────────────────────────────
// MapScreenForSurvey — punto de entrada con toggle AUTO / MANUAL
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenForSurvey(
    title       : String      = "Capturar ubicación",
    initialMode : MapMode     = MapMode.AUTO,
    allowManual : Boolean     = false,
    onAccepted  : (LocationData) -> Unit,
    onDismiss   : () -> Unit,
    viewModel   : MapViewModel = viewModel(),
) {
    var mode by remember { mutableStateOf(initialMode) }

    // Al volver a AUTO, reseteamos la ubicación calculada para que
    // el usuario pueda georeferenciarse de nuevo si quiere
    LaunchedEffect(mode) {
        if (mode == MapMode.AUTO) viewModel.recalculateLocation()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cerrar")
                    }
                },
                actions = {
                    if (allowManual) {
                        ModeToggle(
                            current  = mode,
                            onToggle = {
                                mode = if (mode == MapMode.AUTO) MapMode.MANUAL else MapMode.AUTO
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState  = mode,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label        = "map_mode_transition",
            ) { currentMode ->
                when (currentMode) {

                    // El ViewModel es compartido — el GPS ya corre, solo
                    // cambia si georeferencia o solo centra cámara
                    MapMode.AUTO -> MapScreen(
                        viewModel          = viewModel,
                        showCoordenadas    = true,
                        onLocationAccepted = onAccepted,
                    )

                    MapMode.MANUAL -> ManualMarkerMap(
                        viewModel          = viewModel,
                        onLocationAccepted = onAccepted,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toggle AUTO / MANUAL en la TopBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ModeToggle(current: MapMode, onToggle: () -> Unit) {
    val isManual = current == MapMode.MANUAL
    Surface(
        onClick  = onToggle,
        shape    = RoundedCornerShape(20.dp),
        color    = if (isManual) MaterialTheme.colorScheme.secondaryContainer
        else          MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(end = 8.dp),
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector        = if (isManual) Icons.Default.TouchApp else Icons.Default.GpsFixed,
                contentDescription = null,
                modifier           = Modifier.size(14.dp),
                tint               = if (isManual) MaterialTheme.colorScheme.onSecondaryContainer
                else          MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text       = if (isManual) "Manual" else "Auto GPS",
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (isManual) MaterialTheme.colorScheme.onSecondaryContainer
                else          MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Panel de confirmación de ubicación manual
// ─────────────────────────────────────────────────────────────────────────────

//@Composable
//private fun ManualLocationPanel(
//    pos       : LatLng,
//    onClear   : () -> Unit,
//    onAccepted: (LatLng) -> Unit,
//) {
//    Card(
//        modifier  = Modifier.fillMaxWidth(),
//        shape     = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(8.dp),
//        colors    = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
//        ),
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            Row(
//                verticalAlignment     = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(6.dp),
//            ) {
//                Icon(
//                    Icons.Default.LocationOn,
//                    contentDescription = null,
//                    tint     = Color(0xFF1565C0),
//                    modifier = Modifier.size(20.dp),
//                )
//                Text(
//                    "Ubicación seleccionada",
//                    style      = MaterialTheme.typography.titleSmall,
//                    fontWeight = FontWeight.Bold,
//                )
//            }
//
//            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
//
//            Row(
//                Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//            ) {
//                ManualCoordCell("Latitud",  "%.6f".format(pos.latitude),  Modifier.weight(1f))
//                ManualCoordCell("Longitud", "%.6f".format(pos.longitude), Modifier.weight(1f))
//            }
//
//            Spacer(Modifier.height(4.dp))
//
//            Row(
//                verticalAlignment     = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
//                modifier              = Modifier.padding(top = 2.dp),
//            ) {
//                Icon(
//                    Icons.Default.Info,
//                    contentDescription = null,
//                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
//                    modifier = Modifier.size(12.dp),
//                )
//                Text(
//                    "Ubicación indicada manualmente — sin altitud ni precisión GPS.",
//                    fontSize = 10.sp,
//                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
//                )
//            }
//
//            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
//
//            Row(
//                modifier              = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(10.dp),
//            ) {
//                OutlinedButton(
//                    onClick  = onClear,
//                    modifier = Modifier.weight(1f),
//                    shape    = RoundedCornerShape(10.dp),
//                ) {
//                    Icon(Icons.Default.Close, null, Modifier.size(16.dp))
//                    Spacer(Modifier.width(4.dp))
//                    Text("Cambiar")
//                }
//                Button(
//                    onClick  = { onAccepted(pos) },
//                    modifier = Modifier.weight(1f),
//                    shape    = RoundedCornerShape(10.dp),
//                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
//                ) {
//                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
//                    Spacer(Modifier.width(4.dp))
//                    Text("Aceptar", fontWeight = FontWeight.Bold)
//                }
//            }
//        }
//    }
//}

// ─────────────────────────────────────────────────────────────────────────────
// ManualLocationPanel — panel inferior con modo lectura y edición
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ManualLocationPanel(
    pos          : LatLng?,
    isEditing    : Boolean,
    onEdit       : () -> Unit,
    onConfirmEdit: (LatLng) -> Unit,
    onCancelEdit : () -> Unit,
    onClear      : () -> Unit,
    onAccepted   : (LatLng) -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Encabezado ────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint     = Color(0xFF1565C0),
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        if (isEditing) "Ingresar coordenadas" else "Ubicación seleccionada",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Botón editar — solo en modo lectura con marker existente
                if (!isEditing && pos != null) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar coordenadas",
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // ── Contenido: lectura o edición ──────────────────────────────────
            AnimatedContent(
                targetState   = isEditing,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
                label         = "panel_mode",
            ) { editing ->
                if (editing) {
                    CoordEditFields(
                        initialLat   = pos?.latitude,
                        initialLng   = pos?.longitude,
                        onConfirm    = onConfirmEdit,
                        onCancel     = onCancelEdit,
                    )
                } else {
                    CoordReadView(
                        pos      = pos,
                        onClear  = onClear,
                        onAccept = onAccepted,
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// CoordReadView — coords en modo lectura + acciones Cambiar / Aceptar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CoordReadView(
    pos     : LatLng?,
    onClear : () -> Unit,
    onAccept: (LatLng) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        if (pos != null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CoordDisplayCell("Latitud",  "%.6f".format(pos.latitude),  Modifier.weight(1f))
                CoordDisplayCell("Longitud", "%.6f".format(pos.longitude), Modifier.weight(1f))
            }

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    "Sin altitud ni precisión GPS.",
                    fontSize = 10.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider()

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick  = onClear,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
            ) {
                Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cambiar")
            }
            Button(
                onClick  = { pos?.let(onAccept) },
                enabled  = pos != null,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            ) {
                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Aceptar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CoordEditFields — campos de texto + Confirmar / Cancelar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CoordEditFields(
    initialLat: Double?,
    initialLng: Double?,
    onConfirm : (LatLng) -> Unit,
    onCancel  : () -> Unit,
) {
    var latText  by remember { mutableStateOf(initialLat?.let { "%.6f".format(it) } ?: "") }
    var lngText  by remember { mutableStateOf(initialLng?.let { "%.6f".format(it) } ?: "") }
    var latError by remember { mutableStateOf(false) }
    var lngError by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    fun validate(): Boolean {
        val lat = latText.toDoubleOrNull()
        val lng = lngText.toDoubleOrNull()
        latError = lat == null || lat !in -90.0..90.0
        lngError = lng == null || lng !in -180.0..180.0
        return !latError && !lngError
    }

    fun confirm() {
        if (validate()) {
            focusManager.clearFocus()
            onConfirm(LatLng(latText.toDouble(), lngText.toDouble()))
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        OutlinedTextField(
            value         = latText,
            onValueChange = { latText = it; latError = false },
            label         = { Text("Latitud") },
            placeholder   = { Text("ej. -12.066400") },
            isError       = latError,
            supportingText = if (latError) {{ Text("Valor entre -90 y 90") }} else null,
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction    = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value         = lngText,
            onValueChange = { lngText = it; lngError = false },
            label         = { Text("Longitud") },
            placeholder   = { Text("ej. -77.042800") },
            isError       = lngError,
            supportingText = if (lngError) {{ Text("Valor entre -180 y 180") }} else null,
            singleLine    = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { confirm() }
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick  = onCancel,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
            ) {
                Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cancelar")
            }
            Button(
                onClick  = ::confirm,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            ) {
                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Confirmar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ManualCoordCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            fontSize   = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers de display
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CoordDisplayCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Medium,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            fontSize   = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MapViewer — solo lectura
//
// Muestra un marker fijo en la coordenada dada.
// Sin GPS, sin permisos, sin cálculo — solo visualización.
// Anima la cámara al cargar.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MapViewer(
    locationData: LocationData,
    modifier: Modifier = Modifier,
    zoom: Float = 16f,
) {
    val pos = LatLng(locationData.latitude, locationData.longitude)
    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pos, zoom - 4f) // arranca un poco más lejos
    }

    // Animación de entrada al cargar
    LaunchedEffect(Unit) {
        scope.launch {
            runCatching {
                cameraPositionState.animate(
                    update    = CameraUpdateFactory.newLatLngZoom(pos, zoom),
                    durationMs = 900,
                )
            }
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier            = Modifier.fillMaxSize(),
            properties          = MapProperties(isMyLocationEnabled = false),
            uiSettings          = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled     = true,
                scrollGesturesEnabled   = true,
                zoomGesturesEnabled     = true,
                tiltGesturesEnabled     = false,
                rotationGesturesEnabled = false,
            ),
            cameraPositionState = cameraPositionState,
        ) {
            Marker(
                state   = MarkerState(position = pos),
                title   = "Ubicación capturada",
                snippet = "${"%.6f".format(pos.latitude)}, ${"%.6f".format(pos.longitude)}",
                icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
            )
        }

        // Badge de coords en la esquina superior izquierda
        Surface(
            shape    = RoundedCornerShape(10.dp),
            color    = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                CoordBadgeRow("Lat", "%.6f".format(locationData.latitude))
                CoordBadgeRow("Lng", "%.6f".format(locationData.longitude))
                if (locationData.altitude != 0.0) {
                    CoordBadgeRow("Alt", "${"%.1f".format(locationData.altitude)} m")
                }
            }
        }
    }
}

@Composable
private fun CoordBadgeRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
        Text(value, fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// MapViewerDialog — dialog de solo lectura para verificar una coordenada
// Úsalo desde GpsQuestion con el botón "Ver en mapa"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MapViewerDialog(
    locationData: LocationData,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            MapViewer(
                locationData = locationData,
                modifier     = Modifier.fillMaxSize(),
            )

            // Botón cerrar superpuesto arriba a la derecha
            Surface(
                onClick  = onDismiss,
                shape    = RoundedCornerShape(50),
                color    = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp),
                shadowElevation = 4.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint     = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
