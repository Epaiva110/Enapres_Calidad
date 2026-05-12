package com.minedu.gob.pe.enaprescalidad.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.repository.LoginResult
import com.minedu.gob.pe.enaprescalidad.data.repository.UsuarioRepository
import com.minedu.gob.pe.enaprescalidad.ui.screens.login.sesion.SessionManager
import com.minedu.gob.pe.enaprescalidad.ui.screens.login.sesion.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UsuarioRepository ,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)

    val state = _state.asStateFlow()

    val currentUser = sessionManager.user


    // ── Form state vive aquí, no en la Screen ──
    var codsup by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    val isLoginEnabled: Boolean
        get() = codsup.isNotBlank() && password.length >= 4 && _state.value !is LoginState.Loading

    fun onCodsupChange(value: String) { codsup = value }
    fun onPasswordChange(value: String) { password = value }

    fun login(isOnline: Boolean) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            val result = repository.login(codsup, password, isOnline)

            _state.value = when (result) {
                is LoginResult.Success -> {
                    val user = result.user

                    sessionManager.setUser(
                        UserSession(
                            user = user.user,
                            user_name = user.user_name,
                            role = user.role
                        )
                    )

                    LoginState.Success
                }
                is LoginResult.Inactive -> LoginState.Error("Usuario inactivo")
                is LoginResult.Error -> LoginState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            _state.value = LoginState.Idle
        }
        codsup = ""
        password = ""
        sessionManager.clear()
    }


}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}