package com.minedu.gob.pe.encuestasatisfaccinenapres.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from

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

class LoginRepository {
    suspend fun login(usuarioInput: String, passwordInput: String): Result<Usuario> {
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
                return Result.failure(Exception("Usuario o contraseña incorrectos"))
            }

            val user = result.first()

            if (!user.activo) {
                return Result.failure(Exception("Usuario inactivo"))
            }

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class LoginViewModel : ViewModel() {

    private val repository = LoginRepository()

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun login(usuario: String, password: String) {

        viewModelScope.launch {
            _state.value = LoginState.Loading

            val result = repository.login(usuario, password)

            result
                .onSuccess {
                    _state.value = LoginState.Success(it.usuario)
                }
                .onFailure {
                    _state.value = LoginState.Error(it.message ?: "Error desconocido")
                }
        }
    }
    fun logout() {
        _state.value = LoginState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

