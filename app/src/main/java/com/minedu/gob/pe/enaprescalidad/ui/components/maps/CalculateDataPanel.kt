package com.minedu.gob.pe.enaprescalidad.ui.components.maps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun Prueba () {
    CalculatedDataPanel(
        LocationData(altitude = 99.0, longitude = 99.1, latitude = 99.2, accuracy = 99f, quality = PrecisionQuality.EXCELENTE),
        false,180,{},{}
    )
}


@Composable
fun CalculatedDataPanel(
    locationData: LocationData,
    isCalculating: Boolean,
    remainingSeconds: Int,
    onRecalculate: () -> Unit,
    onAccept: (LocationData) -> Unit
) {

    val precisionUi = remember(locationData.quality) {
        locationData.quality.toUi()
    }

    var expanded by rememberSaveable {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            PanelHeader(
                expanded = expanded,
                isCalculating = isCalculating,
                remainingSeconds = remainingSeconds,
                precisionUi = precisionUi,
                onToggle = {
                    expanded = !expanded
                }
            )

            AnimatedVisibility(
                visible = expanded
            ) {

                if (locationData.tieneCoordenadasValidas) {

                    Column {

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        CoordinatesSection(
                            locationData = locationData,
                            statusColor = precisionUi.color
                        )
                    }

                } else {

                    WaitingCoordinates()
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            ActionButtons(
                enabledAccept = locationData.tieneCoordenadasValidas && !isCalculating,
                enabledRecalculate = !isCalculating,
                onAccept = {
                    onAccept(locationData)
                },
                onRecalculate = onRecalculate
            )
        }
    }
}

@Composable
private fun PanelHeader(
    expanded: Boolean,
    isCalculating: Boolean,
    remainingSeconds: Int,
    precisionUi: PrecisionUi,
    onToggle: () -> Unit
) {

    val timeFormatted by remember(remainingSeconds) {

        derivedStateOf {
            "%02d:%02d".format(
                remainingSeconds / 60,
                remainingSeconds % 60
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isCalculating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = precisionUi.color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buscando ($timeFormatted)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = precisionUi.color,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Ubicado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                color = precisionUi.color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {

                Text(
                    text = precisionUi.label,
                    color = precisionUi.color,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector =
                    if (expanded)
                        Icons.Default.KeyboardArrowDown
                    else
                        Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CoordinatesSection(
    locationData: LocationData,
    statusColor: Color
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        CoordinateRow(
            label = "Latitud",
            value = locationData.latitude.toString()
        )

        CoordinateRow(
            label = "Longitud",
            value = locationData.longitude.toString()
        )

        CoordinateRow(
            label = "Altitud",
            value = "${"%.2f".format(locationData.altitude)} m"
        )

        CoordinateRow(
            label = "Margen de error",
            value = "${"%.1f".format(locationData.accuracy)} m",
            valueColor = statusColor,
            boldValue = true
        )
    }
}

@Composable
private fun CoordinateRow(
    label: String,
    value: String,
    valueColor: Color = LocalContentColor.current,
    boldValue: Boolean = false
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (boldValue) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun WaitingCoordinates() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "Esperando coordenadas del satélite...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtons(
    enabledAccept: Boolean,
    enabledRecalculate: Boolean,
    onAccept: () -> Unit,
    onRecalculate: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        OutlinedButton(
            onClick = onRecalculate,
            enabled = enabledRecalculate,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Recalcular",
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick = onAccept,
            enabled = enabledAccept,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {

            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Aceptar",
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Immutable
data class PrecisionUi(
    val color: Color,
    val label: String
)

private fun PrecisionQuality.toUi(): PrecisionUi {
    return when (this) {

        PrecisionQuality.EXCELENTE -> {
            PrecisionUi(
                color = Color(0xFF10B981),
                label = "Precisión Excelente (<10m)"
            )
        }

        PrecisionQuality.REGULAR -> {
            PrecisionUi(
                color = Color(0xFFF59E0B),
                label = "Precisión Regular (<50m)"
            )
        }

        PrecisionQuality.DEFICIENTE -> {
            PrecisionUi(
                color = Color(0xFFEF4444),
                label = "Precisión Deficiente (>50m)"
            )
        }

        PrecisionQuality.DESCONOCIDA -> {
            PrecisionUi(
                color = Color.Gray,
                label = "Buscando señal..."
            )
        }
    }
}
