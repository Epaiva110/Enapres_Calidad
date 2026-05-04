package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class MuestraConglomeradoDto(
    val idcongsup: String,
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

@Serializable
data class MuestraViviendaDto(
    val idcongsup: String,
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

@Serializable
data class MuestraReentrevistaDto(
    val idcongsup: String,
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
