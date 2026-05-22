package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

// ─────────────────────────────────────────────────────────────────────────────
// GpsQuestion.kt  (v2)
//
// Uso en DynamicQuestionAdapter (SurveyScreem.kt):
//   "gps" -> GpsQuestion(pregunta, valorActual as? String ?: "", onValueChange)
//
// Import a añadir en SurveyScreem.kt:
//   import com.minedu.gob.pe.enaprescalidad.surveys.ui.GpsQuestion
// ─────────────────────────────────────────────────────────────────────────────

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.ui.components.maps.LocationData
import com.minedu.gob.pe.enaprescalidad.ui.components.maps.MapMode
import com.minedu.gob.pe.enaprescalidad.ui.components.maps.MapScreenForSurvey
import com.minedu.gob.pe.enaprescalidad.ui.components.maps.MapViewer
import com.minedu.gob.pe.enaprescalidad.ui.components.maps.PrecisionQuality

// ─────────────────────────────────────────────────────────────────────────────
// Modelo interno para guardar toda la info GPS en las respuestas de la encuesta
// Se serializa a String con formato legible y parseable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Parsea el String almacenado en las respuestas de la encuesta.
 * Formato: "lat=X;lng=Y;alt=Z;acc=W;quality=Q"
 * Si es un formato antiguo "X, Y" también lo acepta (retrocompatibilidad).
 */
//fun parseGpsValue(raw: String): LocationData? {
//    if (raw.isBlank()) return null
//    return try {
//        if (raw.contains(";")) {
//            val map = raw.split(";").associate {
//                val (k, v) = it.split("=")
//                k to v
//            }
//            LocationData(
//                latitude  = map["lat"]?.toDouble()   ?: 0.0,
//                longitude = map["lng"]?.toDouble()   ?: 0.0,
//                altitude  = map["alt"]?.toDouble()   ?: 0.0,
//                accuracy  = map["acc"]?.toFloat()    ?: 0f,
//                quality   = runCatching {
//                    PrecisionQuality.valueOf(map["quality"] ?: "DESCONOCIDA")
//                }.getOrElse { PrecisionQuality.DESCONOCIDA },
//                calculationFinished = true
//            )
//        } else {
//            // Formato legacy "lat, lng"
//            val parts = raw.split(",").map { it.trim() }
//            LocationData(latitude = parts[0].toDouble(), longitude = parts[1].toDouble())
//        }
//    } catch (_: Exception) { null }
//}
//
//fun LocationData.toGpsString(): String =
//    "lat=$latitude;lng=$longitude;alt=$altitude;acc=$accuracy;quality=${quality.name}"
//
//// ─────────────────────────────────────────────────────────────────────────────
//// Componente principal
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun GpsQuestion(
//    pregunta: Pregunta,
//    rawValue: String,
//    onValueChange: (String, Any?) -> Unit,
//) {
//    val locationData = remember(rawValue) { parseGpsValue(rawValue) }
//    val tieneCoords  = locationData != null
//    var showMapDialog by remember { mutableStateOf(false) }
//
//    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
//
//        // ── Panel de datos capturados ─────────────────────────────────────────
//        AnimatedVisibility(
//            visible = tieneCoords,
//            enter   = fadeIn() + expandVertically(),
//            exit    = fadeOut() + shrinkVertically(),
//        ) {
//            locationData?.let { data ->
//                GpsCapturedPanel(
//                    data     = data,
//                    onClear  = { onValueChange(pregunta.variable, null) },
//                )
//            }
//        }
//
//        // ── Botón de acción ───────────────────────────────────────────────────
//        Button(
//            onClick  = { showMapDialog = true },
//            modifier = Modifier.fillMaxWidth(),
//            shape    = RoundedCornerShape(10.dp),
//            colors   = ButtonDefaults.buttonColors(
//                containerColor = if (tieneCoords)
//                    MaterialTheme.colorScheme.secondaryContainer
//                else
//                    MaterialTheme.colorScheme.primary,
//                contentColor   = if (tieneCoords)
//                    MaterialTheme.colorScheme.onSecondaryContainer
//                else
//                    Color.White,
//            )
//        ) {
//            Icon(
//                imageVector = if (tieneCoords) Icons.Default.EditLocation
//                else Icons.Default.MyLocation,
//                contentDescription = null,
//                modifier = Modifier.size(18.dp)
//            )
//            Spacer(Modifier.width(8.dp))
//            Text(
//                text       = if (tieneCoords) "Recapturar ubicación" else "Capturar ubicación GPS",
//                fontWeight = FontWeight.SemiBold,
//            )
//        }
//
//        // Texto de ayuda para modo manual
//        if (pregunta.allow_manual == true && !tieneCoords) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
//                modifier = Modifier.padding(horizontal = 2.dp)
//            ) {
//                Icon(
//                    Icons.Default.TouchApp,
//                    contentDescription = null,
//                    modifier = Modifier.size(13.dp),
//                    tint = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                Text(
//                    "También puedes tocar el mapa para indicar la posición manualmente.",
//                    fontSize = 11.sp,
//                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
//                )
//            }
//        }
//    }
//
//    //---------------------------------------------------------------------------
//
////    if (tieneCoords) {
////        var showViewer by remember { mutableStateOf(false) }
////
////        TextButton(onClick = { showViewer = true }) {
////            Icon(Icons.Default.Map, null, Modifier.size(14.dp))
////            Spacer(Modifier.width(4.dp))
////            Text("Ver en mapa")
////        }
////
////        if (showViewer) {
////            Dialog(onDismissRequest = { showViewer = false }, ...) {
////                MapViewer(locationData = locationData!!, modifier = Modifier.fillMaxSize())
////            }
////        }
////    }
//
//    // ── Dialog de pantalla completa ───────────────────────────────────────────
//    if (showMapDialog) {
//        Dialog(
//            onDismissRequest = { showMapDialog = false },
//            properties = DialogProperties(
//                usePlatformDefaultWidth = false,
//                dismissOnBackPress      = true,
//                dismissOnClickOutside   = false,
//            )
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(MaterialTheme.colorScheme.background)
//            ) {
//                MapScreenForSurvey(
//                    initialMode = MapMode.AUTO,
//                    allowManual = pregunta.allow_manual ?: false,
//                    onAccepted   = { data ->
//                        onValueChange(pregunta.variable, data.toGpsString())
//                        showMapDialog = false
//                    },
//                    onDismiss    = { showMapDialog = false },
//                )
//            }
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────────────
//// Panel de datos GPS ya capturados
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//private fun GpsCapturedPanel(data: LocationData, onClear: () -> Unit) {
//    val (qualityColor, qualityLabel) = when (data.quality) {
//        PrecisionQuality.EXCELENTE  -> Color(0xFF10B981) to "Excelente (<10 m)"
//        PrecisionQuality.REGULAR    -> Color(0xFFF59E0B) to "Regular (<50 m)"
//        PrecisionQuality.DEFICIENTE -> Color(0xFFEF4444) to "Deficiente (>50 m)"
//        PrecisionQuality.DESCONOCIDA-> Color(0xFF6B7280) to "Manual"
//    }
//
//    Surface(
//        shape = RoundedCornerShape(12.dp),
//        color = qualityColor.copy(alpha = 0.08f),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//
//            // Encabezado
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(6.dp)
//                ) {
//                    Icon(
//                        Icons.Default.LocationOn,
//                        contentDescription = null,
//                        tint = qualityColor,
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Text(
//                        "Ubicación capturada",
//                        fontSize   = 13.sp,
//                        fontWeight = FontWeight.Bold,
//                        color      = qualityColor,
//                    )
//                }
//
//                // Badge de calidad
//                Surface(
//                    shape = RoundedCornerShape(20.dp),
//                    color = qualityColor.copy(alpha = 0.15f),
//                ) {
//                    Text(
//                        qualityLabel,
//                        fontSize = 10.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = qualityColor,
//                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
//                    )
//                }
//            }
//
//            HorizontalDivider(color = qualityColor.copy(alpha = 0.20f))
//
//            // Datos en grid 2x2
//            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                    GpsDataCell("Latitud",  "%.6f".format(data.latitude),  Modifier.weight(1f))
//                    GpsDataCell("Longitud", "%.6f".format(data.longitude), Modifier.weight(1f))
//                }
//                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                    GpsDataCell("Altitud",  "${"%.1f".format(data.altitude)} m", Modifier.weight(1f))
//                    GpsDataCell("Precisión","${"%.1f".format(data.accuracy)} m",
//                        Modifier.weight(1f), valueColor = qualityColor)
//                }
//            }
//
//            // Botón limpiar (pequeño, al final)
//            TextButton(
//                onClick  = onClear,
//                modifier = Modifier.align(Alignment.End),
//                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
//            ) {
//                Icon(Icons.Default.DeleteOutline, null, Modifier.size(14.dp))
//                Spacer(Modifier.width(4.dp))
//                Text("Borrar y recapturar", fontSize = 11.sp)
//            }
//        }
//    }
//}
//
//@Composable
//private fun GpsDataCell(
//    label: String,
//    value: String,
//    modifier: Modifier = Modifier,
//    valueColor: Color = MaterialTheme.colorScheme.onSurface,
//) {
//    Column(modifier = modifier) {
//        Text(
//            label,
//            fontSize = 10.sp,
//            color    = MaterialTheme.colorScheme.onSurfaceVariant,
//            fontWeight = FontWeight.Medium,
//        )
//        Text(
//            value,
//            fontSize   = 12.sp,
//            fontFamily = FontFamily.Monospace,
//            fontWeight = FontWeight.SemiBold,
//            color      = valueColor,
//        )
//    }
//}

import com.minedu.gob.pe.enaprescalidad.ui.components.maps.*


// ─────────────────────────────────────────────────────────────────────────────
// Serialización / deserialización
// ─────────────────────────────────────────────────────────────────────────────

fun parseGpsValue(raw: String): LocationData? {
    if (raw.isBlank()) return null
    return try {
        if (raw.contains(";")) {
            val map = raw.split(";").associate {
                val (k, v) = it.split("=")
                k to v
            }
            LocationData(
                latitude  = map["lat"]?.toDouble()  ?: 0.0,
                longitude = map["lng"]?.toDouble()  ?: 0.0,
                altitude  = map["alt"]?.toDouble()  ?: 0.0,
                accuracy  = map["acc"]?.toFloat()   ?: 0f,
                quality   = runCatching {
                    PrecisionQuality.valueOf(map["quality"] ?: "DESCONOCIDA")
                }.getOrElse { PrecisionQuality.DESCONOCIDA },
                calculationFinished = true,
            )
        } else {
            // Formato legacy "lat, lng"
            val parts = raw.split(",").map { it.trim() }
            LocationData(latitude = parts[0].toDouble(), longitude = parts[1].toDouble())
        }
    } catch (_: Exception) { null }
}

fun LocationData.toGpsString(): String =
    "lat=$latitude;lng=$longitude;alt=$altitude;acc=$accuracy;quality=${quality.name}"

// ─────────────────────────────────────────────────────────────────────────────
// GpsQuestion
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GpsQuestion(
    pregunta     : Pregunta,
    rawValue     : String,
    onValueChange: (String, Any?) -> Unit,
) {
    val locationData = remember(rawValue) { parseGpsValue(rawValue) }
    val tieneCoords  = locationData != null

    var showMapCapture by remember { mutableStateOf(false) }
    var showMapViewer  by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        // ── Panel de datos capturados ─────────────────────────────────────────
        AnimatedVisibility(
            visible = tieneCoords,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically(),
        ) {
            locationData?.let { data ->
                GpsCapturedPanel(
                    data    = data,
                    onClear = { onValueChange(pregunta.variable, null) },
                )
            }
        }

        // ── Botón capturar / recapturar ───────────────────────────────────────
        Button(
            onClick  = { showMapCapture = true },
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = if (tieneCoords)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primary,
                contentColor   = if (tieneCoords)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    Color.White,
            )
        ) {
            Icon(
                imageVector        = if (tieneCoords) Icons.Default.EditLocation
                else Icons.Default.MyLocation,
                contentDescription = null,
                modifier           = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text       = if (tieneCoords) "Recapturar ubicación" else "Capturar ubicación GPS",
                fontWeight = FontWeight.SemiBold,
            )
        }

        // ── Botón ver en mapa (solo si ya hay coordenadas) ────────────────────
        AnimatedVisibility(
            visible = tieneCoords,
            enter   = fadeIn() + expandVertically(),
            exit    = fadeOut() + shrinkVertically(),
        ) {
            OutlinedButton(
                onClick  = { showMapViewer = true },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = "Ver ubicación en mapa",
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // ── Texto de ayuda para modo manual ───────────────────────────────────
        if (pregunta.allow_manual == true && !tieneCoords) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier              = Modifier.padding(horizontal = 2.dp),
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    //modifier = Modifier.size(13.dp),
                    tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "También puedes tocar el mapa o ingresar coordenadas manualmente.",
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    // ── Dialog de captura (AUTO / MANUAL) ─────────────────────────────────────
    if (showMapCapture) {
        Dialog(
            onDismissRequest = { showMapCapture = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress      = true,
                dismissOnClickOutside   = false,
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MapScreenForSurvey(
                    initialMode = MapMode.AUTO,
                    allowManual = pregunta.allow_manual ?: false,
                    onAccepted  = { data ->
                        onValueChange(pregunta.variable, data.toGpsString())
                        showMapCapture = false
                    },
                    onDismiss   = { showMapCapture = false },
                )
            }
        }
    }

    // ── Dialog de solo lectura (ver coordenada capturada) ─────────────────────
    if (showMapViewer && locationData != null) {
        MapViewerDialog(
            locationData = locationData,
            onDismiss    = { showMapViewer = false },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Panel de datos GPS ya capturados
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GpsCapturedPanel(data: LocationData, onClear: () -> Unit) {
    val (qualityColor, qualityLabel) = when (data.quality) {
        PrecisionQuality.EXCELENTE   -> Color(0xFF10B981) to "Excelente (<10 m)"
        PrecisionQuality.REGULAR     -> Color(0xFFF59E0B) to "Regular (<50 m)"
        PrecisionQuality.DEFICIENTE  -> Color(0xFFEF4444) to "Deficiente (>50 m)"
        PrecisionQuality.DESCONOCIDA -> Color(0xFF6B7280) to "Manual"
    }

    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = qualityColor.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Encabezado con badge de calidad
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint     = qualityColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        "Ubicación capturada",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = qualityColor,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = qualityColor.copy(alpha = 0.15f),
                ) {
                    Text(
                        qualityLabel,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = qualityColor,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            HorizontalDivider(color = qualityColor.copy(alpha = 0.20f))

            // Grid 2x2 de datos
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GpsDataCell("Latitud",  "%.6f".format(data.latitude),  Modifier.weight(1f))
                    GpsDataCell("Longitud", "%.6f".format(data.longitude), Modifier.weight(1f))
                }
                if (data.quality != PrecisionQuality.DESCONOCIDA) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        GpsDataCell("Altitud",   "${"%.1f".format(data.altitude)} m",  Modifier.weight(1f))
                        GpsDataCell("Precisión", "${"%.1f".format(data.accuracy)} m",  Modifier.weight(1f), valueColor = qualityColor)
                    }
                }
            }

            // Botón limpiar
            TextButton(
                onClick        = onClear,
                modifier       = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Icon(Icons.Default.DeleteOutline, null, Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Eliminar Coordenada", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun GpsDataCell(
    label     : String,
    value     : String,
    modifier  : Modifier = Modifier,
    valueColor: Color    = MaterialTheme.colorScheme.onSurface,
) {
    Column(modifier = modifier) {
        Text(
            label,
            fontSize   = 10.sp,
            color      = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        Text(
            value,
            fontSize   = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor,
        )
    }
}