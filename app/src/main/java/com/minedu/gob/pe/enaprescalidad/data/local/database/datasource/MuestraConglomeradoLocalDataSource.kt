package com.minedu.gob.pe.enaprescalidad.data.local.database.datasource

import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import jakarta.inject.Inject

class MuestraConglomeradoLocalDataSource @Inject constructor (
    private val dao: MuestraConglomeradoDao
    ){
    suspend fun get(user: String): List<MuestraConglomeradoEntity> {
        return dao.getMuestraUsuario(user)
    }
    suspend fun saveMuestras(data: List<MuestraConglomeradoEntity>) {
        dao.deleteByUsuario(data.firstOrNull()?.usuario ?: return)
        dao.insertAll(data)
    }
}