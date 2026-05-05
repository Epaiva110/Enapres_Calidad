package com.minedu.gob.pe.enaprescalidad.data.local.database.datasource

import com.minedu.gob.pe.enaprescalidad.data.local.dao.MarcoTrabajoDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class MarcoTrabajoLDS @Inject constructor(
    private val dao: MarcoTrabajoDao
) {
    fun getMarcoTrabajo(user: String): Flow<List<MarcoTrabajoEntity>> {
        return dao.getMarcoTrabajo(user)
    }

    fun getMarcoTrabajoTipo(tipo: String, user: String): Flow<List<MarcoTrabajoEntity>> {
        return dao.getMarcoTrabajoTipo(tipo, user)
    }

    suspend fun saveMarcoTrabajo(data: List<MarcoTrabajoEntity>) {
        dao.deleteByUsuario(data.firstOrNull()?.usuario?: return)
        dao.insertAll(data)
    }
}