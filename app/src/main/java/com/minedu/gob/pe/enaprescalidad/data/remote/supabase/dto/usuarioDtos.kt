package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: Int,
    val user: String,
    val password: String,
    val active: Boolean,
    val user_name: String,
    val role: String,
    val last_connection: Long?
)