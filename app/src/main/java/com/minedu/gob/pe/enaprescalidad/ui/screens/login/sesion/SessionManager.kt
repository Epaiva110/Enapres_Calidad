package com.minedu.gob.pe.enaprescalidad.ui.screens.login.sesion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.minedu.gob.pe.enaprescalidad.ui.screens.main.MainUiState
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class SessionManager @Inject constructor() {

    private val _user = MutableStateFlow<UserSession?>(null)
    val user: StateFlow<UserSession?> = _user

    fun setUser(user: UserSession) {
        _user.value = user
    }

    fun clear() {
        _user.value = null
    }
}

data class UserSession(
    val id: Int,
    val user: String,
    val user_name: String,
    val role: String,
    val last_connection: Long
)