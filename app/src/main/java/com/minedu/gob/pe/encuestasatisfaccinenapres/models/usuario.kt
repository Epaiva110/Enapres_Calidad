package com.minedu.gob.pe.encuestasatisfaccinenapres.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.AppRepository
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Database.AppDataBase
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.UsuarioRoom
import com.minedu.gob.pe.encuestasatisfaccinenapres.ui.utils.CryptoManager

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.compareTo
import kotlin.div
import kotlin.text.get

@Serializable
data class Usuario(
    val usuario: String,
    val password: String,
    val activo: Boolean
)


class LoginRepository {

    suspend fun login(usuarioInput: String, passwordInput: String): LoginResult {
        return try {

            val supabase = createSupabaseClient(
                supabaseUrl = "https://vofuwtljegyjajwjzlll.supabase.co",
                supabaseKey = "sb_publishable_wWNTLpcXWobt0Bh7IMeopw_pJbxUGVi"
            ) {
                install(Postgrest)
            }

            val result = supabase
                .from("usuario")
                .select {
                    filter {
                        eq("usuario", usuarioInput)
                        eq("password", passwordInput)
                    }
                }
                .decodeList<Usuario>()

            if (result.isEmpty()) {
                return LoginResult.Error("Usuario o contraseña incorrectos")
            }

            val user = result.first()

            if (!user.activo) {
                return LoginResult.Inactive(user) // 👈 ya no es error técnico
            }

            LoginResult.Success(user)

        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Error de conexión")
        }
    }
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val loginRepository = LoginRepository()
    private val appRepository: AppRepository

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    init {
        val dao = AppDataBase.getDatabase(application).usuarioDao()
        appRepository = AppRepository(dao)
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

        val encryptedPass = CryptoManager.encrypt(password)

        val roomUser = UsuarioRoom(
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
