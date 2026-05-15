package com.minedu.gob.pe.enaprescalidad.data.domain
import kotlinx.serialization.Serializable


@Serializable
data class MarcoTrabajo (
    val id: Int,
    val user: String,
    val orden: Int,
    val tipo: String,
    val fecha_programacion: String,
    val anio: Int,
    val mes: Int,
    val periodo: Int,
    val meta: Int,
    val descargas: Int,
    val sincronizado: Boolean,
    val fecha_sincronizacion: Long?,
    val proyecto: Int
){
    // REGLA: Está realmente al día solo si dice "Si" Y el total coincide
    val estaAlDia: Boolean
        get() = sincronizado && descargas >= meta && meta > 0
    val fechasincronizacionAlter: Long?
        get() = if (descargas>0) fecha_sincronizacion else null
}


