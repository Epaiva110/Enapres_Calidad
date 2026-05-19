package com.minedu.gob.pe.enaprescalidad.surveys.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
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
    val respuestas: Map<String, String> = emptyMap(),
    val paginaActual: Int = 0,
    val historial: List<Int> = emptyList(),
    val paginasVisitadas: Set<Int> = emptySet(), // páginas que el usuario sí recorrió
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
) {
    val pagina: Pagina? get() = survey?.paginas?.getOrNull(paginaActual)
    val isLastPage: Boolean get() = survey != null && paginaActual == survey.paginas.lastIndex
    val totalPaginas: Int get() = survey?.paginas?.size ?: 0
    val progreso: Float get() = if (totalPaginas == 0) 0f else (paginaActual + 1f) / totalPaginas

    // Primera pregunta requerida sin respuesta en la página actual
    val variableEnFoco: String get() {
        val p = pagina ?: return ""
        return p.preguntas.find { preg ->
            if (!preg.required) return@find false
            if (!evaluarCondicion(preg.show_if, respuestas)) return@find false
            when (preg.type) {
                "matrix", "matrix_scale" ->
                    preg.options?.any { respuestas[it.variable].isNullOrEmpty() } == true
                "info" -> false
                else   -> respuestas[preg.variable].isNullOrBlank()
            }
        }?.variable ?: ""
    }

    val paginaValida: Boolean get() {
        val p = pagina ?: return false
        val minObs = survey?.config?.min_caracteres_observacion ?: 10
        val obs = respuestas["OBS_${p.seccion_id}"] ?: ""

        val preguntasOk = p.preguntas.all { preg ->
            // Si la pregunta está oculta por show_if, no se valida
            if (!evaluarCondicion(preg.show_if, respuestas)) return@all true
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
                else   -> !respuestas[preg.variable].isNullOrBlank()
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
        if (this.muestraId == muestraId) return
        this.muestraId = muestraId

        val survey = Gson().fromJson(jsonString, Survey::class.java)
        this.surveyId = survey.survey_id

        _uiState.update { it.copy(survey = survey, isLoading = true) }

        viewModelScope.launch {
            dao.observarRespuestas(muestraId, surveyId).collect { lista ->
                val map = lista.associate { it.variable to it.valor }
                _uiState.update { it.copy(respuestas = map, isLoading = false) }
            }
        }
    }

    // ── Respuesta ─────────────────────────────────────────────────────────────

    fun onRespuesta(variable: String, valor: String) {
        val survey = _uiState.value.survey ?: return

        // 1. Actualizar el mapa con la nueva respuesta
        val nuevasRespuestas = _uiState.value.respuestas.toMutableMap()
        nuevasRespuestas[variable] = valor

        // 2. Limpiar respuestas huérfanas en la misma página
        //    Cualquier pregunta de esta página cuyo show_if ahora sea falso
        //    debe borrarse (ella y sus campos auxiliares _OTRO, _ESP, etc.)
        val paginaActual = _uiState.value.pagina
        if (paginaActual != null) {
            limpiarHuerfanasEnPagina(paginaActual, nuevasRespuestas)
        }

        // 3. Actualizar estado
        _uiState.update { it.copy(respuestas = nuevasRespuestas) }

        // 4. Persistir en Room
        if (survey.config.guardar_automatico) {
            viewModelScope.launch {
                dao.upsert(SurveyConglomeradoEntity(
                    muestra_id = muestraId,
                    survey_id  = surveyId,
                    variable   = variable,
                    valor      = valor,
                ))
            }
        }
    }

    /**
     * Limpia en [respuestas] todas las variables de preguntas de [pagina]
     * cuyo show_if haya pasado a ser false con el nuevo estado de respuestas.
     * También limpia los campos auxiliares derivados (_OTRO, _ESP, _FECHA, _HORA).
     */
    private fun limpiarHuerfanasEnPagina(
        pagina: Pagina,
        respuestas: MutableMap<String, String>,
    ) {
        pagina.preguntas.forEach { preg ->
            if (!evaluarCondicion(preg.show_if, respuestas)) {
                // Borrar variable principal
                respuestas.remove(preg.variable)
                // Borrar auxiliares conocidos
                listOf("_OTRO", "_ESP", "_FECHA", "_HORA").forEach { sufijo ->
                    respuestas.remove("${preg.variable}$sufijo")
                }
                // Si es matrix / matrix_scale, borrar cada fila
                preg.options?.forEach { opt ->
                    opt.variable?.let { v ->
                        respuestas.remove(v)
                        respuestas.remove("${v}_ESP")
                    }
                }
                // Borrar de Room en background
                viewModelScope.launch {
                    dao.borrarVariable(muestraId, surveyId, preg.variable)
                }
            }
        }
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    fun onSiguiente() {
        val state  = _uiState.value
        val survey = state.survey ?: return
        val pagina = state.pagina ?: return
        if (!state.paginaValida) return

        // Calcular destino (salto o siguiente secuencial)
        val jumpTarget = pagina.preguntas.firstNotNullOfOrNull { preg ->
            val resp = state.respuestas[preg.variable]
            preg.options?.find { it.value == resp }?.jump_to_page
                ?: if (preg.jump_to_page != null && resp != null) preg.jump_to_page else null
        }
        val destino = jumpTarget ?: (state.paginaActual + 1)

        if (destino > survey.paginas.lastIndex) {
            onFinalizar()
            return
        }

        // Calcular páginas que se SALTAN y limpiar sus respuestas
        val paginasQueSeSaltan = calcularPaginasSaltadas(
            desde     = state.paginaActual,
            hasta     = destino,
            visitadas = state.paginasVisitadas,
            survey    = survey,
        )
        if (paginasQueSeSaltan.isNotEmpty()) {
            limpiarRespuestasDePaginas(paginasQueSeSaltan, survey)
        }

        _uiState.update {
            it.copy(
                paginaActual      = destino,
                historial         = it.historial + it.paginaActual,
                paginasVisitadas  = it.paginasVisitadas + it.paginaActual,
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

    /**
     * Devuelve los índices de páginas entre [desde]+1 y [hasta]-1 que
     * NO estaban en [visitadas] (es decir, se están saltando por primera vez).
     * Las que ya se visitaron se limpian solo si el salto las excluye definitivamente.
     */
    private fun calcularPaginasSaltadas(
        desde: Int, hasta: Int,
        visitadas: Set<Int>,
        survey: Survey,
    ): List<Int> {
        if (hasta <= desde + 1) return emptyList()
        return (desde + 1 until hasta).filter { idx ->
            // Solo limpiar si esta página nunca fue visitada.
            // Si ya fue visitada, el usuario puso datos válidos conscientemente.
            idx !in visitadas && idx <= survey.paginas.lastIndex
        }
    }

    /**
     * Borra de Room y del uiState todas las respuestas de las páginas indicadas.
     */
    private fun limpiarRespuestasDePaginas(indices: List<Int>, survey: Survey) {
        val variablesABorrar = mutableSetOf<String>()

        indices.forEach { idx ->
            val pagina = survey.paginas.getOrNull(idx) ?: return@forEach
            pagina.preguntas.forEach { preg ->
                variablesABorrar.add(preg.variable)
                // Auxiliares
                listOf("_OTRO", "_ESP", "_FECHA", "_HORA").forEach { s ->
                    variablesABorrar.add("${preg.variable}$s")
                }
                // Filas de matrix
                preg.options?.forEach { opt ->
                    opt.variable?.let { v ->
                        variablesABorrar.add(v)
                        variablesABorrar.add("${v}_ESP")
                    }
                }
            }
            // Observación de la sección
            variablesABorrar.add("OBS_${pagina.seccion_id}")
        }

        // Actualizar uiState
        _uiState.update { state ->
            state.copy(
                respuestas = state.respuestas.filterKeys { it !in variablesABorrar }
            )
        }

        // Borrar de Room en background
        viewModelScope.launch {
            variablesABorrar.forEach { variable ->
                dao.borrarVariable(muestraId, surveyId, variable)
            }
        }
    }

    // ── Guardar / Finalizar ───────────────────────────────────────────────────

    fun onGuardar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val entidades = _uiState.value.respuestas.map { (variable, valor) ->
                SurveyConglomeradoEntity(muestraId, surveyId, variable, valor)
            }
            dao.upsertAll(entidades)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun onFinalizar() {
        _uiState.update { it.copy(paginasVisitadas = it.paginasVisitadas + it.paginaActual) }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            onGuardar()
            _uiState.update { it.copy(isSaving = false, isCompleted = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HELPER — Evalúa condición show_if
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