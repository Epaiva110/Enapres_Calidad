package com.minedu.gob.pe.enaprescalidad.surveys

import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.surveys.models.Survey

enum class SurveyEncuestaStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}

data class SurveyEncuestaProgress(
    val status: SurveyEncuestaStatus,
    val percent: Int = 0,
)

object SurveyProgressHelper {

    fun isCompleted(respuestas: Map<String, Any?>): Boolean {
        val v = respuestas[SurveyCompletion.COMPLETED_VARIABLE]
        return v == true || v == "true"
    }

    fun computeProgress(
        survey: Survey,
        respuestas: Map<String, Any?>,
        evaluator: ConditionEvaluator = ConditionEvaluator(),
    ): SurveyEncuestaProgress {
        if (isCompleted(respuestas)) {
            return SurveyEncuestaProgress(SurveyEncuestaStatus.COMPLETED, 100)
        }

        val required = collectRequiredKeys(survey.paginas, evaluator, respuestas)
        if (required.isEmpty()) {
            return if (respuestas.isEmpty()) {
                SurveyEncuestaProgress(SurveyEncuestaStatus.NOT_STARTED, 0)
            } else {
                SurveyEncuestaProgress(SurveyEncuestaStatus.IN_PROGRESS, 0)
            }
        }

        val answered = required.count { key -> isAnswered(respuestas[key]) }
        val percent = ((answered.toFloat() / required.size) * 100).toInt().coerceIn(0, 99)

        return when {
            answered == 0 -> SurveyEncuestaProgress(SurveyEncuestaStatus.NOT_STARTED, 0)
            else -> SurveyEncuestaProgress(SurveyEncuestaStatus.IN_PROGRESS, percent)
        }
    }

    fun collectRequiredKeys(
        paginas: List<Pagina>,
        evaluator: ConditionEvaluator,
        respuestas: Map<String, Any?>,
    ): Set<String> {
        val keys = mutableSetOf<String>()
        paginas.forEach { pagina ->
            pagina.preguntas.forEach { preg ->
                collectPreguntaKeys(preg, respuestas, evaluator, keys)
            }
            val minObs = 0 // observación por sección se valida al avanzar, no en % global
            if (minObs > 0) {
                keys.add("OBS_${pagina.seccion_id}")
            }
        }
        return keys
    }

    private fun collectPreguntaKeys(
        preg: Pregunta,
        respuestas: Map<String, Any?>,
        evaluator: ConditionEvaluator,
        keys: MutableSet<String>,
    ) {
        if (preg.show_if != null && !evaluator.evaluate(preg.show_if, respuestas)) return

        when (preg.type.lowercase()) {
            "info" -> return

            "matrix", "matrix_scale", "matrix_detail" -> {
                if (!preg.required) return
                preg.options?.forEach { fila ->
                    val clave = fila.variable ?: fila.value
                    if (!clave.isNullOrEmpty()) {
                        keys.add("${preg.variable}_$clave")
                    }
                    fila.detail_questions?.forEach { sub ->
                        collectPreguntaKeys(sub, respuestas, evaluator, keys)
                    }
                }
            }

            else -> {
                if (preg.required) keys.add(preg.variable)
                val valorPadre = respuestas[preg.variable]
                preg.options?.forEach { opcion ->
                    val sel = when (valorPadre) {
                        is List<*> -> valorPadre.map { it.toString() }.contains(opcion.value?.toString())
                        else -> valorPadre?.toString() == opcion.value?.toString()
                    }
                    if (sel) {
                        opcion.detail_questions?.forEach { sub ->
                            collectPreguntaKeys(sub, respuestas, evaluator, keys)
                        }
                    }
                }
            }
        }
    }

    fun isAnswered(valor: Any?): Boolean = when (valor) {
        null -> false
        is String -> valor.isNotBlank()
        is List<*> -> valor.isNotEmpty()
        else -> true
    }
}
