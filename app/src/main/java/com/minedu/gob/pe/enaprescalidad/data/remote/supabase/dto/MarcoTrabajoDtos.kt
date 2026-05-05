package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class MarcoTrabajoDto(
    val id: Int,
    val usuario: String,
    val orden: Int,
    val tipo: String,             // "CONGLOMERADO" | "VIVIENDA" | "REENTREVISTA"
    val fechaProgramacion: String,
    val anio: Int,
    val mes: Int,              // "Enero", "Febrero"...
    val periodo: Int,
    val totalMuestra: Int,
    val totalActualizado: Int,
    val actualizado: Boolean,
    val fechaActualizacion: String?,
    val tipoMuestra: Int
)
