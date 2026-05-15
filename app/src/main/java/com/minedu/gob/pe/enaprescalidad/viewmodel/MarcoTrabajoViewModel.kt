package com.minedu.gob.pe.enaprescalidad.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoType
import com.minedu.gob.pe.enaprescalidad.data.repository.LoginResult
import com.minedu.gob.pe.enaprescalidad.data.repository.MarcoTrabajoRepository
import com.minedu.gob.pe.enaprescalidad.data.repository.MarcoTrabajoResultLocal
import com.minedu.gob.pe.enaprescalidad.ui.screens.login.sesion.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MarcoTrabajoViewModel @Inject constructor(
    private val repository: MarcoTrabajoRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)

    val state = _state.asStateFlow()

    private val _marcoTrabajo =
        MutableStateFlow<MarcoTrabajoResultLocal?>(null)

    val marcoTrabajo: StateFlow<MarcoTrabajoResultLocal?> =
        _marcoTrabajo



    fun getMarcoTrabajo(user: String, isOnline: Boolean) {
        viewModelScope.launch {
            repository.getMarcoTrabajo(user, isOnline)
        }
    }

//    fun loadMarcoTrabajo(user: String, tipo: String) {
//        viewModelScope.launch {
//            repository.getMarcoTrabajoTipo(user, tipo)
//                .collect {
//                    (marcoTrabajo as MutableStateFlow).value = it
//                }
//        }
//    }

    fun loadMarcoTrabajo(user: String) {
        viewModelScope.launch {
            repository.getMarcoTrabajoLocal(user)
                .collect { _marcoTrabajo.value = it }
        }
    }
}

sealed class MarcoTrabajoState {
    object Idle : MarcoTrabajoState()
    object Loading : MarcoTrabajoState()
    object Success : MarcoTrabajoState()
    data class Error(val message: String) : MarcoTrabajoState()
}


