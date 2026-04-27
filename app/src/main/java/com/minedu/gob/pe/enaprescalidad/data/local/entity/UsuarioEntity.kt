package com.minedu.gob.pe.enaprescalidad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val usuario: String,
    val password: String,
    val activo: Boolean,
    val lastUpdated: Long
)