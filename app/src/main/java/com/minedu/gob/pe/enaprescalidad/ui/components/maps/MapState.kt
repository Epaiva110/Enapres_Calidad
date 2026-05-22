package com.minedu.gob.pe.enaprescalidad.ui.components.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.minedu.gob.pe.enaprescalidad.utils.isAirplaneMode
import com.minedu.gob.pe.enaprescalidad.utils.isGpsEnabled
import com.minedu.gob.pe.enaprescalidad.utils.openAirplaneSettings
import com.minedu.gob.pe.enaprescalidad.utils.openAppSettings
import com.minedu.gob.pe.enaprescalidad.utils.requestGpsEnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Contrato público — lo consumen GoogleMapBase, MapScreen y ManualMarkerMap
// ─────────────────────────────────────────────────────────────────────────────

@Stable
class MapState(
    // Estado del ViewModel
    val uiState: MapUiState,
    // Estado de la cámara compartido entre modos
    val cameraPositionState: CameraPositionState,
    // Permisos
    val permisosOtorgados: Boolean,
    val canShowNativePermissionDialog: Boolean,
    // Acciones de hardware (GPS / avión / permisos)
    val onRequestGps: () -> Unit,
    val onCenterCamera: () -> Unit,
    val onAllowPermission: () -> Unit,
    val onGoToSettingsPermission: () -> Unit,
    val onDenyPermission: () -> Unit,
    val onGoToSettingsAirplane: () -> Unit,
    val onDismissAirplane: () -> Unit,
    // Acciones de georeferenciación automática
    val onRecalculate: () -> Unit,
    val onRetryTimeout: () -> Unit,
    val onAcceptManualTimeout: () -> Unit,
)

// ─────────────────────────────────────────────────────────────────────────────
// Factory
// El parámetro [calculateLocation] controla si el GPS activo georeferencia
// o solo se usa para centrar la cámara (modo manual).
// ─────────────────────────────────────────────────────────────────────────────

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberMapState(
    viewModel: MapViewModel = viewModel(),
    calculateLocation: Boolean,
    focusOnCurrentLocation: Boolean = true,
): MapState {

    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState        by viewModel.uiState.collectAsState()
    val scope          = rememberCoroutineScope()

    val permissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    val cameraPositionState = rememberCameraPositionState()

    // ── Helpers internos ──────────────────────────────────────────────────────

    fun permisosRealmenteOtorgados(): Boolean =
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ).all {
            ContextCompat.checkSelfPermission(context, it) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        }

    fun evaluate() {
        val tienePermisos = permisosRealmenteOtorgados()
        val gpsOn         = isGpsEnabled(context)

        Log.d("MapState", "evaluate gpsOn=$gpsOn permisos=$tienePermisos")

        when {
            !tienePermisos -> {
                if (permissionsState.shouldShowRationale) viewModel.showRationale(true)
                else viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
            }
            !gpsOn -> viewModel.setStep(MapFlowStep.GPS_REQUIRED)
            else   -> viewModel.setStep(MapFlowStep.MAP_OK)
        }
    }

    fun centerCamera() {
        if (uiState.locationData.tieneCoordenadasValidas) {
            scope.launch {
                runCatching {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                uiState.locationData.latitude,
                                uiState.locationData.longitude,
                            ),
                            16f,
                        )
                    )
                }
            }
        }
    }

    // ── Launcher del diálogo nativo de GPS ───────────────────────────────────
    // Solo se dispara desde el botón "Activar GPS", nunca automáticamente

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

    // ── Efectos ───────────────────────────────────────────────────────────────

    LaunchedEffect(Unit) {
        viewModel.setGpsDialogActive(false)
        viewModel.onHardwareChanged(
            gpsOn             = isGpsEnabled(context),
            airplaneOn        = isAirplaneMode(context),
            calculateLocation = calculateLocation,
            isInitialCheck    = true,
        )
        if (permisosRealmenteOtorgados()) evaluate()
        else permissionsState.launchMultiplePermissionRequest()
    }

    LaunchedEffect(permissionsState.allPermissionsGranted, permissionsState.shouldShowRationale) {
        evaluate()
    }

    // Solo arranca la georeferenciación si calculateLocation=true
    // En modo manual, el GPS sigue activo pero solo para centrar cámara
    LaunchedEffect(uiState.step) {
        if (uiState.step == MapFlowStep.MAP_OK
            && calculateLocation
            && !uiState.isCalculatingLocation
            && !uiState.locationData.calculationFinished
        ) {
            viewModel.startLocationCalculation()
        }
    }

    // Centra cámara cuando llega la primera coordenada (ambos modos)
    var hasFocused by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(uiState.locationData.tieneCoordenadasValidas) {
        if (focusOnCurrentLocation
            && uiState.locationData.tieneCoordenadasValidas
            && !hasFocused
        ) {
            delay(500L)
            centerCamera()
            hasFocused = true
        }
    }

    // ON_RESUME — cubre volver desde Ajustes del sistema
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) evaluate()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // BroadcastReceiver — cambios de GPS y modo avión en tiempo real
    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val gpsOn      = isGpsEnabled(ctx)
                val airplaneOn = isAirplaneMode(ctx)

                Log.i("MapState", "onReceive gpsOn=$gpsOn airplaneOn=$airplaneOn")

                if (gpsOn && viewModel.uiState.value.isGpsDialogActive) {
                    viewModel.setGpsDialogActive(false)
                }

                viewModel.onHardwareChanged(
                    gpsOn             = gpsOn,
                    airplaneOn        = airplaneOn,
                    calculateLocation = calculateLocation,
                )

                evaluate()
                // Sin requestGps() automático — solo desde el botón
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ── Construcción del estado público ──────────────────────────────────────

    return MapState(
        uiState                      = uiState,
        cameraPositionState          = cameraPositionState,
        permisosOtorgados            = permisosRealmenteOtorgados(),
        canShowNativePermissionDialog = permissionsState.shouldShowRationale,
        onRequestGps                 = requestGps,
        onCenterCamera               = ::centerCamera,
        onAllowPermission            = {
            viewModel.showRationale(false)
            permissionsState.launchMultiplePermissionRequest()
        },
        onGoToSettingsPermission     = {
            viewModel.showRationale(false)
            viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
            openAppSettings(context)
        },
        onDenyPermission             = {
            viewModel.showRationale(false)
            viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
        },
        onGoToSettingsAirplane       = {
            viewModel.dismissAirplaneDialog()
            openAirplaneSettings(context)
        },
        onDismissAirplane            = { viewModel.dismissAirplaneDialog() },
        onRecalculate                = { viewModel.recalculateLocation() },
        onRetryTimeout               = { viewModel.onRetryAfterTimeout() },
        onAcceptManualTimeout        = { viewModel.onAcceptManualAfterTimeout() },
    )
}