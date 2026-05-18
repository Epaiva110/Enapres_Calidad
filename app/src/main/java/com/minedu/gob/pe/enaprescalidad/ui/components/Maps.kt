package com.minedu.gob.pe.enaprescalidad.ui.components

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState

import android.Manifest
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.google.accompanist.permissions.*
import com.minedu.gob.pe.enaprescalidad.utils.isGpsEnabled
import com.google.android.gms.location.*
import com.google.maps.android.compose.*
import com.minedu.gob.pe.enaprescalidad.utils.isAirplaneMode
import com.minedu.gob.pe.enaprescalidad.utils.openAirplaneSettings
import com.minedu.gob.pe.enaprescalidad.utils.openAppSettings
import com.minedu.gob.pe.enaprescalidad.utils.requestGpsEnable

// ─────────────────────────────────────────────────────────────────────────────
//  ESTADOS POSIBLES
// ─────────────────────────────────────────────────────────────────────────────

enum class MapFlowStep {
    CHECKING,          // Evaluando estado inicial
    MAP_OK,            // Todo OK — mostrar mapa
    GPS_REQUIRED,      // Permisos OK pero GPS apagado
    BLOCKED_SETTINGS,  // Permisos denegados permanentemente
    AIRPLANE_MODE,     // Modo avión activo
}

// ─────────────────────────────────────────────────────────────────────────────
//  SCREEN PRINCIPAL
// ─────────────────────────────────────────────────────────────────────────────

@SuppressLint("InlinedApi")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ── Estado ────────────────────────────────────────────────────────────────
    var step by remember { mutableStateOf(MapFlowStep.CHECKING) }
    var showRationaleDialog by remember { mutableStateOf(false) }
    var isAirplaneModeOn by remember { mutableStateOf(isAirplaneMode(context)) }
    var isGpsOn by remember { mutableStateOf(isGpsEnabled(context)) }

    // ── Permisos ──────────────────────────────────────────────────────────────
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    // ── Launcher para el diálogo de activar GPS ───────────────────────────────
    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        isGpsOn = isGpsEnabled(context)
        step = if (isGpsOn) MapFlowStep.MAP_OK else MapFlowStep.GPS_REQUIRED
    }

    val requestGps: () -> Unit = {
        requestGpsEnable(context, gpsLauncher::launch)
    }

    // ── Evaluación centralizada del estado (CORREGIDA) ────────────────────────
    val evaluate: () -> Unit = {
        isAirplaneModeOn = isAirplaneMode(context)
        isGpsOn = isGpsEnabled(context)

        step = when {
            isAirplaneModeOn -> MapFlowStep.AIRPLANE_MODE

            !permissionsState.allPermissionsGranted -> {
                if (permissionsState.shouldShowRationale) {
                    if (!showRationaleDialog) showRationaleDialog = true
                    // Si ya estábamos en CHECKING, mantenemos la carga limpia,
                    // sino nos quedamos en el paso anterior mientras se decide
                    if (step == MapFlowStep.CHECKING) MapFlowStep.CHECKING else step
                } else {
                    // Si no hay permisos y el sistema no permite mostrar la explicación,
                    // significa que está denegado de forma absoluta y permanente.
                    MapFlowStep.BLOCKED_SETTINGS
                }
            }
            isGpsOn -> MapFlowStep.MAP_OK
            else -> MapFlowStep.GPS_REQUIRED
        }
    }

    // ── Monitor de ciclo de vida ──────────────────────────────────────────────
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // CORRECCIÓN: Si el usuario ya está bloqueado en Ajustes, solo revaluamos si vuelve a primer plano
            if (event == Lifecycle.Event.ON_RESUME) {
                evaluate()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Monitor de GPS y modo avión en tiempo real ────────────────────────────
    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                evaluate()
                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION
                    && !isGpsEnabled(ctx)
                    && step == MapFlowStep.MAP_OK
                    && permissionsState.allPermissionsGranted) {
                    requestGps()
                }
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ── Monitor de conectividad ───────────────────────────────────────────────
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { evaluate() }
            override fun onLost(network: Network) { evaluate() }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, callback)
        onDispose { cm.unregisterNetworkCallback(callback) }
    }

    // ── Carga inicial ─────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        if (permissionsState.allPermissionsGranted) {
            evaluate()
        } else {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    // ── Reaccionar cuando cambian los permisos (CORREGIDO CORDÓN DE SEGURIDAD) ──
    LaunchedEffect(permissionsState.allPermissionsGranted, permissionsState.shouldShowRationale) {
        // CORRECCIÓN: Si el flujo ya determinó que estamos bloqueados permanentemente,
        // no permitimos que un cambio de recomposición local altere el estado hasta que
        // se regrese de manera formal de los Ajustes mediante ON_RESUME.
        if (step != MapFlowStep.BLOCKED_SETTINGS) {
            evaluate()
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────
    AnimatedContent(
        targetState = step,
        transitionSpec = { fadeIn(tween(300)).togetherWith(fadeOut(tween(200))) },
        label = "map_step",
    ) { currentStep ->
        when (currentStep) {
            MapFlowStep.MAP_OK -> MapOkContent()

            MapFlowStep.GPS_REQUIRED -> BlockedContent(
                icon = Icons.Default.LocationOff,
                title = "GPS desactivado",
                message = "El GPS está apagado. Actívalo para usar el mapa.",
                buttonLabel = "Activar GPS",
                onAction = requestGps,
            )

            MapFlowStep.BLOCKED_SETTINGS -> BlockedContent(
                icon = Icons.Default.LocationDisabled,
                title = "Permiso de ubicación necesario",
                message = "Has desactivado o denegado los permisos de ubicación. " +
                        "Para usar esta funcionalidad, ve a Ajustes → Permisos → Ubicación y actívalos de forma manual.",
                buttonLabel = "Ir a Ajustes",
                onAction = { openAppSettings(context) },
            )

            MapFlowStep.AIRPLANE_MODE -> BlockedContent(
                icon = Icons.Default.AirplanemodeActive,
                title = "Modo avión activo",
                message = "El GPS no funciona con el modo avión activo. " +
                        "Desactívalo para continuar.",
                buttonLabel = "Abrir Ajustes",
                onAction = { openAirplaneSettings(context) },
                isWarning = true,
            )

            MapFlowStep.CHECKING -> Box(
                Modifier.fillMaxSize(), Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Verificando ubicación...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // ── Diálogo intermedio (primera denegación) ───────────────────────────────
    if (showRationaleDialog) {
        RationaleDialog(
            canShowNativeDialog = permissionsState.shouldShowRationale,
            onAllowNative = {
                showRationaleDialog = false
                permissionsState.launchMultiplePermissionRequest()
            },
            onGoToSettings = {
                showRationaleDialog = false
                step = MapFlowStep.BLOCKED_SETTINGS
                openAppSettings(context)
            },
            onDeny = {
                showRationaleDialog = false
                step = MapFlowStep.BLOCKED_SETTINGS
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CONTENIDO: MAPA OK
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MapOkContent() {
    val cameraPositionState = rememberCameraPositionState()
    GoogleMap(
        modifier           = Modifier.fillMaxSize(),
        properties         = MapProperties(isMyLocationEnabled = true),
        cameraPositionState = cameraPositionState,
        uiSettings         = MapUiSettings(myLocationButtonEnabled = true),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  CONTENIDO: PANTALLA DE BLOQUEO GENÉRICA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BlockedContent(
    icon: ImageVector,
    title: String,
    message: String,
    buttonLabel: String,
    onAction: () -> Unit,
    isWarning: Boolean = false,
) {
    val accentColor = if (isWarning) Color(0xFFF59E0B) else MaterialTheme.colorScheme.error

    Box(
        modifier           = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment   = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Ícono con fondo circular
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null,
                    tint     = accentColor,
                    modifier = Modifier.size(40.dp))
            }

            Text(title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center)

            Text(message,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)

            Button(
                onClick = onAction,
                colors  = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape   = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(buttonLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DIÁLOGO INTERMEDIO — primera denegación de permisos
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RationaleDialog(
    canShowNativeDialog: Boolean,
    onAllowNative: () -> Unit,
    onGoToSettings: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        icon  = { Icon(Icons.Default.LocationOn, null,
            tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Permiso de ubicación necesario") },
        text  = {
            Text(
                "Esta función requiere acceso a tu ubicación para mostrar el mapa " +
                        "y registrar coordenadas GPS. Sin este permiso no podrás usar " +
                        "la función de mapa ni captura GPS.\n\n" +
                        if (canShowNativeDialog) "¿Deseas conceder el permiso?"
                        else "Para activar esta función, debes habilitar los permisos desde los ajustes del sistema."
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canShowNativeDialog) {
                        onAllowNative()
                    } else {
                        onGoToSettings()
                    }
                }
            ) {
                Text(if (canShowNativeDialog) "Conceder permiso" else "Ir a Ajustes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) { Text("No, cancelar") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}
