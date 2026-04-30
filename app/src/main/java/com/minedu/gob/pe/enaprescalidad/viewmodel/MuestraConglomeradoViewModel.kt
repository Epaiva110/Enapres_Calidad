package com.minedu.gob.pe.enaprescalidad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncType
import com.minedu.gob.pe.enaprescalidad.data.repository.MuestraConglomeradoRepository
import com.minedu.gob.pe.enaprescalidad.data.repository.MuestraResult
import com.minedu.gob.pe.enaprescalidad.data.repository.SyncStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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