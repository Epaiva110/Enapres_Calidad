package com.minedu.gob.pe.enaprescalidad.data.local.entity

import android.R
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
    val conglomerado: String,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val odeiEnapres: String,
    val usuario: String,
    val fechaCreacion: String,
    val fechaEnvio: String?,
    val enviado: Boolean,
    val cerrado: Boolean,
    val tipoMuestra: Int
)


@Entity(tableName = "MuestraVivienda")
data class MuestraViviendaEntity(
    @PrimaryKey val idcongsup: String,
    val anioSup: Int,
    val mesSup: Int,
    val perSup: Int,
    val anio: Int,
    val mes: Int,
    val conglomerado: String,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val odeiEnapres: String,
    val usuario: String,
    val fechaCreacion: String,
    val fechaEnvio: String?,
    val enviado: Boolean,
    val cerrado: Boolean,
    val tipoMuestra: Int
)


@Entity(tableName = "MuestraConglomerado")
data class MuestraReentrevistaEntity(
    @PrimaryKey val idcongsup: String,
    val anioSup: Int,
    val mesSup: Int,
    val perSup: Int,
    val anio: Int,
    val mes: Int,
    val conglomerado: String,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val odeiEnapres: String,
    val usuario: String,
    val fechaCreacion: String,
    val fechaEnvio: String?,
    val enviado: Boolean,
    val cerrado: Boolean,
    val tipoMuestra: Int
)
