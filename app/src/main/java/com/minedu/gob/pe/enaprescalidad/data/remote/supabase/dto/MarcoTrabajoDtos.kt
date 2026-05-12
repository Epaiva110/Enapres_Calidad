package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class MarcoTrabajoDto(
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
    val fecha_sincronizacion: String?,
    val proyecto: Int
)
