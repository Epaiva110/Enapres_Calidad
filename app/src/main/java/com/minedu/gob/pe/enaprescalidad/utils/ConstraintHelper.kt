package com.minedu.gob.pe.enaprescalidad.surveys.ui.util

import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConstraintResult
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConstraintRule
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.surveys.models.ValidationSeverity

// ─────────────────────────────────────────────────────────────────────────────
//  CONSTRAINT HELPER  (capa UI)
//
//  Provee funciones puras para que cada componente de pregunta pueda:
//
//    1. Saber si está completamente BLOQUEADO por un ERROR de constraint.
//       → La UI debe deshabilitar la interacción (no se puede seleccionar
//         ni escribir). Equivalente a `enabled = false` en el componente.
//
//    2. Obtener el primer mensaje de WARNING activo para la variable.
//       → La UI lo muestra como texto de advertencia debajo o encima de
//         la pregunta. No bloquea; el usuario puede ignorarlo y avanzar.
//
//  SEMÁNTICA CONFIRMADA:
//    severity = ERROR   → bloquea la selección Y el avance de página.
//    severity = WARNING → solo muestra el mensaje; no bloquea nada.
//
//  CÓMO USARLO EN UN COMPONENTE DE PREGUNTA:
//
//    val bloqueado = ConstraintHelper.isBlocked(pregunta.variable, variablesBlockedByError)
//    val aviso     = ConstraintHelper.warningMessage(pregunta.variable, constraintResults)
//
//    // El componente se deshabilita si está en modo solo lectura O bloqueado por ERROR
//    val enabled = !soloLectura && !bloqueado
//
//    if (aviso != null) {
//        Text(text = aviso, color = MaterialTheme.colorScheme.error, style = bodySmall)
//    }
// ─────────────────────────────────────────────────────────────────────────────

object ConstraintHelper {

    /**
     * Indica si la variable está BLOQUEADA por al menos un constraint con severity=ERROR.
     * Cuando es `true` el componente debe ponerse en `enabled = false`.
     */
    fun isBlocked(
        variable: String,
        variablesBlockedByError: Set<String>,
    ): Boolean = variable in variablesBlockedByError

    /**
     * Devuelve el primer mensaje de WARNING activo para la variable, o `null` si no hay.
     * El componente puede mostrarlo como texto de advertencia sin alterar su estado habilitado.
     */
    fun warningMessage(
        variable: String,
        constraintResults: List<ConstraintResult>,
    ): String? = constraintResults
        .firstOrNull { it.variable == variable && it.severity == ValidationSeverity.WARNING }
        ?.message

    /**
     * Devuelve todos los mensajes de ERROR activos para la variable.
     * Útil para mostrar múltiples razones de bloqueo en la UI si se desea.
     */
    fun errorMessages(
        variable: String,
        constraintResults: List<ConstraintResult>,
    ): List<String> = constraintResults
        .filter { it.variable == variable && it.severity == ValidationSeverity.ERROR }
        .map { it.message }

    /**
     * Evalúa de forma puntual las constraints de una pregunta contra el mapa de respuestas
     * actual. Útil en componentes que reciben la pregunta completa y quieren evaluar sus
     * propias constraints sin depender del ViewModel.
     *
     * Nota: en la mayoría de casos es preferible usar los sets/listas del UiState
     * (variablesBlockedByError, constraintResults) que ya vienen pre-evaluados por el VM.
     * Esta función es para casos de evaluación local puntual (p. ej. sub-preguntas inline).
     */
    fun evaluarConstraints(
        pregunta: Pregunta,
        respuestas: Map<String, Any?>,
        evaluator: ConditionEvaluator,
    ): List<ConstraintResult> {
        val reglas = pregunta.constraints ?: return emptyList()
        return reglas.mapNotNull { regla ->
            if (evaluator.evaluate(regla.trigger_if, respuestas)) {
                ConstraintResult(
                    variable = pregunta.variable,
                    severity = regla.severity,
                    message  = regla.message,
                )
            } else null
        }
    }

    /**
     * Conveniencia: devuelve `true` si al evaluar las constraints de la pregunta
     * hay al menos un ERROR activo (la opción/campo no debería poder seleccionarse).
     */
    fun tieneErrorActivo(
        pregunta: Pregunta,
        respuestas: Map<String, Any?>,
        evaluator: ConditionEvaluator,
    ): Boolean = evaluarConstraints(pregunta, respuestas, evaluator)
        .any { it.severity == ValidationSeverity.ERROR }

    /**
     * Conveniencia: devuelve `true` si al evaluar las constraints de la pregunta
     * hay al menos un WARNING activo (mostrar aviso, no bloquear).
     */
    fun tieneWarningActivo(
        pregunta: Pregunta,
        respuestas: Map<String, Any?>,
        evaluator: ConditionEvaluator,
    ): Boolean = evaluarConstraints(pregunta, respuestas, evaluator)
        .any { it.severity == ValidationSeverity.WARNING }
}