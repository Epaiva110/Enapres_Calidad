package com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey
    val usuario: String,
    val password: String,
    val estado: Boolean = true
)

@Entity(
    tableName = "muestra",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["usuario"],
            childColumns = ["usuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuario")]
)
data class MuestraEntity(
    @PrimaryKey
    val idcong: String,
    val usuario: String
)