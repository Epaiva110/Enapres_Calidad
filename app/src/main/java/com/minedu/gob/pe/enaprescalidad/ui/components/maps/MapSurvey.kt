package com.minedu.gob.pe.enaprescalidad.ui.components.maps



import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

enum class MapMode { AUTO, MANUAL }

// ─────────────────────────────────────────────────────────────────────────────
// Composable principal
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Pantalla de mapa reutilizable.
 *
 * @param title         Título que se muestra en la TopBar.
 * @param initialMode   Modo inicial. null = AUTO si allowManual=false, AUTO por defecto.
 * @param allowManual   Muestra el toggle y permite cambiar a modo manual.
 * @param onAccepted    Callback con [LocationData] cuando el usuario acepta la ubicación.
 * @param onDismiss     Callback al pulsar "Cerrar" sin aceptar.
 * @param viewModel     ViewModel del mapa (se puede inyectar para testing o reutilizar estado).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenForSurvey(
    title        : String      = "Capturar ubicación",
    initialMode  : MapMode?    = null,
    allowManual  : Boolean     = false,
    onAccepted   : (LocationData) -> Unit,
    onDismiss    : () -> Unit,
    viewModel    : MapViewModel = viewModel(),
) {
    // Modo activo: si no se especifica, siempre AUTO
    var mode by remember { mutableStateOf(initialMode ?: MapMode.AUTO) }

    // Al cambiar a AUTO, reiniciamos el marker manual
    var markerManual by remember { mutableStateOf<LatLng?>(null) }

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
                                if (mode == MapMode.AUTO) markerManual = null
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
            // Animación entre modos
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                },
                label = "map_mode_transition"
            ) { currentMode ->
                when (currentMode) {

                    // ── MODO AUTOMÁTICO ─────────────────────────────────────
                    MapMode.AUTO -> {
                        MapScreen(
                            viewModel            = viewModel,
                            showCoordenadas      = true,
                            calculateLocation    = true,
                            focusOnCurrentLocation = true,
                            onLocationAccepted   = { locationData ->
                                onAccepted(locationData)
                            }
                        )
                    }

                    // ── MODO MANUAL ─────────────────────────────────────────
                    MapMode.MANUAL -> {
                        ManualMarkerMap(
                            marker         = markerManual,
                            onMarkerPlaced = { markerManual = it },
                            onMarkerCleared= { markerManual = null },
                            onAccepted     = { latLng ->
                                onAccepted(
                                    LocationData(
                                        latitude  = latLng.latitude,
                                        longitude = latLng.longitude,
                                        altitude  = 0.0,
                                        accuracy  = 0f,
                                        quality   = PrecisionQuality.DESCONOCIDA,
                                        calculationFinished = true,
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Toggle Auto / Manual en la TopBar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ModeToggle(current: MapMode, onToggle: () -> Unit) {

    val isManual = current == MapMode.MANUAL

    Surface(
        onClick       = onToggle,
        shape         = RoundedCornerShape(20.dp),
        color         = if (isManual) {MaterialTheme.colorScheme.secondaryContainer} else {MaterialTheme.colorScheme.primaryContainer},
        modifier      = Modifier.padding(end = 8.dp)
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isManual) Icons.Default.TouchApp else Icons.Default.GpsFixed,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isManual) MaterialTheme.colorScheme.onSecondaryContainer
                else         MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text       = if (isManual) "Manual" else "Auto GPS",
                fontSize   = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color      = if (isManual) MaterialTheme.colorScheme.onSecondaryContainer
                else         MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mapa de selección manual con Marker
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ManualMarkerMap(
    marker         : LatLng?,
    onMarkerPlaced : (LatLng) -> Unit,
    onMarkerCleared: () -> Unit,
    onAccepted     : (LatLng) -> Unit,
) {
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.0664, -77.0428), 13f)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier            = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            onMapClick          = { latLng -> onMarkerPlaced(latLng) },
            uiSettings          = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled     = true,
            )
        ) {
            marker?.let { pos ->
                Marker(
                    state   = MarkerState(position = pos),
                    title   = "Ubicación seleccionada",
                    snippet = "${"%.6f".format(pos.latitude)}, ${"%.6f".format(pos.longitude)}",
                    icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                    draggable = true,
                    onClick = { false }, // permite que se muestre el snippet
                )
            }
        }

        // ── Instrucción flotante (visible mientras no hay marker) ─────────────
        AnimatedVisibility(
            visible  = marker == null,
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

        // ── Panel inferior con coords y acciones ──────────────────────────────
        AnimatedVisibility(
            visible  = marker != null,
            enter    = slideInVertically { it } + fadeIn(tween(200)),
            exit     = slideOutVertically { it } + fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) {
            marker?.let { pos ->
                ManualLocationPanel(
                    pos        = pos,
                    onClear    = onMarkerCleared,
                    onAccepted = onAccepted,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Panel de confirmación de ubicación manual
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ManualLocationPanel(
    pos       : LatLng,
    onClear   : () -> Unit,
    onAccepted: (LatLng) -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado
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
                    "Ubicación seleccionada",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Coordenadas
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ManualCoordCell("Latitud",  "%.6f".format(pos.latitude),  Modifier.weight(1f))
                ManualCoordCell("Longitud", "%.6f".format(pos.longitude), Modifier.weight(1f))
            }

            Spacer(Modifier.height(4.dp))

            // Nota de precisión manual
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    "Ubicación indicada manualmente — sin altitud ni precisión GPS.",
                    fontSize = 10.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Acciones
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
                    onClick  = { onAccepted(pos) },
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
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