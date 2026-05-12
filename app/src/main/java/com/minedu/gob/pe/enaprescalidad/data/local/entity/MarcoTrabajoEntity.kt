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

enum class MarcoTrabajoType {
    CONGLOMERADO,
    VIVIENDA,
    REENTREVISTA
}