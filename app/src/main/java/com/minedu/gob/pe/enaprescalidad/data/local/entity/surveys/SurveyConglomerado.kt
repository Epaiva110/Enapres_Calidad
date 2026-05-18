package com.minedu.gob.pe.enaprescalidad.data.local.entity.surveys

import androidx.room.Entity
import androidx.room.Index

// ─────────────────────────────────────────────────────────────────────────────
//  RespuestaEntity — tabla Room para guardar respuestas del cuestionario
//
//  Clave compuesta: (muestra_id + survey_id + variable)
//  Así cada muestra tiene sus propias respuestas y no hay colisión entre
//  diferentes cuestionarios o diferentes conglomerados.
// ─────────────────────────────────────────────────────────────────────────────

@Entity(
    tableName = "Survey_Conglomerado",
    primaryKeys = ["muestra_id", "survey_id", "variable"],
    indices = [
        Index("muestra_id"),
        Index("survey_id")
    ],
)
data class SurveyConglomeradoEntity(
    val muestra_id: Int,
    val survey_id: String,
    val variable: String,
    val valor: String,
    val actualizado_en: Long = System.currentTimeMillis(),
)