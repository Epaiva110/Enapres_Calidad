package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource


import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api.MuestraApi
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import jakarta.inject.Inject

class MuestraConglomeradoRemoteDataSource @Inject constructor(
    private val api: MuestraApi
) {
    suspend fun getMuestraConglomeradoUsuario(idmt: Int): List<MuestraConglomeradoDto> {
        return api.getMuestraC(idmt)
    }
}