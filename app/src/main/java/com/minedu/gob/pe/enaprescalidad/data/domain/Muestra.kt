package com.minedu.gob.pe.enaprescalidad.data.domain

import kotlinx.serialization.Serializable

@Serializable
data class MuestraConglomerado (
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


//@Serializable
//data class MuestraVivienda (
//    val idcongsup: String,
//    val anioSup: Int,
//    val mesSup: Int,
//    val perSup: Int,
//    val anio: Int,
//    val mes: Int,
//    val conglomerado: String,
//    val departamento: String,
//    val provincia: String,
//    val distrito: String,
//    val odeiEnapres: String,
//    val usuario: String,
//    val fechaCreacion: String,
//    val fechaEnvio: String?,
//    val enviado: Boolean,
//    val cerrado: Boolean,
//    val tipoMuestra: Int
//)
//
//
//@Serializable
//data class MuestraReentrevista (
//    val idcongsup: String,
//    val anioSup: Int,
//    val mesSup: Int,
//    val perSup: Int,
//    val anio: Int,
//    val mes: Int,
//    val conglomerado: String,
//    val departamento: String,
//    val provincia: String,
//    val distrito: String,
//    val odeiEnapres: String,
//    val usuario: String,
//    val fechaCreacion: String,
//    val fechaEnvio: String?,
//    val enviado: Boolean,
//    val cerrado: Boolean,
//    val tipoMuestra: Int
//)