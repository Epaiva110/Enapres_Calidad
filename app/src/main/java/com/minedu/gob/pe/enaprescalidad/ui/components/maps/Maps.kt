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

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    showCoordenadas: Boolean = true,
    calculateLocation: Boolean = true,
    focusOnCurrentLocation: Boolean = true,
    onLocationAccepted: ((LocationData) -> Unit)? = null,  // callback para uso externo / encuesta
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    val cameraPositionState = rememberCameraPositionState()

    fun permisosRealmenteOtorgados(): Boolean =
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ).all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

    fun evaluate() {
        val tienePermisos = permisosRealmenteOtorgados()
        val gpsOn = isGpsEnabled(context)

        when {
            !tienePermisos -> {
                if (permissionsState.shouldShowRationale) {
                    viewModel.showRationale(true)
                } else {
                    viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
                }
            }

            !gpsOn -> {
               viewModel.setStep(MapFlowStep.GPS_REQUIRED)

            }

            else -> {
                viewModel.setStep(MapFlowStep.MAP_OK)
            }
        }
    }

    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        viewModel.setGpsDialogActive(false)
        evaluate()
    }

    val requestGps: () -> Unit = {
        if (!uiState.isGpsDialogActive) {
            viewModel.setGpsDialogActive(true)
            requestGpsEnable(context) { senderRequest ->
                gpsLauncher.launch(senderRequest)
            }
        }
    }

    fun centrarCamaraEnUbicacion() {
        if (uiState.locationData.tieneCoordenadasValidas) {
            scope.launch {
                try {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(uiState.locationData.latitude, uiState.locationData.longitude),
                            16f
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // ── Ciclos de Carga y Eventos Efectivos ───────────────────────────────────
    LaunchedEffect(Unit) {
        val gpsOn = isGpsEnabled(context)
        val airplaneOn = isAirplaneMode(context)

        viewModel.onHardwareChanged(
            gpsOn = gpsOn,
            airplaneOn = airplaneOn,
            calculateLocation = calculateLocation,
            isInitialCheck = true
        )
        if (permisosRealmenteOtorgados()) {
            evaluate()
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(permissionsState.allPermissionsGranted, permissionsState.shouldShowRationale) {
        evaluate()
    }

    LaunchedEffect(uiState.step) {
        if (uiState.step == MapFlowStep.MAP_OK
            && calculateLocation
            && !uiState.isCalculatingLocation
            && !uiState.locationData.calculationFinished
        ) {
            viewModel.startLocationCalculation()
        }
    }

    var hasFocused by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(uiState.locationData.tieneCoordenadasValidas) {
        if (focusOnCurrentLocation && uiState.locationData.tieneCoordenadasValidas && !hasFocused) {
            delay(500L)
            centrarCamaraEnUbicacion()
            hasFocused = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                evaluate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val gpsOn = isGpsEnabled(ctx)
                val airplaneOn = isAirplaneMode(ctx)

                viewModel.onHardwareChanged(
                    gpsOn = gpsOn,
                    airplaneOn = airplaneOn,
                    calculateLocation = calculateLocation,
                )

                evaluate()

                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION
                    && !gpsOn
                    && permisosRealmenteOtorgados()
                    && !viewModel.uiState.value.isGpsDialogActive
                ) {
                    requestGps()
                }
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ── Estructura de UI ──────────────────────────────────────────────────────
    Box(Modifier.fillMaxSize()) {

        // Mapa base siempre activo
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = MapProperties(
                isMyLocationEnabled = permisosRealmenteOtorgados(),
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false // Desactivado para implementar nuestro botón abajo a la izquierda
            ),
            cameraPositionState = cameraPositionState,
        )

        // BOTÓN PERSONALIZADO DE MI UBICACIÓN (Abajo a la izquierda)
        if (uiState.step == MapFlowStep.MAP_OK && uiState.locationData.tieneCoordenadasValidas) {
            FloatingActionButton(
                onClick = { centrarCamaraEnUbicacion() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    // Offset condicional para que suba si el panel de datos está visible
                    .padding(
                        start = 16.dp,
                        bottom = if (calculateLocation) 230.dp else 16.dp
                    )
                    .size(52.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar ubicación")
            }
        }

        // Overlays de bloqueo (GPS / Permisos)
        AnimatedVisibility(
            visible = uiState.step != MapFlowStep.MAP_OK && uiState.step != MapFlowStep.CHECKING,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(200)),
        ) {
            when (uiState.step) {
                MapFlowStep.GPS_REQUIRED -> MapOverlay(
                    icon = Icons.Default.LocationOff,
                    title = "GPS desactivado",
                    message = "El GPS está apagado. Actívalo para usar el mapa y capturar coordenadas.",
                    buttonLabel = "Activar GPS",
                    onAction = requestGps,
                )

                MapFlowStep.BLOCKED_SETTINGS -> MapOverlay(
                    icon = Icons.Default.LocationDisabled,
                    title = "Permiso de ubicación requerido",
                    message = "Denegaste los permisos de ubicación. Ve a Ajustes para habilitarlos.",
                    buttonLabel = "Ir a Ajustes",
                    onAction = { openAppSettings(context) },
                    isError = true,
                )
                else -> Unit
            }
        }

        // Banner de Modo Avión (Superior, no bloqueante)
        AnimatedVisibility(
            visible = uiState.isAirplaneMode,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            AirplaneBanner(onGoToSettings = { openAirplaneSettings(context) })
        }

        // Panel de datos calculados
        if (calculateLocation && uiState.step == MapFlowStep.MAP_OK && showCoordenadas) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                CalculatedDataPanel(
                    locationData = uiState.locationData,
                    isCalculating = uiState.isCalculatingLocation,
                    remainingSeconds = uiState.remainingSeconds,
                    onRecalculate = { viewModel.recalculateLocation() },
                    onAccept = { datosEnviados ->
                        onLocationAccepted?.invoke(datosEnviados)
                    }
                )
            }
        }

        // Loading de verificación inicial
        AnimatedVisibility(
            visible = uiState.step == MapFlowStep.CHECKING,
            enter = fadeIn(),
            exit = fadeOut(tween(200)),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Verificando ubicación...", color = Color.White)
                }
            }
        }
    }

    // ── Diálogos de Gestión Flujo Hardware ────────────────────────────────────
    if (uiState.showRationaleDialog) {
        RationaleDialog(
            canShowNativeDialog = permissionsState.shouldShowRationale,
            onAllowNative = {
                viewModel.showRationale(false)
                permissionsState.launchMultiplePermissionRequest()
            },
            onGoToSettings = {
                viewModel.showRationale(false)
                viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
                openAppSettings(context)
            },
            onDeny = {
                viewModel.showRationale(false)
                viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
            },
        )
    }

    if (uiState.showAirplaneDialog && !uiState.rpShowAirplaneDialog) {
        AirplaneDialog(
            onGoToSettings = {
                viewModel.dismissAirplaneDialog()
                openAirplaneSettings(context)
            },
            onContinue = { viewModel.dismissAirplaneDialog() },
        )
    }

    if (uiState.showTimeoutDialog) {
        TimeoutDialog(
            onRetry = { viewModel.onRetryAfterTimeout() },
            onAcceptManual = { viewModel.onAcceptManualAfterTimeout() },
        )
    }
}








