package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.UsuarioDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import jakarta.inject.Inject

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UsuarioApi @Inject constructor(
    private val client: SupabaseClient
) {
    private val postgrest = client.postgrest

    suspend fun login(user: String, password: String): UsuarioDto? {
        return postgrest.from("usuario")
            .select {
                filter {
                    eq("user", user)
                    eq("password", password)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun update(user: String, lastConnection: Long) {
        val formatted = Instant.ofEpochMilli(lastConnection)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        postgrest.from("usuario")
            .update(
                {
                    set("last_connection", formatted)
                }
            ) {
                filter {
                    eq("user", user)
                }
            }
    }
}





