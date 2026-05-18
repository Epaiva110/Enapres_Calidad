package com.minedu.gob.pe.enaprescalidad.ui.navigation

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class NavigationManager @Inject constructor() {

    private val _currentScreen = MutableStateFlow("")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screenId: String) {
        _currentScreen.value = screenId
    }
}
