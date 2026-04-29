package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import jakarta.inject.Inject
import kotlinx.coroutines.delay

class MuestraConnglomeradoApi @Inject constructor(
    private val client: SupabaseClient
) {
    private val postgrest = client.postgrest

    suspend fun getMuestraConglomeradoUsuario(usuario: String): List<MuestraConglomeradoDto> {
        return postgrest.from("muestra_conglomerado")
            .select {
                filter {
                    eq("usuario", usuario)
                }
            }
            .decodeList()
    }

    suspend fun getNumRegCM(usuario: String): Long {
        return postgrest.from("muestra_conglomerado")
            .select {
                filter {
                    eq("usuario", usuario)
                }
                count(Count.EXACT)
            }
            .countOrNull() ?: 0L
    }
}