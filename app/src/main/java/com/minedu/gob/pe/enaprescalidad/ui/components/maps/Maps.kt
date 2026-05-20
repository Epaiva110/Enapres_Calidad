package com.minedu.gob.pe.enaprescalidad.ui.components.maps

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
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

    if (uiState.showAirplaneDialog && !uiState.rpshowAirplaneDialog) {
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


@Composable
private fun MapOverlay(
    icon: ImageVector, title: String, message: String,
    buttonLabel: String, onAction: () -> Unit, isError: Boolean = false,
) {
    val color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF1565C0)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(999.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(buttonLabel, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AirplaneBanner(onGoToSettings: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        color = Color(0xFFF59E0B).copy(alpha = 0.95f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                Icons.Default.AirplanemodeActive,
                null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Modo avión activo — la señal GPS puede ser limitada o inexistente.",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onGoToSettings,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AirplaneDialog(onGoToSettings: () -> Unit, onContinue: () -> Unit) {
    AlertDialog(
        onDismissRequest = onContinue,
        icon = {
            Icon(
                Icons.Default.AirplanemodeActive,
                null,
                tint = Color(0xFFF59E0B)
            )
        },
        title = { Text("Modo avión activado") },
        text = {
            Text(
                "Con el modo avión activo, el GPS puede no funcionar correctamente " +
                        "y no se podrán obtener coordenadas precisas.\n\n" +
                        "¿Deseas desactivar el modo avión para continuar con normalidad?"
            )
        },
        confirmButton = {
            Button(
                onClick = onGoToSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            ) {
                Icon(Icons.Default.Settings, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Desactivar modo avión")
            }
        },
        dismissButton = {
            TextButton(onClick = onContinue) { Text("Continuar de todas formas") }
        },
        shape = RoundedCornerShape(16.dp),
    )
}

@Composable
private fun RationaleDialog(
    canShowNativeDialog: Boolean,
    onAllowNative: () -> Unit, onGoToSettings: () -> Unit, onDeny: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDeny,
        icon = {
            Icon(
                Icons.Default.LocationOn,
                null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Permiso de ubicación necesario") },
        text = {
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
        icon = { Icon(Icons.Default.GpsOff, null, tint = Color(0xFFF59E0B)) },
        title = { Text("No se pudo obtener la ubicación") },
        text = {
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

@Preview
@Composable
private fun prueba ()
{
    CalculatedDataPanel(
        locationData = LocationData(quality = PrecisionQuality.EXCELENTE),
        isCalculating = false,
        remainingSeconds = 120,
        onRecalculate = {},
        onAccept = { }
    )
}

@Preview
@Composable
private fun prueba2 ()
{
    CalculatedDataPanel23(
        locationData = LocationData(quality = PrecisionQuality.EXCELENTE),
        isCalculating = false,
        remainingSeconds = 120,
        onRecalculate = {},
        onAccept = { }
    )
}


@Composable
private fun CalculatedDataPanel(
    locationData: LocationData,
    isCalculating: Boolean,
    remainingSeconds: Int,
    onRecalculate: () -> Unit,
    onAccept: (LocationData) -> Unit
) {
    val (statusColor, statusLabel) = when (locationData.quality) {
        PrecisionQuality.EXCELENTE -> Color(0xFF10B981) to "Precisión Excelente (<10m)"
        PrecisionQuality.REGULAR -> Color(0xFFF59E0B) to "Precisión Regular (<50m)"
        PrecisionQuality.DEFICIENTE -> Color(0xFFEF4444) to "Precisión Deficiente (>50m)"
        PrecisionQuality.DESCONOCIDA -> Color.Gray to "Buscando señal..."
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    var expanded by rememberSaveable { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.98f
            )
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCalculating) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = statusColor
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Calculando ($timeFormatted)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Cálculo Finalizado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Row(

                ) {

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

                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector =
                            if (expanded)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

            }

            if (!expanded) {HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))}

            AnimatedVisibility(visible = !expanded) {

                if (locationData.tieneCoordenadasValidas) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Latitud:",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                locationData.latitude.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Longitud:",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                locationData.longitude.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Altitud:",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${"%.2f".format(locationData.altitude)} m",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Margen de error (Precisión):",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${"%.1f".format(locationData.accuracy)} m",
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Esperando coordenadas del satélite...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            }

            if (!expanded) {HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))}

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRecalculate,
                    modifier = Modifier.weight(1f),
                    enabled = !isCalculating,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Recalcular", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { onAccept(locationData) },
                    modifier = Modifier.weight(1f),
                    enabled = locationData.tieneCoordenadasValidas && !isCalculating,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CalculatedDataPanel23(
    locationData: LocationData,
    isCalculating: Boolean,
    remainingSeconds: Int,
    onRecalculate: () -> Unit,
    onAccept: (LocationData) -> Unit
) {

    var expanded by rememberSaveable { mutableStateOf(true) }

    val (statusColor, statusLabel) = when (locationData.quality) {
        PrecisionQuality.EXCELENTE -> Color(0xFF10B981) to "Precisión Excelente (<10m)"
        PrecisionQuality.REGULAR -> Color(0xFFF59E0B) to "Precisión Regular (<50m)"
        PrecisionQuality.DEFICIENTE -> Color(0xFFEF4444) to "Precisión Deficiente (>50m)"
        PrecisionQuality.DESCONOCIDA -> Color.Gray to "Buscando señal..."
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    if (isCalculating) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = statusColor
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Calculando ($timeFormatted)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {

                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Cálculo Finalizado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {

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

                    Spacer(Modifier.width(6.dp))

                    Icon(
                        imageVector =
                            if (expanded)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // CONTENIDO COLAPSABLE
            AnimatedVisibility(visible = expanded) {

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    if (locationData.tieneCoordenadasValidas) {

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                            DataRow(
                                label = "Latitud:",
                                value = locationData.latitude.toString()
                            )

                            DataRow(
                                label = "Longitud:",
                                value = locationData.longitude.toString()
                            )

                            DataRow(
                                label = "Altitud:",
                                value = "${"%.2f".format(locationData.altitude)} m"
                            )

                            DataRow(
                                label = "Margen de error:",
                                value = "${"%.1f".format(locationData.accuracy)} m",
                                valueColor = statusColor
                            )
                        }

                    } else {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                "Esperando coordenadas del satélite...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        OutlinedButton(
                            onClick = onRecalculate,
                            modifier = Modifier.weight(1f),
                            enabled = !isCalculating,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(Modifier.width(6.dp))

                            Text("Recalcular")
                        }

                        Button(
                            onClick = { onAccept(locationData) },
                            modifier = Modifier.weight(1f),
                            enabled = locationData.tieneCoordenadasValidas && !isCalculating,
                            shape = RoundedCornerShape(10.dp)
                        ) {

                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(Modifier.width(6.dp))

                            Text("Aceptar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DataRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            color = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}

///<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
//@SuppressLint("InlinedApi")
//@OptIn(ExperimentalPermissionsApi::class)
//@Composable
//fun MapScreen(
//    viewModel: MapViewModel = viewModel(),
//    showCoordenadas: Boolean = true,
//    calculateLocation: Boolean = true,
//    focusOnCurrentLocation: Boolean = true,
//    onLocationAccepted: ((LocationData) -> Unit)? = null,   // ← NUEVO (nullable para compatibilidad)
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val uiState by viewModel.uiState.collectAsState()
//    val scope = rememberCoroutineScope()
//
//    val permissionsState = rememberMultiplePermissionsState(
//        listOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//        )
//    )
//
//    val cameraPositionState = rememberCameraPositionState()
//
//    fun permisosRealmenteOtorgados(): Boolean =
//        listOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//        ).all {
//            ContextCompat.checkSelfPermission(
//                context,
//                it
//            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
//        }
//
//    fun evaluate() {
//        val tienePermisos = permisosRealmenteOtorgados()
//        val gpsOn = isGpsEnabled(context)
//
//        when {
//            !tienePermisos -> {
//                if (permissionsState.shouldShowRationale) {
//                    viewModel.showRationale(true)
//                } else {
//                    viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
//                }
//            }
//
//            !gpsOn -> {
//                viewModel.setStep(MapFlowStep.GPS_REQUIRED)
//            }
//
//            else -> {
//                viewModel.setStep(MapFlowStep.MAP_OK)
//            }
//        }
//    }
//
//    val gpsLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.StartIntentSenderForResult()
//    ) {
//        viewModel.setGpsDialogActive(false)
//        evaluate()
//    }
//
//    val requestGps: () -> Unit = {
//        if (!uiState.isGpsDialogActive) {
//            viewModel.setGpsDialogActive(true)
//            requestGpsEnable(context) { senderRequest ->
//                gpsLauncher.launch(senderRequest)
//            }
//        }
//    }
//
//    fun centrarCamaraEnUbicacion() {
//        if (uiState.locationData.tieneCoordenadasValidas) {
//            scope.launch {
//                try {
//                    cameraPositionState.animate(
//                        CameraUpdateFactory.newLatLngZoom(
//                            LatLng(uiState.locationData.latitude, uiState.locationData.longitude),
//                            16f
//                        )
//                    )
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
//
//    // ── Ciclos de Carga y Eventos Efectivos ───────────────────────────────────
//    LaunchedEffect(Unit) {
//        val gpsOn = isGpsEnabled(context)
//        val airplaneOn = isAirplaneMode(context)
//
//        viewModel.onHardwareChanged(
//            gpsOn = gpsOn,
//            airplaneOn = airplaneOn,
//            calculateLocation = calculateLocation,
//            isInitialCheck = true
//        )
//        if (permisosRealmenteOtorgados()) {
//            evaluate()
//        } else {
//            permissionsState.launchMultiplePermissionRequest()
//        }
//    }
//
//    LaunchedEffect(permissionsState.allPermissionsGranted, permissionsState.shouldShowRationale) {
//        evaluate()
//    }
//
//    LaunchedEffect(uiState.step) {
//        if (uiState.step == MapFlowStep.MAP_OK
//            && calculateLocation
//            && !uiState.isCalculatingLocation
//            && !uiState.locationData.calculationFinished
//        ) {
//            viewModel.startLocationCalculation()
//        }
//    }
//
//    var hasFocused by rememberSaveable { mutableStateOf(false) }
//    LaunchedEffect(uiState.locationData.tieneCoordenadasValidas) {
//        if (focusOnCurrentLocation && uiState.locationData.tieneCoordenadasValidas && !hasFocused) {
//            delay(500L)
//            centrarCamaraEnUbicacion()
//            hasFocused = true
//        }
//    }
//
//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_RESUME) {
//                evaluate()
//            }
//        }
//        lifecycleOwner.lifecycle.addObserver(observer)
//        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
//    }
//
//    DisposableEffect(context) {
//        val filter = IntentFilter().apply {
//            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
//            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
//        }
//        val receiver = object : BroadcastReceiver() {
//            override fun onReceive(ctx: Context, intent: Intent) {
//                val gpsOn = isGpsEnabled(ctx)
//                val airplaneOn = isAirplaneMode(ctx)
//
//                viewModel.onHardwareChanged(
//                    gpsOn = gpsOn,
//                    airplaneOn = airplaneOn,
//                    calculateLocation = calculateLocation,
//                )
//
//                evaluate()
//
//                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION
//                    && !gpsOn
//                    && permisosRealmenteOtorgados()
//                    && !viewModel.uiState.value.isGpsDialogActive
//                ) {
//                    requestGps()
//                }
//            }
//        }
//        context.registerReceiver(receiver, filter)
//        onDispose { context.unregisterReceiver(receiver) }
//    }
//
//    // ── Estructura de UI ──────────────────────────────────────────────────────
//    Box(Modifier.fillMaxSize()) {
//
//        // Mapa base siempre activo
//        GoogleMap(
//            modifier = Modifier.fillMaxSize(),
//            properties = MapProperties(
//                isMyLocationEnabled = permisosRealmenteOtorgados(),
//            ),
//            uiSettings = MapUiSettings(
//                myLocationButtonEnabled = false // Desactivado para implementar nuestro botón abajo a la izquierda
//            ),
//            cameraPositionState = cameraPositionState,
//        )
//
//        // BOTÓN PERSONALIZADO DE MI UBICACIÓN (Abajo a la izquierda)
//        if (uiState.step == MapFlowStep.MAP_OK && uiState.locationData.tieneCoordenadasValidas) {
//            FloatingActionButton(
//                onClick = { centrarCamaraEnUbicacion() },
//                containerColor = MaterialTheme.colorScheme.primaryContainer,
//                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
//                modifier = Modifier
//                    .align(Alignment.CenterEnd)
//                    // Offset condicional para que suba si el panel de datos está visible
//                    .padding(
//                        start = 16.dp,
//                        bottom = if (calculateLocation) 230.dp else 16.dp
//                    )
//                    .size(52.dp)
//            ) {
//                Icon(Icons.Default.MyLocation, contentDescription = "Centrar ubicación")
//            }
//        }
//
//        // Overlays de bloqueo (GPS / Permisos)
//        AnimatedVisibility(
//            visible = uiState.step != MapFlowStep.MAP_OK && uiState.step != MapFlowStep.CHECKING,
//            enter = fadeIn(tween(300)),
//            exit = fadeOut(tween(200)),
//        ) {
//            when (uiState.step) {
//                MapFlowStep.GPS_REQUIRED -> MapOverlay(
//                    icon = Icons.Default.LocationOff,
//                    title = "GPS desactivado",
//                    message = "El GPS está apagado. Actívalo para usar el mapa y capturar coordenadas.",
//                    buttonLabel = "Activar GPS",
//                    onAction = requestGps,
//                )
//
//                MapFlowStep.BLOCKED_SETTINGS -> MapOverlay(
//                    icon = Icons.Default.LocationDisabled,
//                    title = "Permiso de ubicación requerido",
//                    message = "Denegaste los permisos de ubicación. Ve a Ajustes para habilitarlos.",
//                    buttonLabel = "Ir a Ajustes",
//                    onAction = { openAppSettings(context) },
//                    isError = true,
//                )
//                else -> Unit
//            }
//        }
//
//        // Banner de Modo Avión (Superior, no bloqueante)
//        AnimatedVisibility(
//            visible = uiState.isAirplaneMode,
//            enter = slideInVertically { -it } + fadeIn(),
//            exit = slideOutVertically { -it } + fadeOut(),
//            modifier = Modifier.align(Alignment.TopCenter),
//        ) {
//            AirplaneBanner(onGoToSettings = { openAirplaneSettings(context) })
//        }
//
//        // Panel de datos calculados
//        if (calculateLocation && uiState.step == MapFlowStep.MAP_OK && showCoordenadas) {
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(16.dp)
//            ) {
//                CalculatedDataPanel(
//                    locationData = uiState.locationData,
//                    isCalculating = uiState.isCalculatingLocation,
//                    remainingSeconds = uiState.remainingSeconds,
//                    onRecalculate = { viewModel.recalculateLocation() },
//                    onAccept = { datosEnviados ->
//                        println("Coordenadas listas para enviar: ${datosEnviados.latitude}")
//                    }
//                )
//            }
//        }
//
//        // Loading de verificación inicial
//        AnimatedVisibility(
//            visible = uiState.step == MapFlowStep.CHECKING,
//            enter = fadeIn(),
//            exit = fadeOut(tween(200)),
//        ) {
//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.4f)),
//                Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    CircularProgressIndicator(color = Color.White)
//                    Text("Verificando ubicación...", color = Color.White)
//                }
//            }
//        }
//    }
//
//    // ── Diálogos de Gestión Flujo Hardware ────────────────────────────────────
//    if (uiState.showRationaleDialog) {
//        RationaleDialog(
//            canShowNativeDialog = permissionsState.shouldShowRationale,
//            onAllowNative = {
//                viewModel.showRationale(false)
//                permissionsState.launchMultiplePermissionRequest()
//            },
//            onGoToSettings = {
//                viewModel.showRationale(false)
//                viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
//                openAppSettings(context)
//            },
//            onDeny = {
//                viewModel.showRationale(false)
//                viewModel.setStep(MapFlowStep.BLOCKED_SETTINGS)
//            },
//        )
//    }
//
//    if (uiState.showAirplaneDialog && !uiState.rpshowAirplaneDialog) {
//        AirplaneDialog(
//            onGoToSettings = {
//                viewModel.dismissAirplaneDialog()
//                openAirplaneSettings(context)
//            },
//            onContinue = { viewModel.dismissAirplaneDialog() },
//        )
//    }
//
//    if (uiState.showTimeoutDialog) {
//        TimeoutDialog(
//            onRetry = { viewModel.onRetryAfterTimeout() },
//            onAcceptManual = { viewModel.onAcceptManualAfterTimeout() },
//        )
//    }
//}
//
//
//@Composable
//private fun MapOverlay(
//    icon: ImageVector, title: String, message: String,
//    buttonLabel: String, onAction: () -> Unit, isError: Boolean = false,
//) {
//    val color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF1565C0)
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MaterialTheme.colorScheme.surface),
//        contentAlignment = Alignment.Center,
//    ) {
//        Column(
//            modifier = Modifier.padding(32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(80.dp)
//                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(999.dp)),
//                contentAlignment = Alignment.Center,
//            ) {
//                Icon(icon, null, tint = color, modifier = Modifier.size(40.dp))
//            }
//            Text(
//                title,
//                style = MaterialTheme.typography.titleLarge,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center
//            )
//            Text(
//                message,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = TextAlign.Center
//            )
//            Button(
//                onClick = onAction,
//                colors = ButtonDefaults.buttonColors(containerColor = color),
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier.fillMaxWidth(),
//            ) {
//                Text(buttonLabel, fontWeight = FontWeight.SemiBold)
//            }
//        }
//    }
//}
//
//@Composable
//private fun AirplaneBanner(onGoToSettings: () -> Unit) {
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        color = Color(0xFFF59E0B).copy(alpha = 0.95f),
//        shape = RoundedCornerShape(12.dp),
//        shadowElevation = 4.dp,
//    ) {
//        Row(
//            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//        ) {
//            Icon(
//                Icons.Default.AirplanemodeActive,
//                null,
//                tint = Color.White,
//                modifier = Modifier.size(20.dp)
//            )
//            Text(
//                "Modo avión activo — la señal GPS puede ser limitada o inexistente.",
//                color = Color.White,
//                fontSize = 12.sp,
//                modifier = Modifier.weight(1f),
//            )
//            TextButton(
//                onClick = onGoToSettings,
//                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
//                contentPadding = PaddingValues(horizontal = 8.dp),
//            ) {
//                Text("Ajustes", fontWeight = FontWeight.Bold, fontSize = 12.sp)
//            }
//        }
//    }
//}
//
//@Composable
//private fun AirplaneDialog(onGoToSettings: () -> Unit, onContinue: () -> Unit) {
//    AlertDialog(
//        onDismissRequest = onContinue,
//        icon = {
//            Icon(
//                Icons.Default.AirplanemodeActive,
//                null,
//                tint = Color(0xFFF59E0B)
//            )
//        },
//        title = { Text("Modo avión activado") },
//        text = {
//            Text(
//                "Con el modo avión activo, el GPS puede no funcionar correctamente " +
//                        "y no se podrán obtener coordenadas precisas.\n\n" +
//                        "¿Deseas desactivar el modo avión para continuar con normalidad?"
//            )
//        },
//        confirmButton = {
//            Button(
//                onClick = onGoToSettings,
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
//            ) {
//                Icon(Icons.Default.Settings, null, Modifier.size(16.dp))
//                Spacer(Modifier.width(4.dp))
//                Text("Desactivar modo avión")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onContinue) { Text("Continuar de todas formas") }
//        },
//        shape = RoundedCornerShape(16.dp),
//    )
//}
//
//@Composable
//private fun RationaleDialog(
//    canShowNativeDialog: Boolean,
//    onAllowNative: () -> Unit, onGoToSettings: () -> Unit, onDeny: () -> Unit,
//) {
//    AlertDialog(
//        onDismissRequest = onDeny,
//        icon = {
//            Icon(
//                Icons.Default.LocationOn,
//                null,
//                tint = MaterialTheme.colorScheme.primary
//            )
//        },
//        title = { Text("Permiso de ubicación necesario") },
//        text = {
//            Text(
//                "Esta función requiere acceso a tu ubicación para mostrar " +
//                        "el mapa y capturar coordenadas GPS. Sin este permiso el mapa " +
//                        "no podrá centrar tu posición ni registrar coordenadas."
//            )
//        },
//        confirmButton = {
//            Button(onClick = if (canShowNativeDialog) onAllowNative else onGoToSettings) {
//                Text(if (canShowNativeDialog) "Conceder permiso" else "Ir a Ajustes")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDeny) { Text("Cancelar") }
//        },
//        shape = RoundedCornerShape(16.dp),
//    )
//}
//
//@Composable
//private fun TimeoutDialog(onRetry: () -> Unit, onAcceptManual: () -> Unit) {
//    AlertDialog(
//        onDismissRequest = onAcceptManual,
//        icon = { Icon(Icons.Default.GpsOff, null, tint = Color(0xFFF59E0B)) },
//        title = { Text("No se pudo obtener la ubicación") },
//        text = {
//            Text(
//                "Pasaron 2 minutos sin recibir señal GPS suficiente. " +
//                        "¿Deseas reintentar o ingresar las coordenadas manualmente?"
//            )
//        },
//        confirmButton = {
//            Button(onClick = onRetry) {
//                Icon(Icons.Default.Refresh, null, Modifier.size(16.dp))
//                Spacer(Modifier.width(4.dp))
//                Text("Reintentar")
//            }
//        },
//        dismissButton = {
//            OutlinedButton(onClick = onAcceptManual) { Text("Ingresar manualmente") }
//        },
//        shape = RoundedCornerShape(16.dp),
//    )
//}
//
//@Preview
//@Composable
//private fun prueba ()
//{
//    CalculatedDataPanel(
//        locationData = LocationData(quality = PrecisionQuality.EXCELENTE),
//        isCalculating = false,
//        remainingSeconds = 120,
//        onRecalculate = {},
//        onAccept = { }
//    )
//}
//
//@Preview
//@Composable
//private fun prueba2 ()
//{
//    CalculatedDataPanel23(
//        locationData = LocationData(quality = PrecisionQuality.EXCELENTE),
//        isCalculating = false,
//        remainingSeconds = 120,
//        onRecalculate = {},
//        onAccept = { }
//    )
//}
//
//
//@Composable
//private fun CalculatedDataPanel(
//    locationData: LocationData,
//    isCalculating: Boolean,
//    remainingSeconds: Int,
//    onRecalculate: () -> Unit,
//    onAccept: (LocationData) -> Unit
//) {
//    val (statusColor, statusLabel) = when (locationData.quality) {
//        PrecisionQuality.EXCELENTE -> Color(0xFF10B981) to "Precisión Excelente (<10m)"
//        PrecisionQuality.REGULAR -> Color(0xFFF59E0B) to "Precisión Regular (<50m)"
//        PrecisionQuality.DEFICIENTE -> Color(0xFFEF4444) to "Precisión Deficiente (>50m)"
//        PrecisionQuality.DESCONOCIDA -> Color.Gray to "Buscando señal..."
//    }
//
//    val minutes = remainingSeconds / 60
//    val seconds = remainingSeconds % 60
//    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
//    var expanded by rememberSaveable { mutableStateOf(true) }
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface.copy(
//                alpha = 0.98f
//            )
//        ),
//        elevation = CardDefaults.cardElevation(6.dp),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Column(modifier = Modifier
//            .padding(8.dp)
//            .fillMaxWidth()
//            .clickable { expanded = !expanded }
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    if (isCalculating) {
//                        CircularProgressIndicator(
//                            Modifier.size(16.dp),
//                            strokeWidth = 2.dp,
//                            color = statusColor
//                        )
//                        Spacer(Modifier.width(4.dp))
//                        Text(
//                            text = "Calculando ($timeFormatted)",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                    } else {
//                        Icon(
//                            Icons.Default.CheckCircle,
//                            contentDescription = null,
//                            tint = statusColor,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(Modifier.width(4.dp))
//                        Text(
//                            "Cálculo Finalizado",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//                Row(
//
//                ) {
//
//                    Surface(
//                        color = statusColor.copy(alpha = 0.15f),
//                        shape = RoundedCornerShape(8.dp)
//                    ) {
//                        Text(
//                            text = statusLabel,
//                            color = statusColor,
//                            style = MaterialTheme.typography.labelSmall,
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//
//                    Spacer(Modifier.width(2.dp))
//                    Icon(
//                        imageVector =
//                            if (expanded)
//                                Icons.Default.KeyboardArrowUp
//                            else
//                                Icons.Default.KeyboardArrowDown,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.align(Alignment.CenterVertically)
//                    )
//                }
//
//            }
//
//            if (!expanded) {HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))}
//
//            AnimatedVisibility(visible = !expanded) {
//
//                if (locationData.tieneCoordenadasValidas) {
//                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
//                        Row(
//                            Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                "Latitud:",
//                                fontWeight = FontWeight.Medium,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                            Text(
//                                locationData.latitude.toString(),
//                                style = MaterialTheme.typography.bodyMedium,
//                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
//                            )
//                        }
//                        Row(
//                            Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                "Longitud:",
//                                fontWeight = FontWeight.Medium,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                            Text(
//                                locationData.longitude.toString(),
//                                style = MaterialTheme.typography.bodyMedium,
//                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
//                            )
//                        }
//                        Row(
//                            Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                "Altitud:",
//                                fontWeight = FontWeight.Medium,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                            Text(
//                                "${"%.2f".format(locationData.altitude)} m",
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                        }
//                        Row(
//                            Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                "Margen de error (Precisión):",
//                                fontWeight = FontWeight.Medium,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                            Text(
//                                "${"%.1f".format(locationData.accuracy)} m",
//                                color = statusColor,
//                                fontWeight = FontWeight.Bold,
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                        }
//                    }
//                } else {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 16.dp), contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "Esperando coordenadas del satélite...",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//
//            }
//
//            if (!expanded) {HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))}
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                OutlinedButton(
//                    onClick = onRecalculate,
//                    modifier = Modifier.weight(1f),
//                    enabled = !isCalculating,
//                    shape = RoundedCornerShape(10.dp)
//                ) {
//                    Icon(
//                        Icons.Default.Refresh,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Spacer(Modifier.width(6.dp))
//                    Text("Recalcular", fontWeight = FontWeight.Medium)
//                }
//
//                Button(
//                    onClick = { onAccept(locationData) },
//                    modifier = Modifier.weight(1f),
//                    enabled = locationData.tieneCoordenadasValidas && !isCalculating,
//                    shape = RoundedCornerShape(10.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
//                ) {
//                    Icon(
//                        Icons.Default.Check,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Spacer(Modifier.width(6.dp))
//                    Text("Aceptar", fontWeight = FontWeight.Bold)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun CalculatedDataPanel23(
//    locationData: LocationData,
//    isCalculating: Boolean,
//    remainingSeconds: Int,
//    onRecalculate: () -> Unit,
//    onAccept: (LocationData) -> Unit
//) {
//
//    var expanded by rememberSaveable { mutableStateOf(true) }
//
//    val (statusColor, statusLabel) = when (locationData.quality) {
//        PrecisionQuality.EXCELENTE -> Color(0xFF10B981) to "Precisión Excelente (<10m)"
//        PrecisionQuality.REGULAR -> Color(0xFFF59E0B) to "Precisión Regular (<50m)"
//        PrecisionQuality.DEFICIENTE -> Color(0xFFEF4444) to "Precisión Deficiente (>50m)"
//        PrecisionQuality.DESCONOCIDA -> Color.Gray to "Buscando señal..."
//    }
//
//    val minutes = remainingSeconds / 60
//    val seconds = remainingSeconds % 60
//    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
//        ),
//        elevation = CardDefaults.cardElevation(6.dp),
//        shape = RoundedCornerShape(16.dp)
//    ) {
//
//        Column {
//
//            // HEADER
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable { expanded = !expanded },
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//
//                    if (isCalculating) {
//                        CircularProgressIndicator(
//                            Modifier.size(16.dp),
//                            strokeWidth = 2.dp,
//                            color = statusColor
//                        )
//
//                        Spacer(Modifier.width(8.dp))
//
//                        Text(
//                            text = "Calculando ($timeFormatted)",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                    } else {
//
//                        Icon(
//                            Icons.Default.CheckCircle,
//                            contentDescription = null,
//                            tint = statusColor,
//                            modifier = Modifier.size(20.dp)
//                        )
//
//                        Spacer(Modifier.width(8.dp))
//
//                        Text(
//                            "Cálculo Finalizado",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//                }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//
//                    Surface(
//                        color = statusColor.copy(alpha = 0.15f),
//                        shape = RoundedCornerShape(8.dp)
//                    ) {
//                        Text(
//                            text = statusLabel,
//                            color = statusColor,
//                            style = MaterialTheme.typography.labelSmall,
//                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//
//                    Spacer(Modifier.width(6.dp))
//
//                    Icon(
//                        imageVector =
//                            if (expanded)
//                                Icons.Default.KeyboardArrowUp
//                            else
//                                Icons.Default.KeyboardArrowDown,
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            // CONTENIDO COLAPSABLE
//            AnimatedVisibility(visible = expanded) {
//
//                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
//
//                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
//
//                    if (locationData.tieneCoordenadasValidas) {
//
//                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
//
//                            DataRow(
//                                label = "Latitud:",
//                                value = locationData.latitude.toString()
//                            )
//
//                            DataRow(
//                                label = "Longitud:",
//                                value = locationData.longitude.toString()
//                            )
//
//                            DataRow(
//                                label = "Altitud:",
//                                value = "${"%.2f".format(locationData.altitude)} m"
//                            )
//
//                            DataRow(
//                                label = "Margen de error:",
//                                value = "${"%.1f".format(locationData.accuracy)} m",
//                                valueColor = statusColor
//                            )
//                        }
//
//                    } else {
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 16.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//
//                            Text(
//                                "Esperando coordenadas del satélite...",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//
//                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 16.dp),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//
//                        OutlinedButton(
//                            onClick = onRecalculate,
//                            modifier = Modifier.weight(1f),
//                            enabled = !isCalculating,
//                            shape = RoundedCornerShape(10.dp)
//                        ) {
//                            Icon(
//                                Icons.Default.Refresh,
//                                contentDescription = null,
//                                modifier = Modifier.size(16.dp)
//                            )
//
//                            Spacer(Modifier.width(6.dp))
//
//                            Text("Recalcular")
//                        }
//
//                        Button(
//                            onClick = { onAccept(locationData) },
//                            modifier = Modifier.weight(1f),
//                            enabled = locationData.tieneCoordenadasValidas && !isCalculating,
//                            shape = RoundedCornerShape(10.dp)
//                        ) {
//
//                            Icon(
//                                Icons.Default.Check,
//                                contentDescription = null,
//                                modifier = Modifier.size(16.dp)
//                            )
//
//                            Spacer(Modifier.width(6.dp))
//
//                            Text("Aceptar")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun DataRow(
//    label: String,
//    value: String,
//    valueColor: Color = MaterialTheme.colorScheme.onSurface
//) {
//
//    Row(
//        Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//
//        Text(
//            text = label,
//            fontWeight = FontWeight.Medium
//        )
//
//        Text(
//            text = value,
//            color = valueColor,
//            fontFamily = FontFamily.Monospace
//        )
//    }
//}

