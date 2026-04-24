package com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val usuario: String,
    val password: String,
    val estado: Boolean = true
)

@Serializable
data class MuestraDto(
    @SerialName("idcong") val idcong: String,
    @SerialName("usuario") val usuario: String
)
