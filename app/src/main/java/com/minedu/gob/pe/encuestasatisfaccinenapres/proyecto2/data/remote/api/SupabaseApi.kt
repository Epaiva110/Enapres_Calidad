package com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.api

import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto.MuestraDto
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto.UsuarioDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseApi @Inject constructor(
    private val supabase: SupabaseClient
) {

    private val postgrest = supabase.postgrest

    // ─────────────────────────────
    // USUARIO
    // ─────────────────────────────

    suspend fun getUsuarios(): List<UsuarioDto> =
        postgrest.from("usuario")
            .select()
            .decodeList()

    suspend fun getUsuarioById(usuario: String): UsuarioDto =
        postgrest.from("usuario")
            .select {
                filter { eq("usuario", usuario) }
            }
            .decodeSingle()

    suspend fun insertUsuario(dto: UsuarioDto) {
        postgrest.from("usuario").insert(dto)
    }

    suspend fun updateUsuario(dto: UsuarioDto) {
        postgrest.from("usuario").update(dto) {
            filter { eq("usuario", dto.usuario) }
        }
    }

    suspend fun deleteUsuario(usuario: String) {
        postgrest.from("usuario").delete {
            filter { eq("usuario", usuario) }
        }
    }

    // ─────────────────────────────
    // MUESTRA
    // ─────────────────────────────

    suspend fun getMuestrasByUsuario(usuario: String): List<MuestraDto> =
        postgrest.from("muestra")
            .select {
                filter { eq("usuario", usuario) }
            }
            .decodeList()

    suspend fun insertMuestra(dto: MuestraDto) {
        postgrest.from("muestra").insert(dto)
    }

    suspend fun deleteMuestra(idcong: String) {
        postgrest.from("muestra").delete {
            filter { eq("idcong", idcong) }
        }
    }
}