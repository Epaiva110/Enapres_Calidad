package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api.MuestraConnglomeradoApi
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import jakarta.inject.Inject

class MuestraConglomeradoRemoteDataSource @Inject constructor(
    private val api: MuestraConnglomeradoApi
) {
    suspend fun getMuestraConglomeradoUsuario(usuario: String): List<MuestraConglomeradoDto> {
        return api.getMuestraConglomeradoUsuario(usuario)
    }
}