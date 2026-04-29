package com.minedu.gob.pe.enaprescalidad.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncType
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.repository.LoginResult
import com.minedu.gob.pe.enaprescalidad.data.repository.MuestraConglomeradoRepository
import com.minedu.gob.pe.enaprescalidad.data.repository.MuestraResult
import com.minedu.gob.pe.enaprescalidad.data.repository.SyncStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//@HiltViewModel
//class MuestraConglomeradoViewModel @Inject constructor(
//    private val repository: MuestraConglomeradoRepository
//) : ViewModel() {
//
//    private val _state = MutableStateFlow<MuestraConglomeradoState>(MuestraConglomeradoState.Idle)
//    val state = _state.asStateFlow()
//
//    // ── Form state vive aquí, no en la Screen ──
//    var codsup by mutableStateOf("")
//        private set
//
//    fun syncConglomerado(isOnline: Boolean) {
//
//        viewModelScope.launch {
//            _state.value = MuestraConglomeradoState.Loading
//
//            val result = repository.syncMuestraVivienda(codsup, isOnline)
//
//            _state.value = when (result) {
//                is MuestraResult.Success -> MuestraConglomeradoState.Success(result.data)
//                is MuestraResult.Empty -> MuestraConglomeradoState.Error(result.message)
//                is MuestraResult.Error -> MuestraConglomeradoState.Error(result.message)
//            }
//        }
//    }
//
//    fun syncVivienda(isOnline: Boolean) {
//
//        viewModelScope.launch {
//            try {
//                repository.syncMuestraConglomerado(codsup, isOnline)
//            } catch (e: Exception) {
//                Log.e("SYNCHHHHH", "Error sincronizando muestra", e)
//            }
//        }
//    }
//
//    fun syncReentrevista(isOnline: Boolean) {
//
//        viewModelScope.launch {
//            try {
//                repository.syncMuestraConglomerado(codsup, isOnline)
//            } catch (e: Exception) {
//                Log.e("SYNCHHHHH", "Error sincronizando muestra", e)
//            }
//        }
//    }
//
//    fun resetState() {
//        _state.value = MuestraConglomeradoState.Idle
//    }
//}
//
//sealed class MuestraConglomeradoState {
//    object Idle : MuestraConglomeradoState()
//    object Loading : MuestraConglomeradoState()
//    data class Success(val data: List<MuestraConglomeradoDto>) : MuestraConglomeradoState()
//    data class Error(val message: String) : MuestraConglomeradoState()
//}

@HiltViewModel
class MuestraConglomeradoViewModel @Inject constructor(
    private val repository: MuestraConglomeradoRepository,
    private val syncStateRepo: SyncStateRepository
) : ViewModel() {

    fun observe(userId: String) =
        syncStateRepo.observe(userId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun sync(type: SyncType, userId: String, isOnline: Boolean) {
        viewModelScope.launch {

            syncStateRepo.setSyncing(userId, type)

            val result = when (type) {

                SyncType.CONGLOMERADO ->
                    repository.syncMuestraConglomerado(userId, isOnline)

                SyncType.VIVIENDA ->
                    repository.syncMuestraVivienda(userId, isOnline)

                SyncType.REENTREVISTA ->
                    repository.syncReentrevista(userId, isOnline)
            }

            when (result) {
                is MuestraResult.Success ->
                    syncStateRepo.success(userId, type)

                is MuestraResult.Error ->
                    syncStateRepo.error(userId, type, result.message)

                is MuestraResult.Empty ->
                    syncStateRepo.error(userId, type, result.message)
            }
        }
    }
    fun syncAll(userId: String, isOnline: Boolean) {
        viewModelScope.launch {
            SyncType.values().forEach {
                sync(it, userId, isOnline)
            }
        }
    }
}