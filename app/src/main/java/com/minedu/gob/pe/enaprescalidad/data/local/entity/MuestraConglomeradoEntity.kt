package com.minedu.gob.pe.enaprescalidad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MuestraConglomerado")
data class MuestraConglomeradoEntity(
    @PrimaryKey val idcongsup: String,
    val anioSup: Int,
    val mesSup: Int,
    val perSup: Int,
    val anio: Int,
    val mes: Int,
    val conglomerado: Int,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val odeiEnapres: String,
    val usuario: String,
    val fechaCreacion: String,
    val fechaEnvio: String,
    val enviado: String,
    val cerrado: String,
    val tipoMuestra: Int
)
