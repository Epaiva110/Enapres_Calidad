package com.minedu.gob.pe.enaprescalidad.ui.navigation
import kotlinx.serialization.Serializable
/**
 * Rutas tipadas de la app.
 * Cada objeto/clase aquí es una pantalla navegable.
 * Agregar una pantalla nueva = agregar un objeto aquí + su entry en AppNavigation.
 */
sealed interface Screen {

//    @Serializable
//    data object Main : Screen

    // --- Pantallas del contenido principal (dentro del sidebar layout) ---
    @Serializable
    data object Home : Screen

    @Serializable
    data object Analytics : Screen

    @Serializable
    data object Settings : Screen
}