package com.minedu.gob.pe.enaprescalidad.ui.screens.main.features.Maintance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey

@Composable
fun MaintanceScren(route: NavKey) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pantalla [${route::class.simpleName}] en desarrollo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}