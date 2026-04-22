package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(
    codsup: String,
    onStart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeHeader()

            Spacer(Modifier.height(24.dp))

            WelcomeUserInfo(codsup)

            Spacer(Modifier.height(32.dp))

            WelcomeAction(onStart)
        }
    }
}

@Composable
fun WelcomeHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Sistema de Control de Calidad de Datos ENAPRES",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WelcomeUserInfo(codsup: String) {
    Text(
        text = "Supervisor: $codsup",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true)
@Composable
fun WelcomeUserInfoPreview() {
    WelcomeUserInfo(codsup = "SUP123")
}

@Composable
fun WelcomeAction(onStart: () -> Unit) {
    Button(
        onClick = onStart,
        shape = MaterialTheme.shapes.medium
    ) {
        Text("Ingresar")
    }
}