package com.minedu.gob.pe.enaprescalidad.surveys.models

// ─────────────────────────────────────────────────────────────────────────────
//  MODELOS DEL CUESTIONARIO
//  Compatibles con JSON dinámico (Gson/Moshi).
//  Cada Pregunta declara su type y los campos opcionales que necesita.
// ─────────────────────────────────────────────────────────────────────────────

data class Survey(
    val survey_id: String,
    val title: String,
    val config: SurveyConfig,
    val paginas: List<Pagina>,
)

data class SurveyConfig(
    val color_resaltado: String = "#1565C0",
    val min_caracteres_observacion: Int = 10,
    val guardar_automatico: Boolean = true,    // auto-save en Room al cambiar respuesta
    val mostrar_progreso: Boolean = true,
)

data class Pagina(
    val id_pagina: Int,
    val seccion_id: String,
    val titulo_seccion: String,
    val titulo: String,
    val preguntas: List<Pregunta>,
)

// ── Tipos de pregunta soportados ──────────────────────────────────────────────
// "single"           → RadioButton (una opción)
// "multiple"         → Checkboxes (varias opciones, sin lógica binaria)
// "multiple_binary"  → Checkboxes con opción "Ninguno" y "Otro especifique"
// "matrix"           → Tabla SÍ/NO/NT con filas dinámicas
// "matrix_scale"     → Tabla con escala numérica configurable (1-5, 1-10, etc.)
// "text"             → Campo texto libre
// "number"           → Campo numérico con validación min/max
// "decimal"          → Campo decimal
// "date"             → Selector de fecha (DatePicker)
// "time"             → Selector de hora (TimePicker)
// "datetime"         → Fecha + hora
// "gps"              → Coordenadas GPS (auto + manual)
// "photo"            → Foto con cámara
// "signature"        → Firma digital
// "ranking"          → Ordenar opciones por prioridad
// "slider"           → Escala deslizante numérica
// "likert"           → Escala de satisfacción con emojis/estrellas
// "info"             → Solo texto informativo, sin respuesta

data class Pregunta(
    val variable: String,
    val type: String,
    val label: String,
    val required: Boolean = false,
    val hint: String? = null,
    val options: List<SurveyOption>? = null,

    // Para "text" y "number"
    val min_length: Int? = null,
    val max_length: Int? = null,

    // Para "number", "decimal", "slider"
    val min_value: Double? = null,
    val max_value: Double? = null,
    val step: Double? = 1.0,          // para slider

    // Para "matrix_scale"
    val scale_min: Int? = 1,
    val scale_max: Int? = 5,
    val scale_labels: List<String>? = null,  // etiquetas de los extremos ["Muy malo", "Muy bueno"]

    // Para "likert"
    val likert_type: String? = "stars",   // "stars" | "emoji" | "numbers"
    val likert_count: Int? = 5,

    // Para "photo"
    val max_photos: Int? = 1,
    val allow_gallery: Boolean? = false,

    // Para "single" / "multiple"
    val allow_other: Boolean? = false,   // agrega campo "Otro: ___"
    val allow_skip: Boolean? = false,    // permite omitir con justificación
    val allow_manual: Boolean? = false,

    // Condición de visibilidad: mostrar esta pregunta solo si otra tiene cierto valor
    val show_if: ShowCondition? = null,

    // Lógica de salto al siguiente
    val jump_to_page: Int? = null,       // salto incondicional desde la pregunta
)

data class SurveyOption(
    val value: String? = null,           // para single / multiple
    val variable: String? = null,        // para filas de matrix / multiple_binary
    val label: String,
    val jump_to_page: Int? = null,       // salto condicional si se elige esta opción
    val is_none: Boolean? = false,       // opción "Ninguno de los anteriores"
    val is_other: Boolean? = false,      // opción "Otro" con especifique
    val disabled_if_cols: List<String>? = null,  // columnas deshabilitadas en matrix
)

// Condición de visibilidad
data class ShowCondition(
    val variable: String,
    val operator: String = "eq",         // "eq" | "neq" | "in" | "not_in" | "gt" | "lt"
    val value: String,                   // valor o lista separada por coma para "in"
)