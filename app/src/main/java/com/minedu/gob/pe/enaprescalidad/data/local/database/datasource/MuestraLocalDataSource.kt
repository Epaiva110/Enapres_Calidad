package com.minedu.gob.pe.enaprescalidad.data.local.database.datasource

import com.minedu.gob.pe.enaprescalidad.data.local.dao.MarcoTrabajoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraReentrevistaDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraViviendaDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraReentrevistaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraViviendaEntity
import jakarta.inject.Inject

class MuestraLocalDataSource @Inject constructor (
    private val daoM: MarcoTrabajoDao,
    private val daoC: MuestraConglomeradoDao,
    private val daoV: MuestraViviendaDao,
    private val daoR: MuestraReentrevistaDao
    ){

    /**
     * Muestra Conglomerado
     **/

    suspend fun get(user: String): List<MuestraConglomeradoEntity> {
        return daoC.getMuestraUsuario(user)
    }

    suspend fun updateMT (id: Int, user: String, sincronizado: Boolean, fecha_sincronizacion: String) {
        daoM.updateMarcoTrabajo(id, user, sincronizado, fecha_sincronizacion)
    }

    suspend fun saveMuestrasC(user: String, data: List<MuestraConglomeradoEntity>) {
        daoC.deleteByUsuario(user)
        daoC.insertAll(data)
    }

    /**
     * Muestra Vivienda
     **/

    suspend fun getV(user: String): List<MuestraViviendaEntity> {
        return daoV.getMuestraUsuario(user)
    }
    suspend fun saveMuestrasV(user: String, data: List<MuestraViviendaEntity>) {
        daoV.deleteByUsuario(user)
        daoV.insertAll(data)
    }

    /**
     * Muestra Reentrevista
     **/

    suspend fun getR(user: String): List<MuestraReentrevistaEntity> {
        return daoR.getMuestraUsuario(user)
    }
    suspend fun saveMuestrasR(user: String, data: List<MuestraReentrevistaEntity>) {
        daoR.deleteByUsuario(user)
        daoR.insertAll(data)
    }

}