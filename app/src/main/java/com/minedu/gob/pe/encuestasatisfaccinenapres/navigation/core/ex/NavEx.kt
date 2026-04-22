package com.minedu.gob.pe.encuestasatisfaccinenapres.navigation.core.ex

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.navigateTo(screen: NavKey) {
    add(screen)
}

fun NavBackStack<NavKey>.back() {
    if (isEmpty()) return
    removeLastOrNull()
}

// Función extra usando tus métodos para evitar volver atrás (Splash/Login)
fun NavBackStack<NavKey>.replace(screen: NavKey) {
    if (isNotEmpty()) removeLastOrNull()
    add(screen)
}