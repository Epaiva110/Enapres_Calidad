// SplashScreen.kt
package com.minedu.gob.pe.enaprescalidad.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.ui.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigate: (Routes) -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)

        // Aquí simulas la recuperación de sesión
        val isLogged = false
        "SUP-123" // Esto lo traerías de tu base de datos local

        if (isLogged) {
            // IMPORTANTE: Ahora Main requiere el ID
            onNavigate(Routes.MainDynamic)
        } else {
            onNavigate(Routes.Login)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Aquí podrías poner tu logo del INEI
            Icon(
                Icons.Default.Speed,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Cargando Sistema...",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}