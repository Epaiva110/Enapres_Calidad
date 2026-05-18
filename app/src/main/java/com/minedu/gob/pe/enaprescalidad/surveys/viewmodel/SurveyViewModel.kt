package com.minedu.gob.pe.enaprescalidad.surveys.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.surveys.models.ShowCondition
import com.minedu.gob.pe.enaprescalidad.surveys.models.Survey
import com.google.gson.Gson
import com.minedu.gob.pe.enaprescalidad.data.local.dao.surveys.SurveyConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.surveys.SurveyConglomeradoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
//  UI STATE
// ─────────────────────────────────────────────────────────────────────────────

data class SurveyUiState(
    val survey: Survey? = null,
    val respuestas: Map<String, String> = emptyMap(),  // variable → valor
    val paginaActual: Int = 0,
    val historial: List<Int> = emptyList(),            // backstack de páginas
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
) {
    val pagina: Pagina? get() = survey?.paginas?.getOrNull(paginaActual)
    val isLastPage: Boolean get() = survey != null && paginaActual == survey.paginas.lastIndex
    val totalPaginas: Int get() = survey?.paginas?.size ?: 0
    val progreso: Float get() = if (totalPaginas == 0) 0f else (paginaActual + 1f) / totalPaginas

    // Variable en foco: la primera sin respuesta en la página actual
    val variableEnFoco: String get() {
        val p = pagina ?: return ""
        return p.preguntas.find { preg ->
            if (!preg.required) return@find false
            if (preg.type == "matrix") {
                preg.options?.any { respuestas[it.variable].isNullOrEmpty() } == true
            } else {
                respuestas[preg.variable].isNullOrEmpty()
            }
        }?.variable ?: ""
    }

    // Validación de la página actual
    val paginaValida: Boolean get() {
        val p = pagina ?: return false
        val obs = respuestas["OBS_${p.seccion_id}"] ?: ""
        val minObs = survey?.config?.min_caracteres_observacion ?: 10

        val preguntasOk = p.preguntas.all { preg ->
            if (!preg.required) return@all true
            when (preg.type) {
                "matrix", "matrix_scale" -> preg.options?.all {
                    !respuestas[it.variable].isNullOrEmpty()
                } ?: true
                "multiple", "multiple_binary" ->
                    respuestas[preg.variable]?.split("|")?.firstOrNull()?.isNotBlank() == true
                "gps" -> {
                    val gps = respuestas[preg.variable].orEmpty()
                    gps.contains("OMITIDO") || gps.split("|").firstOrNull()?.isNotEmpty() == true
                }
                "info" -> true
                else -> !respuestas[preg.variable].isNullOrBlank()
            }
        }
        return preguntasOk && obs.trim().length >= minObs
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  VIEW MODEL
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val dao: SurveyConglomeradoDao,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> = _uiState.asStateFlow()

    private var muestraId: Int = -1
    private var surveyId: String = ""

    // ── Inicialización ────────────────────────────────────────────────────────

    fun init(muestraId: Int, jsonString: String) {
        if (this.muestraId == muestraId) return   // ya inicializado (rotación)
        this.muestraId = muestraId

        val survey = Gson().fromJson(jsonString, Survey::class.java)
        this.surveyId = survey.survey_id

        _uiState.update { it.copy(survey = survey, isLoading = true) }

        // Restaurar respuestas guardadas en Room (reactivo)
        viewModelScope.launch {
            dao.observarRespuestas(muestraId, surveyId).collect { lista ->
                val map = lista.associate { it.variable to it.valor }
                _uiState.update { it.copy(respuestas = map, isLoading = false) }
            }
        }
    }

    // ── Respuesta a una pregunta ──────────────────────────────────────────────

    /**
     * Guarda una respuesta. Se llama desde la UI cada vez que el usuario
     * interactúa con cualquier tipo de pregunta.
     * Si guardar_automatico=true, persiste inmediatamente en Room.
     */
    fun onRespuesta(variable: String, valor: String) {
        // Actualiza la UI de forma optimista e inmediata
        _uiState.update { state ->
            state.copy(respuestas = state.respuestas + (variable to valor))
        }

        // Persiste en Room en background si está configurado
        if (_uiState.value.survey?.config?.guardar_automatico == true) {
            guardarEnRoom(variable, valor)
        }
    }

    private fun guardarEnRoom(variable: String, valor: String) {
        viewModelScope.launch {
            dao.upsert(
                SurveyConglomeradoEntity(
                    muestra_id = muestraId,
                    survey_id  = surveyId,
                    variable   = variable,
                    valor      = valor,
                )
            )
        }
    }

    // ── Navegación entre páginas ──────────────────────────────────────────────

    fun onSiguiente() {
        val state  = _uiState.value
        val pagina = state.pagina ?: return
        if (!state.paginaValida) return

        // Detectar salto condicional por respuesta
        val jumpTarget = pagina.preguntas.firstNotNullOfOrNull { preg ->
            val respuesta = state.respuestas[preg.variable]
            preg.options?.find { it.value == respuesta }?.jump_to_page
                ?: if (preg.jump_to_page != null && respuesta != null) preg.jump_to_page else null
        }

        val destino = jumpTarget ?: (state.paginaActual + 1)

        if (destino > state.survey!!.paginas.lastIndex) {
            onFinalizar()
            return
        }

        _uiState.update {
            it.copy(
                paginaActual = destino,
                historial    = it.historial + it.paginaActual,
            )
        }
    }

    fun onAnterior() {
        val historial = _uiState.value.historial
        if (historial.isEmpty()) return
        _uiState.update {
            it.copy(
                paginaActual = historial.last(),
                historial    = historial.dropLast(1),
            )
        }
    }

    // ── Guardar / Finalizar ───────────────────────────────────────────────────

    /**
     * Guarda manualmente TODAS las respuestas actuales en Room.
     * Útil si guardar_automatico=false o para asegurar consistencia antes de salir.
     */
    fun onGuardar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val respuestas = _uiState.value.respuestas
            val entidades  = respuestas.map { (variable, valor) ->
                SurveyConglomeradoEntity(
                    muestra_id = muestraId,
                    survey_id  = surveyId,
                    variable   = variable,
                    valor      = valor,
                )
            }
            dao.upsertAll(entidades)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun onFinalizar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            // Guardar todo por si acaso antes de cerrar
            onGuardar()
            // TODO: marcar la muestra como cerrada en MuestraConglomeradoEntity
            // dao.marcarCerrada(muestraId)
            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPER — Evalúa si una pregunta debe mostrarse según su show_if
// ─────────────────────────────────────────────────────────────────────────────

fun evaluarCondicion(condicion: ShowCondition?, respuestas: Map<String, String>): Boolean {
    if (condicion == null) return true
    val valorActual = respuestas[condicion.variable] ?: ""
    return when (condicion.operator) {
        "eq"     -> valorActual == condicion.value
        "neq"    -> valorActual != condicion.value
        "in"     -> condicion.value.split(",").map { it.trim() }.contains(valorActual)
        "not_in" -> !condicion.value.split(",").map { it.trim() }.contains(valorActual)
        "gt"     -> valorActual.toDoubleOrNull()?.let { it > condicion.value.toDouble() } ?: false
        "lt"     -> valorActual.toDoubleOrNull()?.let { it < condicion.value.toDouble() } ?: false
        else     -> true
    }
}