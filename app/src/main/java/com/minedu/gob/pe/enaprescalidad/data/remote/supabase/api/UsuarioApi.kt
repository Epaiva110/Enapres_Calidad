package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.UsuarioDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import jakarta.inject.Inject

class UsuarioApi @Inject constructor(
    private val client: SupabaseClient
) {
    private val postgrest = client.postgrest

    suspend fun login(usuario: String, password: String): UsuarioDto? {
        return postgrest.from("usuario")
            .select {
                filter {
                    eq("usuario", usuario)
                    eq("password", password)
                }
            }
            .decodeSingleOrNull()
    }
}