package com.minedu.gob.pe.enaprescalidad.surveys.models

data class Survey(
    val survey_id: String,
    val title: String,
    val config: SurveyConfig,
    val paginas: List<Pagina>
)

data class SurveyConfig(
    val color_resaltado: String,
    val min_caracteres_observacion: Int = 10
)

data class Pagina(
    val id_pagina: Int,
    val seccion_id: String,
    val titulo_seccion: String,
    val titulo: String,
    val preguntas: List<Pregunta>
)

data class Pregunta(
    val variable: String,
    val type: String,
    val label: String,
    val required: Boolean = false,
    val hint: String? = null,
    val options: List<SurveyOption>? = null,
    val allow_manual: Boolean? = false,
    val allow_skip: Boolean? = false
)

data class SurveyOption(
    val value: String? = null,      // Para single
    val variable: String? = null,   // Para filas de matriz o checkbox
    val label: String,
    val jump_to_page: Int? = null,
    val is_none: Boolean? = false,
    val is_other: Boolean? = false
)