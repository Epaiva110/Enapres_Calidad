package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.welcome

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.ui.theme.SupNacTheme

@Composable
fun WelcomeScreen(
    codsup: String,
    onStart: () -> Unit
) {
    // Estado simple para evitar múltiples clics en el botón mientras carga la siguiente vista
    var isLoading by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Cabecera con énfasis en el éxito del login
                StatusHeader()

                // Información del Supervisor destacada
                UserCard(codsup)

                // Botón de acción con estado de carga
                StartButton(
                    isLoading = isLoading,
                    onClick = {
                        isLoading = true
                        onStart()
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Un anillo de éxito alrededor del icono
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "¡Acceso Exitoso!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "ENAPRES: Control de Calidad",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UserCard(codsup: String) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "SESIÓN ACTIVA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Supervisor $codsup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StartButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(56.dp),
        shape = CircleShape
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                "COMENZAR JORNADA",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Preview(
    name = "Modo Oscuro",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_7
)
@Composable
fun WelcomeScreenDarkPreview() {
    SupNacTheme {
        WelcomeScreen(
            codsup = "2024-001",
            onStart = { }
        )
    }
}


@Preview(
    name = "Modo Claro",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
fun WelcomeScreenPreview() {
    SupNacTheme {
        // Le pasamos un código de prueba (mock)
        WelcomeScreen(
            codsup = "2024-001",
            onStart = { /* No hace nada en la previa */ }
        )
    }
}

@Preview(name = "Texto Largo", showBackground = true)
@Composable
fun WelcomeLongNamePreview() {
    SupNacTheme {
        WelcomeScreen(codsup = "SUP-LIMA-2026-AREA-NORTE", onStart = {})
    }
}