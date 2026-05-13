package com.minedu.gob.pe.enaprescalidad.data.repository

import android.util.Log
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SyncDao
import com.minedu.gob.pe.enaprescalidad.data.local.database.datasource.MuestraLocalDataSource
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncType
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource.MuestraConglomeradoRemoteDataSource
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraReentrevistaDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraViviendaDto
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton


@Singleton
class SyncStateRepository @Inject constructor(
    private val dao: SyncDao
) {

    fun observe(userId: String) =
        dao.observeByUser(userId)

    suspend fun setSyncing(userId: String, type: SyncType) {
        dao.markSyncing(userId, type.name)
    }

    suspend fun success(userId: String, type: SyncType) {
        dao.markSuccess(
            userId,
            type.name,
            System.currentTimeMillis()
        )
    }

    suspend fun error(userId: String, type: SyncType, msg: String) {
        dao.markError(userId, type.name, msg)
    }
}

@Singleton
class MuestraConglomeradoRepository @Inject constructor(
    private val remote: MuestraConglomeradoRemoteDataSource,
    private val local: MuestraLocalDataSource,
) {

    suspend fun syncMuestra(
        idmt: Int,
        user: String,
        isOnline: Boolean
    ): SyncResult {

        if (syncMuestraConglomerado(idmt, user, isOnline) is MuestraResult.Error) {
            return SyncResult.Error("Error sincronizando conglomerados")
        }

        if (syncMuestraVivienda(idmt, user, isOnline) is MuestraResult.Error) {
            return SyncResult.Error("Error sincronizando viviendas")
        }

        if (syncMuestraReentrevista(idmt, user, isOnline) is MuestraResult.Error) {
            return SyncResult.Error("Error sincronizando reentrevistas")
        }

        return SyncResult.Success
    }

    suspend fun updateMT(
        id: Int,
        user: String)
    {
        local.updateMT(id, user, true, System.currentTimeMillis().toString())
        Log.i("updateMT", "updateMT: $id")
    }


    suspend fun syncMuestraConglomerado(
        idmt: Int,
        user: String,
        isOnline: Boolean
    ): MuestraResult<MuestraConglomeradoDto> {



        return syncMuestraM(
            user = user,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraC(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasC
        )

    }

    suspend fun syncMuestraVivienda(
        idmt: Int,
        user: String,
        isOnline: Boolean
    ): MuestraResult<MuestraViviendaDto> {

        return syncMuestraM(
            user = user,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraV(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasV
        )
    }

    suspend fun syncMuestraReentrevista(
        idmt: Int,
        user: String,
        isOnline: Boolean
    ): MuestraResult<MuestraReentrevistaDto> {

        return syncMuestraM(
            user = user,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraR(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasR
        )
    }

    private suspend fun <DTO, ENTITY> syncMuestraM(
        user: String,
        isOnline: Boolean,
        remoteCall: suspend () -> List<DTO>,
        mapper: (DTO) -> ENTITY,
        saveLocal: suspend (String, List<ENTITY>) -> Unit
    ): MuestraResult<DTO> {

        return try {

            if (!isOnline) {
                return MuestraResult.Error("No hay internet")
            }

            val data = remoteCall()

            if (data.isEmpty()) {
                return MuestraResult.Empty("No hay muestras, informar al administrador")
            }

            val entities = data.map(mapper)

            saveLocal(user, entities)
            MuestraResult.Success(data)

        } catch (e: Exception) {
            MuestraResult.Error("Error de red o servidor")
        }
    }

}

sealed class SyncResult {
    data object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}
sealed class MuestraResult<out T> {
    data class Success<T>(val data: List<T>) : MuestraResult<T>()
    data class Empty(val message: String = "No hay muestras") : MuestraResult<Nothing>()
    data class Error(val message: String) : MuestraResult<Nothing>()
}
