package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class MuestraConglomeradoDto(
    val id: Int,
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

@Serializable
data class MuestraViviendaDto(
    val id: Int,
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

@Serializable
data class MuestraReentrevistaDto(
    val id: Int,
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
