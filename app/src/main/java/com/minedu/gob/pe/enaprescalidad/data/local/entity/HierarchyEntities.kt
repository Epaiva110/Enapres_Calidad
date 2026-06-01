package com.minedu.gob.pe.enaprescalidad.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ═════════════════════════════════════════════════════════════════════════════
//  JERARQUÍA DE ENTIDADES DE CAMPO
// ═════════════════════════════════════════════════════════════════════════════

@Entity(tableName = "viviendas", indices = [Index("muestraConglomeradoId")])
data class ViviendaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val muestraConglomeradoId: Int,
    val numeroOrden: Int,
    val direccion: String = "",
    val estadoEncuesta: String = "NUEVO",
    val creadoEn: Long = System.currentTimeMillis(),
    val actualizadoEn: Long = System.currentTimeMillis(),
)

@Entity(tableName = "hogares", indices = [Index("viviendaId")])
data class HogarEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val viviendaId: Int,
    val numeroOrden: Int,
    val nombreJefeHogar: String = "",
    val estadoEncuesta: String = "NUEVO",
    val creadoEn: Long = System.currentTimeMillis(),
    val actualizadoEn: Long = System.currentTimeMillis(),
)

@Entity(tableName = "personas", indices = [Index("hogarId")])
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hogarId: Int,
    val numeroOrden: Int,
    val nombres: String = "",
    val apellidos: String = "",
    val fechaNacimiento: String = "",
    val edad: String = "",
    val sexo: String = "",
    val estadoEncuesta: String = "NUEVO",
    val creadoEn: Long = System.currentTimeMillis(),
    val actualizadoEn: Long = System.currentTimeMillis(),
) {
    val nombreCompleto: String get() = "$nombres $apellidos".trim()
    val esJefeHogar: Boolean  get() = numeroOrden == 1
}

@Entity(tableName = "visitas_conglomerado", indices = [Index("muestraConglomeradoId")])
data class VisitaConglomeradoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val muestraConglomeradoId: Int,
    val numeroVisita: Int,
    val fechaHoraVisita: String = "",
    val resultado: String = "PENDIENTE",
    val encuestador: String = "",
    val ubicacion: String = "",
    val observaciones: String = "",
    val proximaVisita: String = "",
    val creadoEn: Long = System.currentTimeMillis(),
    val actualizadoEn: Long = System.currentTimeMillis(),
)

@Entity(tableName = "visitas_hogar", indices = [Index("hogarId")])
data class VisitaHogarEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hogarId: Int,
    val numeroVisita: Int,
    val fechaHoraVisita: String = "",
    val resultado: String = "PENDIENTE",
    val encuestador: String = "",
    val ubicacion: String = "",
    val observaciones: String = "",
    val proximaVisita: String = "",
    val creadoEn: Long = System.currentTimeMillis(),
    val actualizadoEn: Long = System.currentTimeMillis(),
)

// ═════════════════════════════════════════════════════════════════════════════
//  VERSIONADO Y RESPUESTAS DE ENCUESTA
//
//  PK usa contextKey en lugar de unidadId para identificar exactamente
//  quién está respondiendo:
//    "CONGLOMERADO:42", "HOGAR:3", "PERSONA:15", "VISITA_HOGAR:3:2", etc.
// ═════════════════════════════════════════════════════════════════════════════

@Entity(
    tableName  = "survey_versions",
    primaryKeys = ["surveyType", "contextKey"],
    indices    = [Index("surveyType"), Index("contextKey")],
)
data class SurveyVersionEntity(
    val surveyType: String,
    val contextKey: String,
    val version: String,
    val jsonSnapshot: String,
    val origen: String = "assets",
    val snapshotEn: Long = System.currentTimeMillis(),
)

@Entity(
    tableName   = "survey_responses",
    primaryKeys = ["surveyType", "contextKey", "variable"],
    indices     = [Index("surveyType", "contextKey")],
)
data class SurveyResponseEntity(
    val surveyType: String,
    val contextKey: String,
    val variable: String,
    val valor: String,
    val actualizadoEn: Long = System.currentTimeMillis(),
)