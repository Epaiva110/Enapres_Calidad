package com.minedu.gob.pe.enaprescalidad.data.local.database.datasource

import com.minedu.gob.pe.enaprescalidad.data.local.dao.MarcoTrabajoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraReentrevistaDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraViviendaDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraReentrevistaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraViviendaEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.ConglomeradoFiltroDto
import jakarta.inject.Inject

class MuestraLocalDataSource @Inject constructor (
    private val daoM: MarcoTrabajoDao,
    private val daoC: MuestraConglomeradoDao,
    private val daoV: MuestraViviendaDao,
    private val daoR: MuestraReentrevistaDao
    ){

    /**
     * Conglomerado Repository
     * **/

//    suspend fun getInfoCb(user: String): List<ConglomeradoFiltroDto> {
//        return daoC.getInfoCb(user)
//    }

    /**
     * Muestra Conglomerado
     **/

    suspend fun getC(idmt: Int, user: String): List<MuestraConglomeradoEntity> {
        return daoC.getMuestraUsuario(idmt,user)
    }

    suspend fun updateMT (id: Int, user: String, fechasincronizacion: String) {
        daoM.updateMarcoTrabajo(id, user, fechasincronizacion)
    }

    suspend fun saveMuestrasC(id: Int, data: List<MuestraConglomeradoEntity>) {
        daoC.deleteById(id)
        daoC.insertAll(data)
    }

    suspend fun saveMuestrasCL(id: List<Int>, data: List<MuestraConglomeradoEntity>) {
        daoC.deleteByIdL(id)
        daoC.insertAll(data)
    }

    suspend fun saveMuestrasRL(id: List<Int>, data: List<MuestraReentrevistaEntity>) {
        daoR.deleteByIdL(id)
        daoR.insertAll(data)
    }

    suspend fun saveMuestrasVL(id: List<Int>, data: List<MuestraViviendaEntity>) {
        daoV.deleteByIdL(id)
        daoV.insertAll(data)
    }

    /**
     * Muestra Vivienda
     **/

    suspend fun getV(idmt: Int,user: String): List<MuestraViviendaEntity> {
        return daoV.getMuestraUsuario(idmt,user)
    }
    suspend fun saveMuestrasV(id: Int, data: List<MuestraViviendaEntity>) {
        daoV.deleteById(id)
        daoV.insertAll(data)
    }

    /**
     * Muestra Reentrevista
     **/

    suspend fun getR(idmt: Int,user: String): List<MuestraReentrevistaEntity> {
        return daoR.getMuestraUsuario(idmt,user)
    }
    suspend fun saveMuestrasR(id: Int, data: List<MuestraReentrevistaEntity>) {
        daoR.deleteById(id)
        daoR.insertAll(data)
    }

}