package com.minedu.gob.pe.enaprescalidad.ui.components.maps

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun AirplaneDialog(onGoToSettings: () -> Unit, onContinue: () -> Unit) {
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
fun TimeoutDialog(onRetry: () -> Unit, onAcceptManual: () -> Unit) {
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


@Composable
fun RationaleDialog(
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