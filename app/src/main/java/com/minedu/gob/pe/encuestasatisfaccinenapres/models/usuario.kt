package com.minedu.gob.pe.encuestasatisfaccinenapres.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Database.AppDataBase
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.UsuarioEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.UsuarioRepository
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Online.Supabase.Repository.LoginRepository
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.utils.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val usuario: String,
    val password: String,
    val activo: Boolean
)




class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val loginRepository = LoginRepository()
    private val appRepository: UsuarioRepository

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    init {
        val dao = AppDataBase.getDatabase(application).usuarioDao()
        appRepository = UsuarioRepository(dao)
    }

    fun login(usuario: String, password: String, isOnline: Boolean) {

        viewModelScope.launch {
            _state.value = LoginState.Loading

            if (isOnline) {

                when (val result = loginRepository.login(usuario, password)) {

                    is LoginResult.Success -> {

                        saveUser(result.user, password)
                        _state.value = LoginState.Success(result.user.usuario)
                    }

                    is LoginResult.Inactive -> {

                        // 🔥 IMPORTANTE: igual guardamos en Room
                        saveUser(result.user, password)
                        _state.value = LoginState.Error("Usuario inactivo")
                    }

                    is LoginResult.Error -> {
                        _state.value = LoginState.Error(result.message)
                    }
                }

            } else {
                loginOffline(usuario, password)
            }
        }
    }
    fun logout() {
        viewModelScope.launch {
            //appRepository.logout() // limpia Room
            _state.value = LoginState.Idle
        }
    }

    private suspend fun saveUser(user: Usuario, password: String) {

        val encryptedPass = if (user.activo) {
            CryptoManager.encrypt(password)
        } else {
            "PASSWORD IS NOT AVAILABLE"
        }

        val roomUser = UsuarioEntity(
            usuario = user.usuario,
            passwordEncrypted = encryptedPass,
            activo = user.activo,
            lastUpdated = System.currentTimeMillis()
        )

        appRepository.save(roomUser)
    }

    private suspend fun loginOffline(usuario: String, password: String) {

        val local = appRepository.get(usuario)

        if (local == null) {
            _state.value = LoginState.Error("Sin datos offline")
            return
        }

        val days = (System.currentTimeMillis() - local.lastUpdated) / (1000 * 60 * 60 * 24)

        if (days > 30) {
            _state.value = LoginState.Error("Sesión expirada (30 días)")
            return
        }

        val decrypted = CryptoManager.decrypt(local.passwordEncrypted)

        if (decrypted == password) {

            if (!local.activo) {
                _state.value = LoginState.Error("Usuario inactivo")
            } else {
                _state.value = LoginState.Success(local.usuario)
            }

        } else {
            _state.value = LoginState.Error("Credenciales incorrectas")
        }
    }

}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class LoginResult {
    data class Success(val user: Usuario) : LoginResult()
    data class Inactive(val user: Usuario) : LoginResult()
    data class Error(val message: String) : LoginResult()
}