package com.minedu.gob.pe.enaprescalidad.data.repository

import android.util.Log
import com.minedu.gob.pe.enaprescalidad.data.local.database.datasource.MarcoTrabajoLDS
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource.MarcoTrabajoRDS
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MarcoTrabajoDto
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarcoTrabajoRepository @Inject constructor(
    private val remote: MarcoTrabajoRDS,
    private val local: MarcoTrabajoLDS,
) {
    suspend fun getMarcoTrabajo(
        user: String,
        isOnline: Boolean
    ): MarcoTrabajoResult {
        return try {

            if (!isOnline) {
                return MarcoTrabajoResult.Error("No hay internet")
            }

            val data = remote.getMarcoTrabajo(user)

            if (data.isEmpty()) {
                return MarcoTrabajoResult.Empty("No hay datos para este usuario")
            }

            val entities = data.map { it.toEntity() }

            local.saveMarcoTrabajo(entities)
            MarcoTrabajoResult.Success(data)

        } catch (e: Exception) {
            //Log.e("Error", "Error", e)
            MarcoTrabajoResult.Error("Error de red o servidor")
        }
    }

    suspend fun getMarcoTrabajoTipo(
        user: String,

        isOnline: Boolean
    ): MarcoTrabajoResult {
        return try {

            if (!isOnline) {
                return MarcoTrabajoResult.Error("No hay internet")
            }

            val data = remote.getMarcoTrabajo(user)

            if (data.isEmpty()) {
                return MarcoTrabajoResult.Empty("No hay datos para este usuario")
            }

            val entities = data.map { it.toEntity() }

            local.saveMarcoTrabajo(entities)
            MarcoTrabajoResult.Success(data)

        } catch (e: Exception) {
            //Log.e("Error", "Error", e)
            MarcoTrabajoResult.Error("Error de red o servidor")
        }
    }
}


sealed class MarcoTrabajoResult {
    data class Success(val data: List<MarcoTrabajoDto>) : MarcoTrabajoResult()
    data class Empty(val message: String = "No hay muestras") : MarcoTrabajoResult()
    data class Error(val message: String) : MarcoTrabajoResult()
}

