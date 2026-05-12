package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MarcoTrabajoDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import jakarta.inject.Inject


class MarcoTrabajoApi @Inject constructor(
    private val client: SupabaseClient
) {
    private val postgrest = client.postgrest

    suspend fun getMarcoTrabajo(user: String): List<MarcoTrabajoDto> {
        return postgrest.from("Marco_Trabajo")
            .select {
                filter {
                    eq("user", user)
                }
            }
            .decodeList()
    }

    suspend fun getMarcoTrabajoTipo(user: String, tipo: String): List<MarcoTrabajoDto> {
        return postgrest.from("Marco_Trabajo")
            .select {
                filter {
                    eq("user", user)
                    eq( "tipo", tipo)
                }
            }
            .decodeList()
    }
}