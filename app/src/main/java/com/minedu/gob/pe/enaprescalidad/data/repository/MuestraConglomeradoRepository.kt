package com.minedu.gob.pe.enaprescalidad.data.repository

import android.util.Log

import com.minedu.gob.pe.enaprescalidad.data.local.database.datasource.MuestraConglomeradoLocalDataSource
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource.MuestraConglomeradoRemoteDataSource
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toDomain
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toEntity

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MuestraConglomeradoRepository @Inject constructor(
    private val remote: MuestraConglomeradoRemoteDataSource,
    private val local: MuestraConglomeradoLocalDataSource,
) {

    suspend fun syncMuestraConglomerado(
        usuario: String,
        isOnline: Boolean
    ): MuestraResult {
        return try {

            Log.d("SYNCHHHHHHHHHHHHH", "isOnline: $isOnline")

            if (!isOnline) {
                return MuestraResult.Error("No hay internet")
            }

            val data = remote.getMuestraConglomeradoUsuario(usuario)

            Log.d("SYNCHHHHHHHHHHHHH", "REMOTE SIZE: ${data.size}")

            if (data.isEmpty()) {
                return MuestraResult.Empty("No hay datos para este usuario")
            }

            val entities = data.map { it.toEntity() }

            Log.d("SYNCHHHHHHHHHHHHH", "ENTITY SIZE: ${entities.size}")

            local.saveMuestras(entities)

//            val test = local.getAll()
//            Log.d("SYNC", "ROOM SIZE: ${test.size}")

            MuestraResult.Success(data)

        } catch (e: Exception) {
            Log.e("SYNCHHHHHHHHHHHHH", "ERROR", e)
            MuestraResult.Error("Error de red o servidor")
        }
    }


}

sealed class MuestraResult {
    data class Success(val data: List<MuestraConglomeradoDto>) : MuestraResult()
    data class Empty(val message: String = "No hay muestras") : MuestraResult()
    data class Error(val message: String) : MuestraResult()
}
