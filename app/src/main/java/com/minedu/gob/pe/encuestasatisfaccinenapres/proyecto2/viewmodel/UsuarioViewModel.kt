package com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.MuestraEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto.MuestraDto
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto.UsuarioDto
import com.example.userapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UsuarioUiState(
    val usuarios: List<UsuarioEntity> = emptyList(),
    val muestras: List<MuestraEntity> = emptyList(),
    val selectedUsuario: UsuarioEntity? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class UsuarioViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuarioUiState())
    val uiState: StateFlow<UsuarioUiState> = _uiState.asStateFlow()

    init {
        observeUsuarios()
        //syncUsuarios()
    }

    private fun observeUsuarios() {
        viewModelScope.launch {
            repository.usuarios.collect { list ->
                _uiState.update { it.copy(usuarios = list) }
            }
        }
    }

//    fun syncUsuarios() {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true, error = null) }
//            runCatching { repository.syncUsuarios() }
//                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
//            _uiState.update { it.copy(isLoading = false) }
//        }
//    }

//    fun selectUsuario(usuario: UsuarioEntity) {
//        _uiState.update { it.copy(selectedUsuario = usuario, muestras = emptyList()) }
//        viewModelScope.launch {
//            runCatching { repository.syncMuestras(usuario.usuario) }
//            repository.muestrasByUsuario(usuario.usuario).collect { muestras ->
//                _uiState.update { it.copy(muestras = muestras) }
//            }
//        }
//    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedUsuario = null, muestras = emptyList()) }
    }

    // ── Usuario CRUD ──────────────────────────────────────────────
    fun addUsuario(usuario: String, password: String, estado: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                repository.insertUsuario(UsuarioDto(usuario = usuario, password = password, estado = estado))
            }.onSuccess {
                _uiState.update { it.copy(successMessage = "Usuario creado") }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateUsuario(entity: UsuarioEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.updateUsuario(entity) }
                .onSuccess { _uiState.update { it.copy(successMessage = "Usuario actualizado") } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

//    fun deleteUsuario(entity: UsuarioEntity) {
//        viewModelScope.launch {
//            _uiState.update { it.copy(isLoading = true) }
//            runCatching { repository.deleteUsuario(entity) }
//                .onSuccess { _uiState.update { it.copy(successMessage = "Usuario eliminado") } }
//                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
//            _uiState.update { it.copy(isLoading = false) }
//        }
//    }

    // ── Muestra CRUD ──────────────────────────────────────────────
    fun addMuestra(usuarioId: String, nombreMuestra: String) {
        viewModelScope.launch {
            runCatching {
                repository.insertMuestra(MuestraDto(usuario = usuarioId, idcong = nombreMuestra))
            }.onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun deleteMuestra(entity: MuestraEntity) {
        viewModelScope.launch {
            runCatching { repository.deleteMuestra(entity) }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(error = null, successMessage = null) }
}
