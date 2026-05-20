package com.minedu.gob.pe.enaprescalidad.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.google.android.gms.location.*
import com.google.maps.android.compose.*
import com.minedu.gob.pe.enaprescalidad.utils.isAirplaneMode
import com.minedu.gob.pe.enaprescalidad.utils.openAirplaneSettings
import com.minedu.gob.pe.enaprescalidad.utils.openAppSettings
import com.minedu.gob.pe.enaprescalidad.utils.requestGpsEnable
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.LocationManager
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat


// ─────────────────────────────────────────────────────────────────────────────
//  MODELOS
// ─────────────────────────────────────────────────────────────────────────────

enum class PrecisionQuality { EXCELENTE, REGULAR, DEFICIENTE, DESCONOCIDA }

// El mapa SIEMPRE se muestra. Los estados ahora son overlays, no bloqueos.
enum class MapFlowStep {
    CHECKING,          // Evaluando estado inicial
    MAP_OK,            // Todo correcto, sin ningún overlay
    GPS_REQUIRED,      // GPS apagado — overlay con botón activar
    BLOCKED_SETTINGS,  // Permisos denegados — overlay con botón ajustes
    // AIRPLANE_MODE ya NO bloquea el mapa — es solo un banner + diálogo
}

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val quality: PrecisionQuality = PrecisionQuality.DESCONOCIDA,
    val calculationFinished: Boolean = false,
) {
    val tieneCoordenadasValidas: Boolean get() = latitude != 0.0 || longitude != 0.0
}

data class MapUiState(
    val step: MapFlowStep = MapFlowStep.CHECKING,

    // Permisos
    val showRationaleDialog: Boolean = false,

    // GPS
    val isGpsDialogActive: Boolean = false,   // evita abrir el diálogo de GPS dos veces

    // Modo avión — no bloquea, solo advierte
    val isAirplaneMode: Boolean = false,
    val showAirplaneDialog: Boolean = false,   // diálogo inicial de advertencia

    // Localización
    val locationData: LocationData = LocationData(),
    val isCalculatingLocation: Boolean = false,
    val showTimeoutDialog: Boolean = false,
    val remainingSeconds: Int = 120 // <-- NUEVO: Contador de tiempo
)

// ─────────────────────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────────────────────

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)
    private var locationCallback: LocationCallback? = null
    private var locationJob: Job? = null

    init {
        // CORRECCIÓN 1: leer el estado real de modo avión al crear el ViewModel,
        // antes de que cualquier BroadcastReceiver se registre.
        val airplaneOn = isAirplaneMode(application)
        _uiState.update { it.copy(isAirplaneMode = airplaneOn) }
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    fun setStep(step: MapFlowStep) = _uiState.update { it.copy(step = step) }
    fun showRationale(show: Boolean) = _uiState.update { it.copy(showRationaleDialog = show) }
    fun setGpsDialogActive(active: Boolean) = _uiState.update { it.copy(isGpsDialogActive = active) }
    fun dismissTimeoutDialog() = _uiState.update { it.copy(showTimeoutDialog = false) }
    fun dismissAirplaneDialog() = _uiState.update { it.copy(showAirplaneDialog = false) }

    /** ── Inicialización desde la Screen ────────────────────────────────────────**/

    /**
     * Llamado en el LaunchedEffect(Unit) de la Screen.
     * Sincroniza el estado de modo avión con el hardware real,
     * incluso si el BroadcastReceiver aún no se ha registrado.
     */
    fun syncInitialHardwareState(context: android.content.Context):Boolean {
        val airplaneOn = isAirplaneMode(context)
        _uiState.update { it.copy(isAirplaneMode = airplaneOn) }
        return airplaneOn
        // Si arranca en modo avión, mostrar el banner pero no el diálogo
        // (el diálogo solo aparece cuando el usuario lo activa, no al entrar)
    }
    // ── Cambios de hardware ───────────────────────────────────────────────────

    /**
     * Llamado desde el BroadcastReceiver cuando cambia GPS o modo avión.
     * El mapa NUNCA se oculta por modo avión — solo se muestra un banner/diálogo.
     */
    fun onHardwareChanged(gpsOn: Boolean, airplaneOn: Boolean, calculateLocation: Boolean, isInitialCheck: Boolean = false) {
        val anteriorAirplane = _uiState.value.isAirplaneMode

        _uiState.update { it.copy(isAirplaneMode = airplaneOn) }

        when {
            // Modo avión acaba de activarse → mostrar diálogo de advertencia (no bloquear)
            airplaneOn && !anteriorAirplane -> {
                //cancelLocationJob()
                _uiState.update { it.copy(showAirplaneDialog = true) }
                // El step NO cambia — el mapa sigue visible
            }
            // Si entró en modo avión al inicializar, solo cancelamos el job sin diálogo
            //airplaneOn && isInitialCheck -> {
            airplaneOn && isInitialCheck -> {
                //cancelLocationJob()
                _uiState.update { it.copy(showAirplaneDialog = true) }
            }

            // Modo avión se desactivó → reevaluar GPS
            !airplaneOn && anteriorAirplane -> {
                _uiState.update { it.copy(showAirplaneDialog = false)}
                if (gpsOn) {
                    setStep(MapFlowStep.MAP_OK)
                    //if (calculateLocation) startLocationCalculation()
                } else {
                    setStep(MapFlowStep.GPS_REQUIRED)
                }
            }
            // GPS se apagó (sin modo avión) → cancelar cálculo
            //!gpsOn && !airplaneOn -> {
            !gpsOn -> {
                cancelLocationJob()
                setStep(MapFlowStep.GPS_REQUIRED)
            }
            // GPS volvió a encenderse → reanudar
            //gpsOn && !airplaneOn -> {
            gpsOn -> {
                if (_uiState.value.step != MapFlowStep.MAP_OK) {
                    setStep(MapFlowStep.MAP_OK)
                }
                if (calculateLocation && !_uiState.value.locationData.calculationFinished) {
                    startLocationCalculation()
                }
            }
        }
    }

    // ── Cálculo de localización ───────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    fun startLocationCalculation() {
        cancelLocationJob()
        _uiState.update { it.copy(isCalculatingLocation = true, showTimeoutDialog = false, remainingSeconds = 120) }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                // Evaluamos la calidad de la nueva coordenada entrante
                val newQuality = when {
                    loc.accuracy < 10f  -> PrecisionQuality.EXCELENTE
                    loc.accuracy <= 50f -> PrecisionQuality.REGULAR
                    else                -> PrecisionQuality.DEFICIENTE
                }
                _uiState.update { state ->
                    val currentData = state.locationData

                    val debeActualizar = !currentData.tieneCoordenadasValidas || loc.accuracy < currentData.accuracy

                    if (debeActualizar) {
                        state.copy(
                            locationData = LocationData(
                                latitude  = loc.latitude,
                                longitude = loc.longitude,
                                altitude  = loc.altitude,
                                accuracy  = loc.accuracy,
                                quality   = newQuality,
                            )
                        )
                    } else {
                        // Si la coordenada que llegó es más imprecisa que la que ya teníamos, la descartamos.
                        state
                    }
                }

                // Si en cualquier iteración logramos la precisión ideal (< 10 metros),
                // cerramos el cálculo de inmediato de manera exitosa para ahorrar batería.
                if (newQuality == PrecisionQuality.EXCELENTE) {
                    stopWithSuccess()
                }
            }
        }

        locationCallback = callback
        fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        locationJob = viewModelScope.launch {

            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L) // Espera 1 segundo
                _uiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }

            //delay(120_000L)
            val data = _uiState.value.locationData
            if (data.tieneCoordenadasValidas) {
                stopWithSuccess()
            } else {
                cancelLocationJob()
                _uiState.update { it.copy(showTimeoutDialog = true) }
            }
        }
    }

    fun onRetryAfterTimeout() {
        _uiState.update { it.copy(showTimeoutDialog = false) }
        startLocationCalculation()
    }

    fun onAcceptManualAfterTimeout() =
        _uiState.update { it.copy(showTimeoutDialog = false) }

    private fun stopWithSuccess() {
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        locationCallback = null
        locationJob?.cancel()
        locationJob = null
        _uiState.update { state ->
            state.copy(
                isCalculatingLocation = false,
                locationData = state.locationData.copy(calculationFinished = true),
            )
        }
    }

    fun cancelLocationJob() {
        locationCallback?.let { fusedClient.removeLocationUpdates(it) }
        locationCallback = null
        locationJob?.cancel()
        locationJob = null
        _uiState.update { it.copy(isCalculatingLocation = false) }
    }

    fun recalculateLocation() {
        // 1. Cancelamos cualquier proceso activo por seguridad
        cancelLocationJob()

        // 2. Limpiamos los datos de ubicación viejos para que la UI sepa que estamos buscando de nuevo
        _uiState.update { state ->
            state.copy(
                locationData = LocationData(), // Se reinicia a DESCONOCIDA y sin coordenadas
                isCalculatingLocation = true,
                remainingSeconds = 120 // Reiniciamos el contador al recalcular
            )
        }

        // 3. Volvemos a arrancar el flujo del GPS
        startLocationCalculation()
    }

    override fun onCleared() {
        super.onCleared()
        cancelLocationJob()
    }
}


@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(),
    calculateLocation: Boolean = true,
    focusOnCurrentLocation: Boolean = true,
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState        by viewModel.uiState.collectAsState()

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
            ContextCompat.checkSelfPermission(context, it) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        }

    fun evaluate() {
        val tienePermisos = permisosRealmenteOtorgados()
        val gpsOn         = isGpsEnabled(context)

        when {
            !tienePermisos -> {
                // CORRECCIÓN PROBLEMA 1: si revocó permisos, mostramos rationale o blocked
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

    // ── GPS launcher ──────────────────────────────────────────────────────────
    // CORRECCIÓN PROBLEMA 2: al cerrarse el diálogo de GPS (con cualquier
    // respuesta), marcamos isGpsDialogActive = false ANTES de evaluar,
    // así el receiver no vuelve a abrir otro diálogo.
    val gpsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        viewModel.setGpsDialogActive(false)
        evaluate()
    }

    // CORRECCIÓN PROBLEMA 2: solo llamamos requestGpsEnable si no hay
    // un diálogo de GPS ya abierto.
    val requestGps: () -> Unit = {
        if (!uiState.isGpsDialogActive) {
            viewModel.setGpsDialogActive(true)
            requestGpsEnable(context) { senderRequest ->
                gpsLauncher.launch(senderRequest)
            }
        }
    }

    // ── Carga inicial ─────────────────────────────────────────────────────────
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

    // ── Monitor de permisos de Compose ────────────────────────────────────────
    LaunchedEffect(permissionsState.allPermissionsGranted, permissionsState.shouldShowRationale) {
        evaluate()
    }

    // ── Arrancar cálculo ──────────────────────────────────────────────────────
    LaunchedEffect(uiState.step) {
        if (uiState.step == MapFlowStep.MAP_OK
            && calculateLocation
            && !uiState.isCalculatingLocation
            && !uiState.locationData.calculationFinished) {
            viewModel.startLocationCalculation()
        }
    }

    // ── Enfocar cámara ────────────────────────────────────────────────────────
    var hasFocused by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.locationData.tieneCoordenadasValidas) {
        if (focusOnCurrentLocation && uiState.locationData.tieneCoordenadasValidas && !hasFocused) {
            // Un pequeño delay de cortesía asegura que el mapa ya asimiló el Layout
            delay(500L)
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        com.google.android.gms.maps.model.LatLng(
                            uiState.locationData.latitude,
                            uiState.locationData.longitude
                        ), 16f
                    )
                )
                hasFocused = true // Marcamos como enfocado para evitar bucles de re-centrado molestos
            } catch (e: Exception) {
                // Evita fallas silenciosas catastróficas
                e.printStackTrace()
            }
        }
    }

    // ── ON_RESUME: verificar permisos reales ──────────────────────────────────
    // CORRECCIÓN PROBLEMA 1: en ON_RESUME usamos permisosRealmenteOtorgados()
    // para detectar permisos revocados en segundo plano, no el estado de Compose.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                evaluate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── BroadcastReceiver: GPS + modo avión ───────────────────────────────────
    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val gpsOn      = isGpsEnabled(ctx)
                //val airplaneOn = isAirplaneMode(ctx)
                val airplaneOn = isAirplaneMode(ctx)

                // Notificar al ViewModel (cancela job si es necesario, maneja modo avión)
                viewModel.onHardwareChanged(
                    gpsOn             = gpsOn,
                    airplaneOn        = airplaneOn,
                    calculateLocation = calculateLocation,
                )

                // Evaluar de inmediato el estado de la pantalla (actualiza el step visual)
                evaluate()

                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION
                    && !gpsOn
                    && permisosRealmenteOtorgados()
                    //&& !airplaneOn
                    && !viewModel.uiState.value.isGpsDialogActive) {
                    requestGps()
                }
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(Modifier.fillMaxSize()) {

        // El mapa SIEMPRE está vivo, incluso en modo avión
        GoogleMap(
            modifier            = Modifier.fillMaxSize(),
            properties          = MapProperties(
                // CORRECCIÓN PROBLEMA 1: usar verificación real, no el estado de Compose
                isMyLocationEnabled = permisosRealmenteOtorgados(),
            ),
            uiSettings          = MapUiSettings(
                myLocationButtonEnabled = focusOnCurrentLocation,
            ),
            cameraPositionState = cameraPositionState,
        )

        // Overlay de estados bloqueantes (GPS y permisos — NO modo avión)
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
                    onAction    = requestGps,
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

        // CORRECCIÓN PROBLEMA 3: modo avión → banner encima del mapa (no overlay bloqueante)
        AnimatedVisibility(
            visible = uiState.isAirplaneMode,
            enter   = slideInVertically { -it } + fadeIn(),
            exit    = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            AirplaneBanner(onGoToSettings = { openAirplaneSettings(context) })
        }

        if (calculateLocation && uiState.step == MapFlowStep.MAP_OK) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                CalculatedDataPanel(
                    locationData = uiState.locationData,
                    isCalculating = uiState.isCalculatingLocation,
                    remainingSeconds = uiState.remainingSeconds,
                    onRecalculate = {
                        // Llama a la función de re-cálculo que añadimos en el ViewModel
                        viewModel.recalculateLocation()
                    },
                    onAccept = { datosEnviados ->
                        // ¡AQUÍ MANEJAS LA NAVEGACIÓN O ENVÍO!
                        // 'datosEnviados' contiene la latitud, longitud, altitud y precisión finales.
                        println("Coordenadas listas para enviar: ${datosEnviados.latitude}")

                        // Ejemplo si usas Navigation Component:
                        // navController.navigate("pantalla_formulario/${datosEnviados.latitude}/${datosEnviados.longitude}")
                    }
                )
            }
        }

        // Loading inicial (muy breve)
        AnimatedVisibility(
            visible  = uiState.step == MapFlowStep.CHECKING,
            enter    = fadeIn(),
            exit     = fadeOut(tween(200)),
        ) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Verificando ubicación...", color = Color.White)
                }
            }
        }
    }

    // ── Diálogos ──────────────────────────────────────────────────────────────

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

    // CORRECCIÓN PROBLEMA 3: diálogo inicial cuando se activa modo avión
    if (uiState.showAirplaneDialog) {
        AirplaneDialog(
            onGoToSettings = {
                viewModel.dismissAirplaneDialog()
                openAirplaneSettings(context)
            },
            onContinue = {
                // Acepta continuar sin GPS — el banner superior lo recordará
                viewModel.dismissAirplaneDialog()
            },
        )
    }

    if (uiState.showTimeoutDialog) {
        TimeoutDialog(
            onRetry        = { viewModel.onRetryAfterTimeout() },
            onAcceptManual = { viewModel.onAcceptManualAfterTimeout() },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  OVERLAY — cubre el mapa para GPS y permisos (sin modo avión)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MapOverlay(
    icon: ImageVector, title: String, message: String,
    buttonLabel: String, onAction: () -> Unit, isError: Boolean = false,
) {
    val color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF1565C0)
    Box(
        modifier         = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier            = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier         = Modifier.size(80.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
            }
            Text(title, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Button(
                onClick  = onAction,
                colors   = ButtonDefaults.buttonColors(containerColor = color),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(buttonLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  BANNER MODO AVIÓN — encima del mapa, no lo bloquea
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AirplaneBanner(onGoToSettings: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        color  = Color(0xFFF59E0B).copy(alpha = 0.95f),
        shape  = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier            = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.AirplanemodeActive, null,
                tint = Color.White, modifier = Modifier.size(20.dp))
            Text(
                "Modo avión activo — la señal GPS puede ser limitada o inexistente.",
                color    = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onGoToSettings,
                colors  = ButtonDefaults.textButtonColors(contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIÁLOGO MODO AVIÓN — pregunta al activarse
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AirplaneDialog(onGoToSettings: () -> Unit, onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = onContinue,
        icon  = {
            Icon(Icons.Default.AirplanemodeActive, null, tint = Color(0xFFF59E0B))
        },
        title = { Text("Modo avión activado") },
        text  = {
            Text(
                "Con el modo avión activo, el GPS puede no funcionar correctamente " +
                        "y no se podrán obtener coordenadas precisas.\n\n" +
                        "¿Deseas desactivar el modo avión para continuar con normalidad?"
            )
        },
        confirmButton = {
            Button(
                onClick = onGoToSettings,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            ) {
                Icon(Icons.Default.Settings, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Desactivar modo avión")
            }
        },
        dismissButton = {
            TextButton(onClick = onContinue) {
                Text("Continuar de todas formas")
            }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  BADGE PRECISIÓN GPS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.LocationAccuracyBadge(
    locationData: LocationData,
    isCalculating: Boolean,
    topPadding: androidx.compose.ui.unit.Dp,
) {
    Card(
        modifier  = Modifier
            .align(Alignment.TopCenter)
            .padding(top = if (topPadding == 0.dp) 16.dp else topPadding + 8.dp)
            .fillMaxWidth(0.9f),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCalculating) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Buscando satélites...", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Cálculo finalizado", style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold)
                    }
                }
                val (color, label) = when (locationData.quality) {
                    PrecisionQuality.EXCELENTE   -> Color(0xFF10B981) to "Excelente (<10m)"
                    PrecisionQuality.REGULAR     -> Color(0xFFF59E0B) to "Regular (<50m)"
                    PrecisionQuality.DEFICIENTE  -> Color(0xFFEF4444) to "Deficiente (>50m)"
                    PrecisionQuality.DESCONOCIDA -> Color.Gray        to "Esperando..."
                }
                Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text(label, color = color,
                        style    = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold)
                }
            }
            if (locationData.tieneCoordenadasValidas) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Lat: ${locationData.latitude}  |  Lon: ${locationData.longitude}",
                    style = MaterialTheme.typography.bodySmall)
                Text("Altitud: ${"%.2f".format(locationData.altitude)} m  " +
                        "·  Precisión: ${"%.1f".format(locationData.accuracy)} m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIÁLOGOS RESTANTES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RationaleDialog(
    canShowNativeDialog: Boolean,
    onAllowNative: () -> Unit, onGoToSettings: () -> Unit, onDeny: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDeny,
        icon  = { Icon(Icons.Default.LocationOn, null,
            tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Permiso de ubicación necesario") },
        text  = {
            Text(
                "Esta función requiere acceso a tu ubicación para mostrar " +
                        "el mapa y capturar coordenadas GPS. Sin este permiso el mapa " +
                        "no podrá centrar tu posición ni registrar coordenadas."
            )
        },
        confirmButton = {
            Button(onClick = if (canShowNativeDialog) onAllowNative else onGoToSettings) {
                Text(if (canShowNativeDialog) "Conceder permiso" else "Ir a Ajustes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun TimeoutDialog(onRetry: () -> Unit, onAcceptManual: () -> Unit) {
    AlertDialog(
        onDismissRequest = onAcceptManual,
        icon  = { Icon(Icons.Default.GpsOff, null, tint = Color(0xFFF59E0B)) },
        title = { Text("No se pudo obtener la ubicación") },
        text  = {
            Text(
                "Pasaron 2 minutos sin recibir señal GPS suficiente. " +
                        "¿Deseas reintentar o ingresar las coordenadas manualmente?"
            )
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Reintentar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onAcceptManual) { Text("Ingresar manualmente") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun CalculatedDataPanel(
    locationData: LocationData,
    isCalculating: Boolean,
    remainingSeconds: Int, // <-- NUEVO
    onRecalculate: () -> Unit, // <-- NUEVO
    onAccept: (LocationData) -> Unit // <-- NUEVO (Pasa el objeto con los datos a la otra pantalla)
) {
    // Definición dinámica de colores y textos según la precisión actual
    val (statusColor, statusLabel) = when (locationData.quality) {
        PrecisionQuality.EXCELENTE   -> Color(0xFF10B981) to "Precisión Excelente (<10m)" // Verde
        PrecisionQuality.REGULAR     -> Color(0xFFF59E0B) to "Precisión Regular (<50m)"   // Amarillo
        PrecisionQuality.DEFICIENTE  -> Color(0xFFEF4444) to "Precisión Deficiente (>50m)" // Rojo
        PrecisionQuality.DESCONOCIDA -> Color.Gray        to "Buscando señal..."            // Gris
    }

    // Formateador dinámico para transformar los segundos en formato MM:SS
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado del estado del cálculo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCalculating) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = statusColor)
                        Spacer(Modifier.width(8.dp))
                        Text(text = "Calculando ($timeFormatted)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cálculo Finalizado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                // Badge de calidad con el color dinámico asignado
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Datos Calculados
            if (locationData.tieneCoordenadasValidas) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Latitud:", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        Text(locationData.latitude.toString(), style = MaterialTheme.typography.bodyMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Longitud:", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        Text(locationData.longitude.toString(), style = MaterialTheme.typography.bodyMedium, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Altitud:", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        Text("${"%.2f".format(locationData.altitude)} m", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Margen de error (Precisión):", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        Text("${"%.1f".format(locationData.accuracy)} m", color = statusColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // En caso de que se esté esperando la primera iteración de coordenadas válidas
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Esperando coordenadas del satélite...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // NUEVA SECCIÓN: BOTONES DE ACCIÓN
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Volver a calcular
                OutlinedButton(
                    onClick = onRecalculate,
                    modifier = Modifier.weight(1f),
                    enabled = !isCalculating, // Se deshabilita si ya está en proceso de cálculo
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Recalcular", fontWeight = FontWeight.Medium)
                }

                // Botón Aceptar enviar datos
                Button(
                    onClick = { onAccept(locationData) },
                    modifier = Modifier.weight(1f),
                    // Solo deja avanzar si ya se capturaron coordenadas reales
                    enabled = locationData.tieneCoordenadasValidas && !isCalculating,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
