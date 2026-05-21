package com.minedu.gob.pe.enaprescalidad.ui.components.maps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minedu.gob.pe.enaprescalidad.utils.isGpsEnabled
import com.google.maps.android.compose.*
import com.minedu.gob.pe.enaprescalidad.utils.isAirplaneMode
import com.minedu.gob.pe.enaprescalidad.utils.openAirplaneSettings
import com.minedu.gob.pe.enaprescalidad.utils.openAppSettings
import com.minedu.gob.pe.enaprescalidad.utils.requestGpsEnable
import android.Manifest
import android.annotation.SuppressLint
import android.location.LocationManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng

import androidx.compose.runtime.Composable
import com.google.maps.android.compose.MapUiSettings

// ── Punto de entrada público ──────────────────────────────────────────────────

@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    showCoordenadas: Boolean = true,
    calculateLocation: Boolean = true,
    focusOnCurrentLocation: Boolean = true,
    onLocationAccepted: ((LocationData) -> Unit)? = null,
) {
    val state = rememberMapState(
        viewModel             = viewModel,
        calculateLocation     = calculateLocation,
        focusOnCurrentLocation = focusOnCurrentLocation,
        onLocationAccepted    = onLocationAccepted,
    )

    MapContent(
        state             = state,
        showCoordenadas   = showCoordenadas,
        calculateLocation = calculateLocation,
    )
}

@Composable
private fun MapContent(
    state: MapState,
    showCoordenadas: Boolean,
    calculateLocation: Boolean,
) {
    val context  = LocalContext.current
    val uiState  = state.uiState

    Box(Modifier.fillMaxSize()) {

        MapBase(
            permisosOtorgados   = state.permisosOtorgados,
            cameraPositionState = state.cameraPositionState,
        )

        MyLocationButton(
            visible           = uiState.step == MapFlowStep.MAP_OK
                    && uiState.locationData.tieneCoordenadasValidas,
            calculateLocation = calculateLocation,
            onClick           = state.onCenterCamera,
        )

        BlockingOverlay(
            step         = uiState.step,
            onRequestGps = state.onRequestGps,
            onGoToSettings = { openAppSettings(context) },
        )

        AirplaneBannerAnimated(
            visible       = uiState.isAirplaneMode,
            onGoToSettings = state.onGoToSettingsAirplane,
        )

        if (calculateLocation && uiState.step == MapFlowStep.MAP_OK && showCoordenadas) {
            LocationDataPanel(
                uiState            = uiState,
                onRecalculate      = state.onRecalculate,
                onLocationAccepted = state.onLocationAccepted,
            )
        }

        CheckingOverlay(visible = uiState.step == MapFlowStep.CHECKING)
    }

    MapDialogs(state = state)
}

@Composable
private fun MapBase(
    permisosOtorgados: Boolean,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
) {
    GoogleMap(
        modifier            = Modifier.fillMaxSize(),
        properties          = MapProperties(isMyLocationEnabled = permisosOtorgados),
        uiSettings          = MapUiSettings(myLocationButtonEnabled = false),
        cameraPositionState = cameraPositionState,
    )
}

@Composable
private fun BoxScope.MyLocationButton(
    visible: Boolean,
    calculateLocation: Boolean,
    onClick: () -> Unit,
) {
    if (!visible) return

    FloatingActionButton(
        onClick        = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier       = Modifier
            .align(Alignment.CenterEnd)
            .padding(
                start  = 16.dp,
                bottom = if (calculateLocation) 230.dp else 16.dp,
            )
            .size(52.dp),
    ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Centrar ubicación")
    }
}

@Composable
private fun BlockingOverlay(
    step: MapFlowStep,
    onRequestGps: () -> Unit,
    onGoToSettings: () -> Unit,
) {
    AnimatedVisibility(
        visible = step != MapFlowStep.MAP_OK && step != MapFlowStep.CHECKING,
        enter   = fadeIn(tween(300)),
        exit    = fadeOut(tween(200)),
    ) {
        when (step) {
            MapFlowStep.GPS_REQUIRED -> MapOverlay(
                icon        = Icons.Default.LocationOff,
                title       = "GPS desactivado",
                message     = "El GPS está apagado. Actívalo para usar el mapa y capturar coordenadas.",
                buttonLabel = "Activar GPS",
                onAction    = onRequestGps,
            )
            MapFlowStep.BLOCKED_SETTINGS -> MapOverlay(
                icon        = Icons.Default.LocationDisabled,
                title       = "Permiso de ubicación requerido",
                message     = "Denegaste los permisos de ubicación. Ve a Ajustes para habilitarlos.",
                buttonLabel = "Ir a Ajustes",
                onAction    = onGoToSettings,
                isError     = true,
            )
            else -> Unit
        }
    }
}

@Composable
private fun BoxScope.AirplaneBannerAnimated(
    visible: Boolean,
    onGoToSettings: () -> Unit,
) {
    AnimatedVisibility(
        visible  = visible,
        enter    = slideInVertically { -it } + fadeIn(),
        exit     = slideOutVertically { -it } + fadeOut(),
        modifier = Modifier.align(Alignment.TopCenter),
    ) {
        AirplaneBanner(onGoToSettings = onGoToSettings)
    }
}

@Composable
private fun BoxScope.LocationDataPanel(
    uiState: MapUiState,
    onRecalculate: () -> Unit,
    onLocationAccepted: (LocationData) -> Unit,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
    ) {
        CalculatedDataPanel(
            locationData     = uiState.locationData,
            isCalculating    = uiState.isCalculatingLocation,
            remainingSeconds = uiState.remainingSeconds,
            onRecalculate    = onRecalculate,
            onAccept         = onLocationAccepted,
        )
    }
}

@Composable
private fun CheckingOverlay(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(),
        exit    = fadeOut(tween(200)),
    ) {
        Box(
            modifier          = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment  = Alignment.Center,
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
}

@Composable
private fun MapDialogs(state: MapState) {
    val uiState = state.uiState

    if (uiState.showRationaleDialog) {
        RationaleDialog(
            canShowNativeDialog = true,
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
            onRetry       = state.onRetryTimeout,
            onAcceptManual = state.onAcceptManualTimeout,
        )
    }
}