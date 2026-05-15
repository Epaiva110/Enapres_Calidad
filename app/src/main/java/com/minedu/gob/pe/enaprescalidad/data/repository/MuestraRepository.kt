package com.minedu.gob.pe.enaprescalidad.data.repository

import com.minedu.gob.pe.enaprescalidad.data.local.dao.SyncDao
import com.minedu.gob.pe.enaprescalidad.data.local.database.datasource.MuestraLocalDataSource
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncType
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource.MuestraRemoteDataSource
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
    private val remote: MuestraRemoteDataSource,
    private val local: MuestraLocalDataSource,
) {

    suspend fun syncMuestra(
        idmt: Int,
        isOnline: Boolean
    ): SyncResult {

        if (syncMuestraConglomerado(idmt, isOnline) is MuestraResult.Error) {
            return SyncResult.Error("Error sincronizando conglomerados")
        }

        if (syncMuestraVivienda(idmt, isOnline) is MuestraResult.Error) {
            return SyncResult.Error("Error sincronizando viviendas")
        }

        if (syncMuestraReentrevista(idmt, isOnline) is MuestraResult.Error) {
            return SyncResult.Error("Error sincronizando reentrevistas")
        }

        return SyncResult.Success
    }

    suspend fun updateMT(
        id: Int,
        user: String)
    {
        local.updateMT(id, user,  System.currentTimeMillis().toString())
    }


    suspend fun syncMuestraConglomeradoL(
        idmt: List<Int>,
        isOnline: Boolean
    ): MuestraResult<MuestraConglomeradoDto> {
        return syncMuestraML(
            id = idmt,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraCL(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasCL
        )

    }

    suspend fun syncMuestraViviendaL(
        idmt: List<Int>,
        isOnline: Boolean
    ): MuestraResult<MuestraViviendaDto> {
        return syncMuestraML(
            id = idmt,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraVL(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasVL
        )

    }

    suspend fun syncMuestraReentrevistaL(
        idmt: List<Int>,
        isOnline: Boolean
    ): MuestraResult<MuestraReentrevistaDto> {
        return syncMuestraML(
            id = idmt,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraRL(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasRL
        )

    }

    /*Casos unicos*/

    suspend fun syncMuestraConglomerado(
        idmt: Int,
        isOnline: Boolean
    ): MuestraResult<MuestraConglomeradoDto> {
        return syncMuestraM(
            id = idmt,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraC(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasC
        )

    }

    suspend fun syncMuestraVivienda(
        idmt: Int,
        isOnline: Boolean
    ): MuestraResult<MuestraViviendaDto> {

        return syncMuestraM(
            id = idmt,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraV(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasV
        )
    }

    suspend fun syncMuestraReentrevista(
        idmt: Int,
        isOnline: Boolean
    ): MuestraResult<MuestraReentrevistaDto> {

        return syncMuestraM(
            id = idmt,
            isOnline = isOnline,
            remoteCall = { remote.getMuestraR(idmt) },
            mapper = { it.toEntity() },
            saveLocal = local::saveMuestrasR
        )
    }

    private suspend fun <DTO, ENTITY> syncMuestraM(
        id: Int,
        isOnline: Boolean,
        remoteCall: suspend () -> List<DTO>,
        mapper: (DTO) -> ENTITY,
        saveLocal: suspend (Int, List<ENTITY>) -> Unit
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

            saveLocal(id, entities)
            MuestraResult.Success(data)

        } catch (e: Exception) {
            MuestraResult.Error("Error de red o servidor")
        }
    }

    private suspend fun <DTO, ENTITY> syncMuestraML(
        id: List<Int>,
        isOnline: Boolean,
        remoteCall: suspend () -> List<DTO>,
        mapper: (DTO) -> ENTITY,
        saveLocal: suspend (List<Int>, List<ENTITY>) -> Unit
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

            saveLocal(id, entities)
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
