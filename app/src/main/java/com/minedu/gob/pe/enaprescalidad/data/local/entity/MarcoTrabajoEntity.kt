package com.minedu.gob.pe.enaprescalidad.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "MarcoTrabajo",
    primaryKeys = ["id"]
)
data class MarcoTrabajoEntity(
    val id: Int,
    val user: String,
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

enum class MarcoTrabajoType {
    CONGLOMERADO,
    VIVIENDA,
    REENTREVISTA
}