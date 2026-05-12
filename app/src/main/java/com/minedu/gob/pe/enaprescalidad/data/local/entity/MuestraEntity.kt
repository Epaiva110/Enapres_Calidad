package com.minedu.gob.pe.enaprescalidad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Muestra_Conglomerado")
data class MuestraConglomeradoEntity(
    @PrimaryKey val id: Int,
    val id_mt: Int,
    val anio: Int,
    val mes: Int,
    val idcong: String,
    val conglomerado: String,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val odeienapres: String,
    val sincronizado: Boolean,
    val cerrado: Boolean,
    val fecha_sincronizacion: String?
)


@Entity(tableName = "Muestra_Vivienda")
data class MuestraViviendaEntity(
    @PrimaryKey val id: Int,
    val id_mt: Int,
    val anio: Int,
    val mes: Int,
    val idviv: String,
    val conglomerado: String,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val odeienapres: String,
    val sincronizado: Boolean,
    val cerrado: Boolean,
    val fecha_sincronizacion: String?
)


@Entity(tableName = "Muestra_Reentrevista")
data class MuestraReentrevistaEntity(
    @PrimaryKey val id: Int,
    val id_mt: Int,
    val anio: Int,
    val mes: Int,
    val idviv: String,
    val conglomerado: String,
    val departamento: String,
    val provincia: String,
    val distrito: String,
    val odeienapres: String,
    val sincronizado: Boolean,
    val cerrado: Boolean,
    val fecha_sincronizacion: String?
)
