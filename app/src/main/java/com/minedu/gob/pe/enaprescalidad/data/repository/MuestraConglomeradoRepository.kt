package com.minedu.gob.pe.enaprescalidad.data.repository

import android.util.Log
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SyncDao

import com.minedu.gob.pe.enaprescalidad.data.local.database.datasource.MuestraLocalDataSource
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncType
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource.MuestraConglomeradoRemoteDataSource
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toEntity

import javax.inject.Inject
import javax.inject.Singleton


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

    suspend fun syncMuestraConglomerado(
        usuario: String,
        isOnline: Boolean
    ): MuestraResult {
        return try {

            if (!isOnline) {
                return MuestraResult.Error("No hay internet")
            }

            val data = remote.getMuestraConglomeradoUsuario(usuario)

            if (data.isEmpty()) {
                return MuestraResult.Empty("No hay datos para este usuario")
            }

            val entities = data.map { it.toEntity() }

            local.saveMuestras(entities)
            MuestraResult.Success(data)

        } catch (e: Exception) {
            MuestraResult.Error("Error de red o servidor")
        }
    }

    suspend fun syncMuestraVivienda(
        usuario: String,
        isOnline: Boolean
    ): MuestraResult {
        return try {

            if (!isOnline) {
                return MuestraResult.Error("No hay internet")
            }

            val data = remote.getMuestraConglomeradoUsuario(usuario)

            if (data.isEmpty()) {
                return MuestraResult.Empty("No hay datos para este usuario")
            }

            val entities = data.map { it.toEntity() }

            local.saveMuestras(entities)

            MuestraResult.Success(data)

        } catch (e: Exception) {
            MuestraResult.Error("Error de red o servidor")
        }
    }

    suspend fun syncReentrevista(
        usuario: String,
        isOnline: Boolean
    ): MuestraResult {
        return try {

            if (!isOnline) {
                return MuestraResult.Error("No hay internet")
            }

            val data = remote.getMuestraConglomeradoUsuario(usuario)

            if (data.isEmpty()) {
                return MuestraResult.Empty("No hay datos para este usuario")
            }

            val entities = data.map { it.toEntity() }

            local.saveMuestras(entities)

            MuestraResult.Success(data)

        } catch (e: Exception) {
            MuestraResult.Error("Error de red o servidor")
        }
    }


}

sealed class MuestraResult {
    data class Success(val data: List<MuestraConglomeradoDto>) : MuestraResult()
    data class Empty(val message: String = "No hay muestras") : MuestraResult()
    data class Error(val message: String) : MuestraResult()
}
