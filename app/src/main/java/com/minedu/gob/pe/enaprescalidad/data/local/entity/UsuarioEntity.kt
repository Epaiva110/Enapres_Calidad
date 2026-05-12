package com.minedu.gob.pe.enaprescalidad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val id: Int,
    val user: String,
    val password: String,
    val active: Boolean,
    val user_name: String,
    val role: String,
    val last_connection: Long
)