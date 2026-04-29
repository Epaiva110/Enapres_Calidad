package com.minedu.gob.pe.enaprescalidad.data.local.database.datasource

import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import jakarta.inject.Inject

class MuestraLocalDataSource @Inject constructor (
    private val dao: MuestraConglomeradoDao,
//    private val daoVivienda: MuestraViviendaDao,
//    private val daoReentrevista: MuestraReentrevistaDao
    ){
    suspend fun get(user: String): List<MuestraConglomeradoEntity> {
        return dao.getMuestraUsuario(user)
    }
    suspend fun saveMuestras(data: List<MuestraConglomeradoEntity>) {
        dao.deleteByUsuario(data.firstOrNull()?.usuario ?: return)
        dao.insertAll(data)
    }
}