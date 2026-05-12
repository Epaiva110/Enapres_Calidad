package com.minedu.gob.pe.enaprescalidad.data.domain
import kotlinx.serialization.Serializable


@Serializable
data class MarcoTrabajo (
    val id: Int,
    val user: String,
    val orden: Int,
    val tipo: String,
    val fecha: String,
    val anio: Int,
    val mes: Int,
    val periodo: Int,
    val totalMuestra: Int,
    val totalActualizado: Int,
    val actualizado: Boolean,
    val fechaActualizacion: String?,
    val tipoMuestra: Int
){
    // REGLA: Está realmente al día solo si dice "Si" Y el total coincide
    val estaAlDia: Boolean
        get() = actualizado == true && totalActualizado!! >= totalMuestra && totalMuestra > 0
}


