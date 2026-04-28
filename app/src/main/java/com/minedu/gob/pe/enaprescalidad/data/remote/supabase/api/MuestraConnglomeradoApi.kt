package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.UsuarioDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import jakarta.inject.Inject

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
}