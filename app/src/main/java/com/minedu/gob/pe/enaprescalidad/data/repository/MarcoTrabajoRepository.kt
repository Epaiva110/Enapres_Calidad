package com.minedu.gob.pe.enaprescalidad.data.repository

import android.util.Log
import com.minedu.gob.pe.enaprescalidad.data.domain.MarcoTrabajo
import com.minedu.gob.pe.enaprescalidad.data.local.database.datasource.MarcoTrabajoLDS
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource.MarcoTrabajoRDS
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MarcoTrabajoDto
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toDomain
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toDto
import com.minedu.gob.pe.enaprescalidad.data.repository.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    ): MarcoTrabajoResultRemote {
        return try {

            if (!isOnline) {
                return MarcoTrabajoResultRemote.Error("No hay internet")
            }

            val data = remote.getMarcoTrabajo(user)

            if (data.isEmpty()) {
                return MarcoTrabajoResultRemote.Empty("No hay datos para este usuario")
            }

            val entities = data.map { it.toEntity() }

            local.saveMarcoTrabajo(entities)
            MarcoTrabajoResultRemote.Success(data)

        } catch (e: Exception) {
            //Log.e("Error", "Error", e)
            MarcoTrabajoResultRemote.Error("Error de red o servidor")
        }
    }

//    fun getMarcoTrabajoTipo(
//        user: String,
//        tipo: String
//    ): Flow<MarcoTrabajoResult> {
//
//        return local.getMarcoTrabajoTipo(tipo, user)
//            .map { data ->
//
//                if (data.isEmpty()) {
//                    MarcoTrabajoResult.Empty("No hay datos para este usuario")
//                } else {
//                    val dto = data.map { it.toDto() }
//                    MarcoTrabajoResult.Success(dto)
//                }
//
//            }
//    }

    fun getMarcoTrabajoTipo(
        user: String,
        tipo: String
    ): Flow<MarcoTrabajoResultLocal> {

        return local.getMarcoTrabajoTipo(tipo, user)
            .map { data ->

                if (data.isEmpty()) {
                    MarcoTrabajoResultLocal.Empty("No hay datos para este usuario")
                } else {
                    val domainList = data.map { it.toDomain() }

                    MarcoTrabajoResultLocal.Success(domainList)
                }

            }
    }

    fun getMarcoTrabajoLocal(
        user: String,
    ): Flow<MarcoTrabajoResultLocal> {

        return local.getMarcoTrabajo(user)
            .map { data ->

                if (data.isEmpty()) {
                    MarcoTrabajoResultLocal.Empty("No hay datos para este usuario")
                } else {
                    val domainList = data.map { it.toDomain() }

                    MarcoTrabajoResultLocal.Success(domainList)
                }

            }
    }
}


sealed class MarcoTrabajoResultRemote {
    data class Success(val data: List<MarcoTrabajoDto>) : MarcoTrabajoResultRemote()
    data class Empty(val message: String = "No hay muestras") : MarcoTrabajoResultRemote()
    data class Error(val message: String) : MarcoTrabajoResultRemote()
}

sealed class MarcoTrabajoResultLocal {
    data class Success(val data: List<MarcoTrabajo>) : MarcoTrabajoResultLocal()
    data class Empty(val message: String = "No hay muestras") : MarcoTrabajoResultLocal()
    data class Error(val message: String) : MarcoTrabajoResultLocal()
}


