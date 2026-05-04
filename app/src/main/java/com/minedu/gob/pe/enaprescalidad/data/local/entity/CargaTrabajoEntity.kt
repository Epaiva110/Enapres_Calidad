package com.minedu.gob.pe.enaprescalidad.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "carga_trabajo",
    primaryKeys = ["id"]
)
data class CargaTrabajoEntity(
    val id: Int,
    val usuario: String,
    val orden: Int,
    val tipo: String,             // "CONGLOMERADO" | "VIVIENDA" | "REENTREVISTA"
    val fechaProgramacion: String,
    val anio: Int,
    val mes: Int,              // "Enero", "Febrero"...
    val periodo: Int,
    val totalMuestras: Int,
    val totalActualizado: Int,
    val actualizado: Boolean,
    val fechaActualizacion: String?
)