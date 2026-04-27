package com.minedu.gob.pe.enaprescalidad.data.domain

import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val usuario: String,
    val password: String,
    val activo: Boolean
)