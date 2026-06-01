package com.minedu.gob.pe.enaprescalidad.surveys.models

// ─────────────────────────────────────────────────────────────────────────────
//  SEVERIDAD DE CONSTRAINT
// ─────────────────────────────────────────────────────────────────────────────

enum class ValidationSeverity { ERROR, WARNING }

data class ConstraintResult(
    val variable: String,
    val severity: ValidationSeverity,
    val message: String,
)

// ─────────────────────────────────────────────────────────────────────────────
//  ESTADOS
// ─────────────────────────────────────────────────────────────────────────────

enum class EstadoEncuesta(val label: String) {
    NUEVO("Nuevo"),
    COMPLETO("Completo"),
    INCOMPLETO("Incompleto"),
    RECHAZO("Rechazo"),
    AUSENTE("Ausente"),
}

enum class ResultadoVisita(val label: String) {
    COMPLETO("Completo"),
    AUSENTE("Ausente"),
    RECHAZO("Rechazo"),
    PENDIENTE("Pendiente"),
}

// ─────────────────────────────────────────────────────────────────────────────
//  TIPOS DE PREGUNTA
// ─────────────────────────────────────────────────────────────────────────────

object TipoPregunta {
    const val INFO             = "info"
    const val TEXT             = "text"
    const val NUMBER           = "number"
    const val DECIMAL          = "decimal"
    const val DATE             = "date"
    const val TIME             = "time"
    const val DATETIME         = "datetime"
    const val SINGLE           = "single"
    const val MULTIPLE         = "multiple"
    const val MULTIPLE_BINARY  = "multiple_binary"
    const val MATRIX           = "matrix"
    const val MATRIX_SCALE     = "matrix_scale"
    const val MATRIX_DETAIL    = "matrix_detail"
    const val LIKERT           = "likert"
    const val SLIDER           = "slider"
    const val RANKING          = "ranking"
    const val PHOTO            = "photo"
    const val GPS              = "gps"
    const val ENTITY_HOGAR     = "entity_hogar"
    const val ENTITY_PERSONA   = "entity_persona"
    const val ENTITY_VISITA    = "entity_visita"

    fun esEntidad(type: String) = type.lowercase() in setOf(
        ENTITY_HOGAR, ENTITY_PERSONA, ENTITY_VISITA
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY
// ─────────────────────────────────────────────────────────────────────────────

data class Survey(
    val survey_id: String,
    val title: String,
    val version: String = "1.0.0",
    val config: SurveyConfig,
    val paginas: List<Pagina>,
)

data class SurveyConfig(
    val color_resaltado: String = "#1565C0",
    val min_caracteres_observacion: Int = 0,
    val guardar_automatico: Boolean = true,
    val mostrar_progreso: Boolean = true,
)

data class Pagina(
    val id_pagina: Int,
    val seccion_id: String,
    val titulo_seccion: String,
    val titulo: String,
    val preguntas: List<Pregunta>,
)

// ─────────────────────────────────────────────────────────────────────────────
//  PREGUNTA
// ─────────────────────────────────────────────────────────────────────────────

data class EntityConfig(
    val min_registros: Int = 1,
    val max_registros: Int? = null,
    val nivel_padre: String = "vivienda",
    val campos_inline: List<String>? = null,
)

data class Pregunta(
    val id: String? = null,
    val variable: String,
    val type: String,
    val label: String,
    val required: Boolean = false,
    val hint: String? = null,
    val options: List<SurveyOption>? = null,
    val min_length: Int? = null,
    val max_length: Int? = null,
    val min_value: Double? = null,
    val max_value: Double? = null,
    val step: Double? = 1.0,
    val scale_min: Int? = 1,
    val scale_max: Int? = 5,
    val scale_labels: List<String>? = null,
    val likert_type: String? = "stars",
    val likert_count: Int? = 5,
    val max_photos: Int? = 1,
    val allow_gallery: Boolean? = false,
    val allow_other: Boolean? = false,
    val allow_skip: Boolean? = false,
    val allow_manual: Boolean? = false,
    val show_if: ConditionNode? = null,
    val jump_to_page: Int? = null,
    val constraints: List<ConstraintRule>? = null,
    val entity_config: EntityConfig? = null,
) {
    val esModuloEntidad: Boolean get() = TipoPregunta.esEntidad(type)
}

data class SurveyOption(
    val value: String? = null,
    val variable: String? = null,
    val label: String,
    val jump_to_page: Int? = null,
    val is_none: Boolean? = false,
    val is_other: Boolean? = false,
    val disabled_if_cols: List<String>? = null,
    val detail_questions: List<Pregunta>? = null,
    val detail_display: String? = "dialog",
    val open_detail_if_selected: Boolean = false,
    val detail_trigger_value: String? = "1",
)

data class ConstraintRule(
    val trigger_if: ConditionNode,
    val severity: ValidationSeverity,
    val message: String,
)

data class SurveyResponse(
    val survey_id: String,
    val answers: MutableMap<String, Any?> = mutableMapOf(),
    var updated_at: Long = System.currentTimeMillis(),
)

// ─────────────────────────────────────────────────────────────────────────────
//  CONDICIONES
// ─────────────────────────────────────────────────────────────────────────────

sealed interface ConditionNode

data class ConditionGroup(
    val logic: String,
    val conditions: List<ConditionNode>,
) : ConditionNode

data class ConditionRule(
    val variable: String,
    val operator: String,
    val value: Any? = null,
) : ConditionNode

data class GroupConditionRule(
    val variables: List<String>,
    val operator: String,
    val value: Any? = null,
    val count: Int? = null,
) : ConditionNode

// ─────────────────────────────────────────────────────────────────────────────
//  EVALUADOR
// ─────────────────────────────────────────────────────────────────────────────

class ConditionEvaluator {

    fun evaluate(node: ConditionNode?, answers: Map<String, Any?>): Boolean {
        if (node == null) return true
        return when (node) {
            is ConditionGroup     -> evaluateGroup(node, answers)
            is ConditionRule      -> evaluateSimpleRule(node, answers)
            is GroupConditionRule -> evaluateGroupRule(node, answers)
        }
    }

    private fun evaluateGroup(group: ConditionGroup, answers: Map<String, Any?>): Boolean =
        when (group.logic.uppercase()) {
            "AND" -> group.conditions.all { evaluate(it, answers) }
            "OR"  -> group.conditions.any { evaluate(it, answers) }
            "NOT" -> group.conditions.none { evaluate(it, answers) }
            else  -> false
        }

    private fun evaluateSimpleRule(rule: ConditionRule, answers: Map<String, Any?>): Boolean {
        val raw     = answers[rule.variable]
        val rawStr  = raw?.toString() ?: ""
        val ruleStr = rule.value?.toString() ?: ""
        return when (rule.operator.lowercase()) {
            "is_null"      -> raw == null
            "not_null"     -> raw != null
            "is_empty"     -> rawStr.isEmpty()
            "not_empty"    -> rawStr.isNotEmpty()
            "eq"           -> rawStr == ruleStr
            "neq"          -> rawStr != ruleStr
            "gt"           -> compareNum(raw, rule.value) { it > 0 }
            "gte"          -> compareNum(raw, rule.value) { it >= 0 }
            "lt"           -> compareNum(raw, rule.value) { it < 0 }
            "lte"          -> compareNum(raw, rule.value) { it <= 0 }
            "contains"     -> rawStr.contains(ruleStr, ignoreCase = true)
            "not_contains" -> !rawStr.contains(ruleStr, ignoreCase = true)
            "starts_with"  -> rawStr.startsWith(ruleStr)
            "ends_with"    -> rawStr.endsWith(ruleStr)
            "in"           -> toStringList(rule.value).contains(rawStr)
            "not_in"       -> !toStringList(rule.value).contains(rawStr)
            "regex"        -> runCatching { rawStr.matches(Regex(ruleStr)) }.getOrElse { false }
            else           -> false
        }
    }

    private fun evaluateGroupRule(rule: GroupConditionRule, answers: Map<String, Any?>): Boolean {
        val values    = rule.variables.map { answers[it] }
        val strValues = values.map { it?.toString() }
        val targetStr = rule.value?.toString()
        return when (rule.operator.lowercase()) {
            "any_eq"    -> strValues.any { it == targetStr }
            "all_eq"    -> strValues.all { it == targetStr }
            "none_eq"   -> strValues.none { it == targetStr }
            "all_null"  -> values.all { it == null }
            "any_null"  -> values.any { it == null }
            "count_eq"  -> strValues.count { it == targetStr } == (rule.count ?: 0)
            "count_gte" -> strValues.count { it == targetStr } >= (rule.count ?: 0)
            "sum_gt"    -> numericOp(values) { it > targetDouble(rule) }
            "sum_gte"   -> numericOp(values) { it >= targetDouble(rule) }
            "sum_lt"    -> numericOp(values) { it < targetDouble(rule) }
            "sum_lte"   -> numericOp(values) { it <= targetDouble(rule) }
            "sum_eq"    -> numericOp(values) { it == targetDouble(rule) }
            "avg_gt"    -> avgOp(values) { it > targetDouble(rule) }
            "avg_gte"   -> avgOp(values) { it >= targetDouble(rule) }
            "avg_lt"    -> avgOp(values) { it < targetDouble(rule) }
            "avg_lte"   -> avgOp(values) { it <= targetDouble(rule) }
            else        -> false
        }
    }

    private fun compareNum(a: Any?, b: Any?, op: (Int) -> Boolean): Boolean {
        val na = a?.toString()?.toDoubleOrNull() ?: return false
        val nb = b?.toString()?.toDoubleOrNull() ?: return false
        return op(na.compareTo(nb))
    }

    private fun toStringList(value: Any?): List<String> = when (value) {
        is List<*> -> value.map { it.toString() }
        is String  -> value.split(",").map { it.trim() }
        else       -> emptyList()
    }

    private fun targetDouble(rule: GroupConditionRule) =
        rule.value?.toString()?.toDoubleOrNull() ?: 0.0

    private fun numericOp(values: List<Any?>, check: (Double) -> Boolean): Boolean {
        val sum = values.mapNotNull { it?.toString()?.toDoubleOrNull() }.sum()
        return check(sum)
    }

    private fun avgOp(values: List<Any?>, check: (Double) -> Boolean): Boolean {
        val nums = values.mapNotNull { it?.toString()?.toDoubleOrNull() }
        return if (nums.isEmpty()) false else check(nums.average())
    }
}