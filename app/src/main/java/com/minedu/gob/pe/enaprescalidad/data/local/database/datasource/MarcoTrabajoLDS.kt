package com.minedu.gob.pe.enaprescalidad.data.local.database.datasource

import android.util.Log
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MarcoTrabajoDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.views.MarcoTrabajoView
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class MarcoTrabajoLDS @Inject constructor(
    private val dao: MarcoTrabajoDao
) {
    fun getMarcoTrabajo(user: String): Flow<List<MarcoTrabajoView>> {
        return dao.getMarcoTrabajo(user)
    }

    fun getMarcoTrabajoTipo(tipo: String, user: String): Flow<List<MarcoTrabajoEntity>> {
        return dao.getMarcoTrabajoTipo(tipo, user)
    }

    suspend fun saveMarcoTrabajo(data: List<MarcoTrabajoEntity>) {
        dao.deleteByUsuario(data.firstOrNull()?.user?: return)
        dao.insertAll(data)
    }

    suspend fun saveMarcoTrabajoTipo(data: List<MarcoTrabajoEntity>) {
        val first = data.firstOrNull() ?: return
        dao.deleteByUsuarioAndTipo(first.user, first.tipo)
        dao.insertAll(data)
    }
}