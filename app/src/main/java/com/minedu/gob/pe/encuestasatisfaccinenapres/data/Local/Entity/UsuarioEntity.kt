package com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioRoom(
    @PrimaryKey val usuario: String,
    val passwordEncrypted: String,
    val activo: Boolean,
    val lastUpdated: Long
)