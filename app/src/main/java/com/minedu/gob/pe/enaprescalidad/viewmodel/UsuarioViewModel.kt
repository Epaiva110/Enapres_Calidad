package com.minedu.gob.pe.enaprescalidad.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.repository.LoginResult
import com.minedu.gob.pe.enaprescalidad.data.repository.MuestraConglomeradoRepository
import com.minedu.gob.pe.enaprescalidad.data.repository.MuestraResult
import com.minedu.gob.pe.enaprescalidad.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UsuarioRepository ,
    private val repositoryC: MuestraConglomeradoRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state = _state.asStateFlow()

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

            if (result is LoginResult.Success) {

                repositoryC.syncMuestraConglomerado(codsup, isOnline)
                Log.d("SYNCHHHHH", "Muestra sincronizada")
            }

            _state.value = when (result) {
                is LoginResult.Success -> LoginState.Success(result.user.usuario)
                is LoginResult.Inactive -> LoginState.Error("Usuario inactivo")
                is LoginResult.Error -> LoginState.Error(result.message)
            }
        }
    }

    //

    fun syncMuestraConglomerado(isOnline: Boolean) {

        viewModelScope.launch {
            try {
                repositoryC.syncMuestraConglomerado(codsup, isOnline)
            } catch (e: Exception) {
                Log.e("SYNCHHHHH", "Error sincronizando muestra", e)
            }
        }
//        viewModelScope.launch {
//
//            val result = repositoryC.syncMuestraConglomerado(codsup, isOnline)
//
////            // aquí decides qué haces con el resultado
////            when (result) {
////                is MuestraResult.Success -> {
////                    // actualizar UI state si tienes uno
////                }
////
////                is MuestraResult.Empty -> {
////                    // mostrar mensaje vacío
////                }
////
////                is MuestraResult.Error -> {
////                    // manejar error
////                }
////            }
//        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }

    fun resetInputs() {
        codsup = ""
        password = ""
    }

    fun logout() {
        viewModelScope.launch {
            _state.value = LoginState.Idle
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: String) : LoginState()
    data class Error(val message: String) : LoginState()
}