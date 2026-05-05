package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api.MarcoTrabajoApi
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api.MuestraConnglomeradoApi
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MarcoTrabajoDto

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import jakarta.inject.Inject

class MarcoTrabajoRDS @Inject constructor(
    private val api: MarcoTrabajoApi
) {
    suspend fun getMarcoTrabajo(user: String): List<MarcoTrabajoDto> {
        return api.getMarcoTrabajo(user)
    }

    suspend fun getMarcoTrabajoTipo(user: String, tipo: String): List<MarcoTrabajoDto> {
        return api.getMarcoTrabajoTipo(user, tipo)
    }
}