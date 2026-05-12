package com.minedu.gob.pe.enaprescalidad.data.domain

import com.google.type.DateTime
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    val id: Int,
    val user: String,
    val password: String,
    val active: Boolean,
    val user_name: String,
    val role: String,
    val last_connection: Long?
)