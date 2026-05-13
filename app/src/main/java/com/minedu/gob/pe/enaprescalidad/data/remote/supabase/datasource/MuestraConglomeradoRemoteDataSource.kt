package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource


import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api.MuestraApi
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraReentrevistaDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraViviendaDto
import jakarta.inject.Inject

class MuestraConglomeradoRemoteDataSource @Inject constructor(
    private val api: MuestraApi
) {
    suspend fun getMuestraC(idmt: Int): List<MuestraConglomeradoDto> {
        return api.getMuestraC(idmt)
    }
    suspend fun getMuestraV(idmt: Int): List<MuestraViviendaDto> {
        return api.getMuestraV(idmt)
    }
    suspend fun getMuestraR(idmt: Int): List<MuestraReentrevistaDto> {
        return api.getMuestraR(idmt)
    }
}