package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.datasource

import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api.UsuarioApi
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.UsuarioDto
import jakarta.inject.Inject

class UsuarioRemoteDataSource @Inject constructor(
    private val api: UsuarioApi
) {
    suspend fun login(usuario: String, password: String): UsuarioDto? {
        return api.login(usuario, password)
    }
}