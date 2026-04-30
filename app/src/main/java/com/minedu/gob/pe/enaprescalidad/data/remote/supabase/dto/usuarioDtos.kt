package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val usuario: String,
    val password: String,
    val activo: Boolean,
    val nombreusu: String,
    val role: String
)

