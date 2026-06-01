package com.minedu.gob.pe.enaprescalidad.surveys.models

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY CONTEXT — contexto de ejecución de una encuesta
//
//  Identifica exactamente QUÉ unidad de análisis se está respondiendo.
//  Resuelve el problema de ambigüedad del unidadId anterior.
//
//  JERARQUÍA:
//    CONGLOMERADO (muestraId)
//      └── VISITA_CONGLOMERADO (muestraId + visitaId)
//      └── VIVIENDA (viviendaId)
//            └── HOGAR (hogarId)
//                  └── VISITA_HOGAR (hogarId + visitaId)
//                  └── PERSONA (personaId)
//
//  CÓMO SE USA:
//    El SurveyContext se construye ANTES de abrir SurveyScreen.
//    Se serializa a una clave única (contextKey) que actúa como
//    identificador de la sesión de respuestas en Room.
//
//  EJEMPLOS DE contextKey:
//    "CONGLOMERADO:42"              → encuesta del conglomerado 42
//    "VIVIENDA:7"                   → encuesta de la vivienda 7
//    "HOGAR:3"                      → encuesta del hogar 3
//    "PERSONA:15"                   → encuesta de la persona 15 (p. ej. reentrevista)
//    "VISITA_HOGAR:3:2"             → visita #2 al hogar 3
//    "VISITA_CONGLOMERADO:42:1"     → visita #1 al conglomerado 42
// ─────────────────────────────────────────────────────────────────────────────

sealed class SurveyContext {

    /** Clave única que identifica esta sesión en la tabla survey_responses */
    abstract val contextKey: String

    /** Tipo de encuesta asociado (para resolver el JSON del catálogo) */
    abstract val surveyType: com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType

    /** Descripción legible para mostrar en la UI (TopBar) */
    abstract val descripcion: String

    // ─────────────────────────────────────────────────────────────────────────
    //  CONGLOMERADO
    // ─────────────────────────────────────────────────────────────────────────

    data class Conglomerado(
        val muestraId: Int,
    ) : SurveyContext() {
        override val contextKey   = "CONGLOMERADO:$muestraId"
        override val surveyType   = com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType.CONGLOMERADO
        override val descripcion  = "Conglomerado #$muestraId"
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VISITA DE CONGLOMERADO
    // ─────────────────────────────────────────────────────────────────────────

    data class VisitaConglomerado(
        val muestraId: Int,
        val visitaId: Int,
        val numeroVisita: Int,
    ) : SurveyContext() {
        override val contextKey   = "VISITA_CONGLOMERADO:$muestraId:$visitaId"
        override val surveyType   = com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType.CONGLOMERADO
        override val descripcion  = "Visita #$numeroVisita — Conglomerado #$muestraId"
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VIVIENDA
    // ─────────────────────────────────────────────────────────────────────────

    data class Vivienda(
        val viviendaId: Int,
        val muestraId: Int,
        val numeroOrden: Int,
    ) : SurveyContext() {
        override val contextKey   = "VIVIENDA:$viviendaId"
        override val surveyType   = com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType.VIVIENDA
        override val descripcion  = "Vivienda #$numeroOrden"
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HOGAR
    // ─────────────────────────────────────────────────────────────────────────

    data class Hogar(
        val hogarId: Int,
        val viviendaId: Int,
        val numeroOrden: Int,
        val nombreJefe: String = "",
    ) : SurveyContext() {
        override val contextKey   = "HOGAR:$hogarId"
        override val surveyType   = com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType.VIVIENDA
        override val descripcion  = if (nombreJefe.isNotBlank()) "Hogar #$numeroOrden — $nombreJefe"
        else "Hogar #$numeroOrden"
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  VISITA DE HOGAR
    // ─────────────────────────────────────────────────────────────────────────

    data class VisitaHogar(
        val visitaId: Int,
        val hogarId: Int,
        val numeroVisita: Int,
        val nombreJefe: String = "",
    ) : SurveyContext() {
        override val contextKey   = "VISITA_HOGAR:$hogarId:$visitaId"
        override val surveyType   = com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType.VIVIENDA
        override val descripcion  = "Visita #$numeroVisita" +
                if (nombreJefe.isNotBlank()) " — $nombreJefe" else ""
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PERSONA
    // ─────────────────────────────────────────────────────────────────────────

    data class Persona(
        val personaId: Int,
        val hogarId: Int,
        val numeroOrden: Int,
        val nombreCompleto: String = "",
    ) : SurveyContext() {
        override val contextKey   = "PERSONA:$personaId"
        override val surveyType   = com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType.REENTREVISTA
        override val descripcion  = if (nombreCompleto.isNotBlank()) "Persona #$numeroOrden — $nombreCompleto"
        else "Persona #$numeroOrden"
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  COMPANION — reconstruir desde contextKey (para Saver en Compose)
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        fun fromKey(key: String): SurveyContext? {
            val partes = key.split(":")
            return try {
                when (partes[0]) {
                    "CONGLOMERADO"          -> Conglomerado(partes[1].toInt())
                    "VISITA_CONGLOMERADO"   -> VisitaConglomerado(partes[1].toInt(), partes[2].toInt(), 0)
                    "VIVIENDA"              -> Vivienda(partes[1].toInt(), 0, 0)
                    "HOGAR"                 -> Hogar(partes[1].toInt(), 0, 0)
                    "VISITA_HOGAR"          -> VisitaHogar(partes[2].toInt(), partes[1].toInt(), 0)
                    "PERSONA"               -> Persona(partes[1].toInt(), 0, 0)
                    else                    -> null
                }
            } catch (_: Exception) { null }
        }
    }
}