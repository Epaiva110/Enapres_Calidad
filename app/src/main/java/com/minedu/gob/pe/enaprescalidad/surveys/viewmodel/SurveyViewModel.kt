package com.minedu.gob.pe.enaprescalidad.surveys.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SurveyResponseDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SurveyResponseEntity
import com.minedu.gob.pe.enaprescalidad.surveys.SurveyCompletion
import com.minedu.gob.pe.enaprescalidad.surveys.adapter.SurveyGson
import com.minedu.gob.pe.enaprescalidad.surveys.models.*
import com.minedu.gob.pe.enaprescalidad.surveys.repository.SurveyVersionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val OTRO_MIN_CHARS = 3

// ─────────────────────────────────────────────────────────────────────────────
//  UI STATE
// ─────────────────────────────────────────────────────────────────────────────

data class SurveyUiState(
    val survey: Survey? = null,
    val respuestas: Map<String, Any?> = emptyMap(),
    val paginaActual: Int = 0,
    val historial: List<Int> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val saveMessage: String? = null,
    val showObsDialog: Boolean = false,
    val variablesConError: Set<String> = emptySet(),
    val constraintResults: List<ConstraintResult> = emptyList(),
    val surveyVersion: String = "",
    val surveyOrigen: String = "",
) {
    val pagina: Pagina?        get() = survey?.paginas?.getOrNull(paginaActual)
    val isLastPage: Boolean    get() = survey != null && paginaActual == survey.paginas.lastIndex
    val totalPaginas: Int      get() = survey?.paginas?.size ?: 0

    val obsValida: Boolean get() {
        val p   = pagina ?: return false
        val min = survey?.config?.min_caracteres_observacion ?: 0
        if (min == 0) return true
        return (respuestas["OBS_${p.seccion_id}"]?.toString()?.trim() ?: "").length >= min
    }

    fun progreso(paginasVisibles: List<Int>): Float {
        val id  = pagina?.id_pagina ?: return 0f
        val idx = paginasVisibles.indexOf(id)
        return if (idx == -1 || paginasVisibles.isEmpty()) 0f
        else (idx + 1f) / paginasVisibles.size
    }

    val variablesBlockedByError: Set<String>
        get() = constraintResults.filter { it.severity == ValidationSeverity.ERROR }.map { it.variable }.toSet()

    val warningMessages: List<ConstraintResult>
        get() = constraintResults.filter { it.severity == ValidationSeverity.WARNING }

    fun obtenerVariableEnFoco(evaluator: ConditionEvaluator): String {
        val p = pagina ?: return ""
        fun ok(v: Any?) = when (v) { null -> false; is String -> v.isNotBlank(); is List<*> -> v.isNotEmpty(); else -> true }
        for (preg in p.preguntas) {
            if (!preg.required) continue
            if (preg.show_if != null && !evaluator.evaluate(preg.show_if, respuestas)) continue
            when (preg.type.lowercase()) {
                "matrix", "matrix_scale", "matrix_detail" ->
                    preg.options?.forEach { f ->
                        val sv = "${preg.variable}_${f.variable ?: f.value ?: ""}"
                        if (!ok(respuestas[sv])) return sv
                    }
                else -> if (!ok(respuestas[preg.variable])) return preg.variable
            }
        }
        return ""
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val responseDao: SurveyResponseDao,
    private val versionRepository: SurveyVersionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    val evaluator = ConditionEvaluator()

    private lateinit var surveyContext: SurveyContext
    private lateinit var survey: Survey

    // ── INIT ─────────────────────────────────────────────────────────────────

    fun init(context: SurveyContext) {
        if (::surveyContext.isInitialized && surveyContext == context && _uiState.value.survey != null) return
        surveyContext = context

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val resolved = versionRepository.resolveJson(context)
                survey = SurveyGson.instance.fromJson(resolved.json, Survey::class.java)

                val mapa = responseDao
                    .obtenerRespuestas(context.surveyType.name, context.contextKey)
                    .associate { it.variable to deserializarValor(it.valor) }

                _uiState.update {
                    it.copy(
                        survey            = survey,
                        respuestas        = mapa,
                        paginaActual      = 0,
                        historial         = emptyList(),
                        isCompleted       = false,
                        variablesConError = emptySet(),
                        constraintResults = emptyList(),
                        showObsDialog     = false,
                        error             = null,
                        isLoading         = false,
                        surveyVersion     = resolved.version,
                        surveyOrigen      = resolved.origen,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al cargar encuesta: ${e.message}") }
            }
        }
    }

    fun consumeCompleted()   = _uiState.update { it.copy(isCompleted = false) }
    fun consumeSaveMessage() = _uiState.update { it.copy(saveMessage = null) }
    fun clearError()         = _uiState.update { it.copy(error = null) }

    // ── UPDATE ANSWER — persistencia en tiempo real ───────────────────────────

    fun onUpdateAnswer(variable: String, valor: Any?) {
        if (variable == SurveyCompletion.COMPLETED_VARIABLE) return

        val nuevas = _uiState.value.respuestas.toMutableMap().apply {
            if (valor == null) remove(variable) else put(variable, valor)
        }

        val antesDeDepurar      = nuevas.keys.toSet()
        purgarRespuestasFantasmas(nuevas)
        val variablesEliminadas = antesDeDepurar - nuevas.keys
        val constraints         = evaluarConstraintsActivas(nuevas)

        _uiState.update { state ->
            val errores = if (variable in state.variablesConError && estaRespondida(nuevas[variable]))
                state.variablesConError - variable else state.variablesConError
            state.copy(respuestas = nuevas, variablesConError = errores, constraintResults = constraints)
        }

        viewModelScope.launch {
            val type = surveyContext.surveyType.name
            val key  = surveyContext.contextKey
            if (nuevas.containsKey(variable)) {
                responseDao.upsertRespuesta(
                    SurveyResponseEntity(type, key, variable, serializarValor(nuevas[variable]))
                )
            } else {
                responseDao.borrarVariable(type, key, variable)
            }
            if (variablesEliminadas.isNotEmpty()) {
                responseDao.borrarVariables(type, key, variablesEliminadas.toList())
            }
        }
    }

    // ── VALIDACIÓN ────────────────────────────────────────────────────────────

    fun calcularVariablesConError(): Set<String> {
        val p    = _uiState.value.pagina ?: return emptySet()
        val resp = _uiState.value.respuestas
        val err  = mutableSetOf<String>()

        fun recolectar(preg: Pregunta) {
            if (preg.show_if != null && !evaluator.evaluate(preg.show_if, resp)) return
            when (preg.type.lowercase()) {
                "info" -> return
                "matrix", "matrix_scale", "matrix_detail" -> {
                    if (!preg.required) return
                    preg.options?.forEach { f ->
                        val sv = "${preg.variable}_${f.variable ?: f.value ?: ""}"
                        if (!estaRespondida(resp[sv])) err += sv
                    }
                }
                TipoPregunta.ENTITY_HOGAR,
                TipoPregunta.ENTITY_PERSONA,
                TipoPregunta.ENTITY_VISITA -> {
                    if (!preg.required) return
                    val conteo = resp[preg.variable]?.toString()?.toIntOrNull() ?: 0
                    if (conteo < (preg.entity_config?.min_registros ?: 1)) err += preg.variable
                }
                else -> {
                    if (preg.required) {
                        val v = resp[preg.variable]
                        when {
                            !estaRespondida(v) -> err += preg.variable
                            // "Otro" seleccionado pero especifique vacío o corto
                            v?.toString() == "__otro__" -> {
                                val especifique = resp["${preg.variable}_otro"]?.toString()?.trim() ?: ""
                                if (especifique.length < OTRO_MIN_CHARS) err += preg.variable
                            }
                        }
                    }
                    val vp = resp[preg.variable]
                    preg.options?.forEach { op ->
                        val sel = when (vp) {
                            is List<*> -> vp.map { it.toString() }.contains(op.value?.toString())
                            else       -> vp?.toString() == op.value?.toString()
                        }
                        if (sel) op.detail_questions?.forEach { if (it.required) recolectar(it) }
                    }
                }
            }
        }

        p.preguntas.forEach { recolectar(it) }

        // Observaciones: sin mínimo obligatorio — nunca bloquean el avance

        // Constraints ERROR bloquean igual que required
        evaluarConstraintsActivas(resp)
            .filter { it.severity == ValidationSeverity.ERROR }
            .forEach { err += it.variable }

        return err
    }

    // ── NAVEGACIÓN ────────────────────────────────────────────────────────────

    fun onNextPage() {
        // Limpiar primero para que LaunchedEffect detecte el nuevo valor
        _uiState.update { it.copy(error = null, variablesConError = emptySet()) }
        val errores = calcularVariablesConError()
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(error = buildErrorMessage(errores), variablesConError = errores) }
            return
        }
        navegarSiguiente()
    }

    fun onNextPageReadOnly() = navegarSiguiente(ignorarValidacion = true)

    private fun navegarSiguiente(ignorarValidacion: Boolean = false) {
        val pag             = _uiState.value.pagina ?: return
        val paginasVisibles = calcularPaginasVisibles().toList().sorted()
        val idxActual       = survey.paginas.indexOfFirst { it.id_pagina == pag.id_pagina }
        val siguienteId     = paginasVisibles.firstOrNull { id ->
            survey.paginas.indexOfFirst { it.id_pagina == id } > idxActual
        }

        if (siguienteId == null) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                val final = _uiState.value.respuestas.toMutableMap().apply {
                    put(SurveyCompletion.COMPLETED_VARIABLE, true)
                }
                guardarTodoEnRoom(final)
                _uiState.update { it.copy(respuestas = final, isSaving = false, isCompleted = true) }
            }
        } else {
            val nuevaPos = survey.paginas.indexOfFirst { it.id_pagina == siguienteId }
            _uiState.update {
                it.copy(
                    paginaActual      = nuevaPos,
                    historial         = it.historial + it.paginaActual,
                    constraintResults = evaluarConstraintsActivas(it.respuestas),
                )
            }
        }
    }

    fun onBackPage() {
        val hist = _uiState.value.historial
        if (hist.isEmpty()) return
        _uiState.update {
            it.copy(
                paginaActual      = hist.last(),
                historial         = hist.dropLast(1),
                constraintResults = evaluarConstraintsActivas(it.respuestas),
            )
        }
    }

    fun calcularPaginasVisibles(
        respuestas: Map<String, Any?> = _uiState.value.respuestas,
    ): Set<Int> {
        val visibles = mutableSetOf<Int>()
        val mapa     = survey.paginas.associateBy { it.id_pagina }
        var idActual = survey.paginas.firstOrNull()?.id_pagina ?: return visibles

        while (mapa.containsKey(idActual)) {
            if (idActual in visibles) break
            visibles += idActual
            val pag = mapa[idActual]!!
            var salto: Int? = null

            outer@ for (preg in pag.preguntas) {
                val ans = respuestas[preg.variable]
                for (opt in preg.options ?: emptyList()) {
                    val sel = when (ans) {
                        is List<*> -> ans.map { it.toString() }.contains(opt.value?.toString())
                        else       -> ans?.toString() == opt.value?.toString()
                    }
                    if (sel && opt.jump_to_page != null) { salto = opt.jump_to_page; break@outer }
                }
            }
            if (salto == null) {
                for (preg in pag.preguntas) {
                    if (preg.jump_to_page != null && respuestas.containsKey(preg.variable)) {
                        val vis = preg.show_if == null || evaluator.evaluate(preg.show_if, respuestas)
                        if (vis) { salto = preg.jump_to_page; break }
                    }
                }
            }
            idActual = salto ?: run {
                val idx = survey.paginas.indexOf(pag)
                if (idx < survey.paginas.lastIndex) survey.paginas[idx + 1].id_pagina else return visibles
            }
        }
        return visibles
    }

    // ── OBSERVACIÓN ───────────────────────────────────────────────────────────

    fun openObsDialog()  = _uiState.update { it.copy(showObsDialog = true) }
    fun closeObsDialog() = _uiState.update { it.copy(showObsDialog = false) }

    fun onGuardarObservacion(texto: String) {
        val p = _uiState.value.pagina ?: return
        onUpdateAnswer("OBS_${p.seccion_id}", texto)
        closeObsDialog()
    }

    // ── GUARDAR MANUAL ────────────────────────────────────────────────────────

    fun onGuardar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            guardarTodoEnRoom(_uiState.value.respuestas)
            val errores = calcularVariablesConError()
            val mensaje = if (errores.isEmpty()) "Datos guardados con éxito"
            else "Guardado, pero hay preguntas sin contestar en esta página"
            _uiState.update {
                it.copy(
                    isSaving          = false,
                    saveMessage       = mensaje,
                    variablesConError = if (errores.isNotEmpty()) errores else it.variablesConError,
                )
            }
        }
    }

    // ── CONSTRAINTS ───────────────────────────────────────────────────────────

    private fun evaluarConstraintsActivas(
        respuestas: Map<String, Any?> = _uiState.value.respuestas,
    ): List<ConstraintResult> {
        val pagina = _uiState.value.survey?.paginas?.getOrNull(_uiState.value.paginaActual)
            ?: return emptyList()
        val resultados = mutableListOf<ConstraintResult>()

        fun procesarPregunta(preg: Pregunta) {
            if (preg.show_if != null && !evaluator.evaluate(preg.show_if, respuestas)) return
            preg.constraints?.forEach { regla ->
                if (evaluator.evaluate(regla.trigger_if, respuestas))
                    resultados += ConstraintResult(preg.variable, regla.severity, regla.message)
            }
            val vp = respuestas[preg.variable]
            preg.options?.forEach { op ->
                val sel = when (vp) {
                    is List<*> -> vp.map { it.toString() }.contains(op.value?.toString())
                    else       -> vp?.toString() == op.value?.toString()
                }
                if (sel) op.detail_questions?.forEach { procesarPregunta(it) }
            }
        }

        pagina.preguntas.forEach { procesarPregunta(it) }
        return resultados
    }

    // ── PURGA ─────────────────────────────────────────────────────────────────

    private fun purgarRespuestasFantasmas(respuestas: MutableMap<String, Any?>) {
        val paginasVisibles = calcularPaginasVisibles(respuestas)
        for (pagina in survey.paginas) {
            val paginaVisible = pagina.id_pagina in paginasVisibles
            for (preg in pagina.preguntas) {
                val pregVisible = paginaVisible &&
                        (preg.show_if == null || evaluator.evaluate(preg.show_if, respuestas))
                if (!pregVisible) eliminarArbol(preg, respuestas)
                else limpiarDetallesInternos(preg, respuestas)
            }
        }
    }

    private fun eliminarArbol(preg: Pregunta, respuestas: MutableMap<String, Any?>) {
        respuestas.remove(preg.variable)
        preg.options?.forEach { op ->
            val clave = op.variable ?: op.value
            if (!clave.isNullOrEmpty()) respuestas.remove("${preg.variable}_$clave")
            op.detail_questions?.forEach { eliminarArbol(it, respuestas) }
        }
    }

    private fun limpiarDetallesInternos(preg: Pregunta, respuestas: MutableMap<String, Any?>) {
        val vp = respuestas[preg.variable]
        preg.options?.forEach { op ->
            val sel = when (vp) {
                is List<*> -> vp.map { it.toString() }.contains(op.value?.toString())
                else       -> vp?.toString() == op.value?.toString()
            }
            op.detail_questions?.forEach { sub ->
                val subVisible = sel && (sub.show_if == null || evaluator.evaluate(sub.show_if, respuestas))
                if (!subVisible) eliminarArbol(sub, respuestas)
                else limpiarDetallesInternos(sub, respuestas)
            }
        }
    }

    // ── ROOM ──────────────────────────────────────────────────────────────────

    private suspend fun guardarTodoEnRoom(mapa: Map<String, Any?>) {
        val type = surveyContext.surveyType.name
        val key  = surveyContext.contextKey
        responseDao.upsertRespuestas(
            mapa.map { (k, v) -> SurveyResponseEntity(type, key, k, serializarValor(v)) }
        )
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private fun estaRespondida(valor: Any?) = when (valor) {
        null -> false; is String -> valor.isNotBlank(); is List<*> -> valor.isNotEmpty(); else -> true
    }

    private fun serializarValor(value: Any?): String = SurveyGson.instance.toJson(value)

    private fun deserializarValor(raw: String): Any? {
        if (raw.isBlank()) return null
        return try { SurveyGson.instance.fromJson(raw, Any::class.java) } catch (_: Exception) { raw }
    }

    private fun buildErrorMessage(errores: Set<String>): String {
        val p      = _uiState.value.pagina ?: return "Completa las preguntas requeridas."
        val partes = mutableListOf<String>()
        if (errores.any { !it.startsWith("OBS_") }) partes += "preguntas requeridas pendientes"
        if (errores.any { it.startsWith("OBS_") }) partes += "observación de sección"
        return if (partes.isEmpty()) "Completa las preguntas requeridas."
        else "Falta: ${partes.joinToString(" y ")}."
    }
}