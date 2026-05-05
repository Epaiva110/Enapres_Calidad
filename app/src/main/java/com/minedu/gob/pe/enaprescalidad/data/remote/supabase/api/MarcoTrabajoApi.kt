package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MarcoTrabajoDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import jakarta.inject.Inject


class MarcoTrabajoApi @Inject constructor(
    private val client: SupabaseClient
) {
    private val postgrest = client.postgrest

    suspend fun getMarcoTrabajo(usuario: String): List<MarcoTrabajoDto> {
        return postgrest.from("Marco_Trabajo")
            .select {
                filter {
                    eq("usuario", usuario)
                }
            }
            .decodeList()
    }
}