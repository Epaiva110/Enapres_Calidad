package com.minedu.gob.pe.encuestasatisfaccinenapres.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.navigation3.runtime.NavBackStack
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.Routes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment


import androidx.navigation3.runtime.NavKey
import com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.core.ex.navigateTo
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.utils.SetupMapSystemUI

@Composable
fun ConglomeradoScreen(backStack: NavBackStack<NavKey>) {
    val years = listOf("Seleccione", "2024", "2025")
    val months = listOf("Seleccione", "Enero", "Febrero", "Marzo")
    val samples = listOf("Seleccione", "Muestra 1", "Muestra 2")

    var selectedYear by remember { mutableStateOf(years[0]) }
    var selectedMonth by remember { mutableStateOf(months[0]) }
    var selectedSample by remember { mutableStateOf(samples[0]) }

    val conglomerados = listOf(
        "Conglomerado 001", "Conglomerado 002",
        "Conglomerado 003", "Conglomerado 004"
    )

    //SetupMapSystemUI()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            // Añadimos esto para que no se pegue a los bordes del sistema
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Text(
            text = "Selección de Muestra",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- SELECTORES ---
        DropdownField("Año", years, selectedYear) { selectedYear = it }
        Spacer(Modifier.height(10.dp))
        DropdownField("Mes", months, selectedMonth) { selectedMonth = it }
        Spacer(Modifier.height(10.dp))
        DropdownField("Muestra", samples, selectedSample) { selectedSample = it }

        Spacer(Modifier.height(24.dp))
        Text("Listado de Conglomerados", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // --- LISTADO ---
        // El weight(1f) es clave para que el botón de abajo no desaparezca
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(conglomerados) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { /* Podrías navegar al mapa desde aquí también */ },
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(item)
                    }
                }
            }
        }

        // --- BOTÓN DE NAVEGACIÓN ---
        Button(
            onClick = { backStack.navigateTo(Routes.Map) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ir al Mapa")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}