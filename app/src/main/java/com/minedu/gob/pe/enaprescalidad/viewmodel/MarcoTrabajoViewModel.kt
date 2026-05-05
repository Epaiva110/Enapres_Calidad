package com.minedu.gob.pe.enaprescalidad.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoType
import com.minedu.gob.pe.enaprescalidad.data.repository.LoginResult
import com.minedu.gob.pe.enaprescalidad.data.repository.MarcoTrabajoRepository
import com.minedu.gob.pe.enaprescalidad.ui.screens.login.sesion.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class MarcoTrabajoViewModel @Inject constructor(
    private val repository: MarcoTrabajoRepository
) : ViewModel() {
    fun getMarcoTrabajo(user: String, isOnline: Boolean) {
        viewModelScope.launch {
            val result = repository.getMarcoTrabajo(user, isOnline)

            Log.i("Errorrrrrrrrrrrrrrrr : 00001",result.toString())
        }
    }
}


