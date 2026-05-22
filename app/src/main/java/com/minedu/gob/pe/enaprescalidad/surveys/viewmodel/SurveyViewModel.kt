package com.minedu.gob.pe.enaprescalidad.surveys.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.models.Survey
import com.minedu.gob.pe.enaprescalidad.data.local.dao.surveys.SurveyConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.surveys.SurveyConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.surveys.SurveyCompletion
import com.minedu.gob.pe.enaprescalidad.surveys.adapter.SurveyGson
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionEvaluator
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
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

    // clave = variable
    // valor = Any?
    val respuestas: Map<String, Any?> = emptyMap(),

    // índice actual
    val paginaActual: Int = 0,

    val erroresValidacion: Map<String, String> = emptyMap(),

    // historial navegación
    val historial: List<Int> = emptyList(),

    // loading states
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,

    // errores
    val error: String? = null,

    // observaciones
    val showObsDialog: Boolean = false,

    // variables que fallaron validación (resaltar en rojo)
    val variablesConError: Set<String> = emptySet(),

    ) {
//    val progreso: Float get() = if (totalPaginas == 0) 0f else (paginaActual + 1f) / totalPaginas
    // ─────────────────────────────────────────────────────────────────────────
    // PAGINA ACTUAL
    // ─────────────────────────────────────────────────────────────────────────

    val pagina: Pagina?
        get() = survey
            ?.paginas
            ?.getOrNull(paginaActual)

    // ─────────────────────────────────────────────────────────────────────────
    // ULTIMA PAGINA
    // ─────────────────────────────────────────────────────────────────────────

    val isLastPage: Boolean
        get() =
            survey != null &&
                    paginaActual == survey.paginas.lastIndex

    // ─────────────────────────────────────────────────────────────────────────
    // TOTAL PAGINAS
    // ─────────────────────────────────────────────────────────────────────────

    val totalPaginas: Int
        get() =
            survey?.paginas?.size ?: 0

    // ─────────────────────────────────────────────────────────────────────────
    // PROGRESO REAL
    // ─────────────────────────────────────────────────────────────────────────

    fun progreso(
        paginasVisibles: List<Int>
    ): Float {

        val paginaId =
            pagina?.id_pagina ?: return 0f

        val idx =
            paginasVisibles.indexOf(paginaId)

        if (
            idx == -1 ||
            paginasVisibles.isEmpty()
        ) {
            return 0f
        }

        return (idx + 1f) / paginasVisibles.size
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OBSERVACION VALIDA
    // ─────────────────────────────────────────────────────────────────────────

    val obsValida: Boolean
        get() {

            val p =
                pagina ?: return false

            val min =
                survey?.config?.min_caracteres_observacion
                    ?: 10

            val texto =
                respuestas["OBS_${p.seccion_id}"]
                    ?.toString()
                    ?.trim()
                    ?: ""

            return texto.length >= min
        }

    // ─────────────────────────────────────────────────────────────────────────
    // VARIABLE EN FOCO
    // ─────────────────────────────────────────────────────────────────────────

    fun obtenerVariableEnFoco(
        evaluator: ConditionEvaluator
    ): String {

        val p =
            pagina ?: return ""

        fun estaRespondida(
            valor: Any?
        ): Boolean {

            return when (valor) {

                null -> false

                is String ->
                    valor.isNotBlank()

                is List<*> ->
                    valor.isNotEmpty()

                else -> true
            }
        }

        for (preg in p.preguntas) {

            // solo requeridas
            if (!preg.required) continue

            // show_if
            if (
                preg.show_if != null &&
                !evaluator.evaluate(
                    preg.show_if,
                    respuestas
                )
            ) continue

            when (preg.type.lowercase()) {

                // ─────────────────────────────────────────────────────────────
                // MATRIX
                // ─────────────────────────────────────────────────────────────

                "matrix",
                "matrix_scale",
                "matrix_detail" -> {

                    preg.options?.forEach { fila ->

                        val subVar =
                            "${preg.variable}_${fila.variable ?: fila.value ?: ""}"

                        if (
                            !estaRespondida(
                                respuestas[subVar]
                            )
                        ) {
                            return subVar
                        }

                        // detail questions internas
                        fila.detail_questions?.forEach { subPreg ->

                            val visible =
                                subPreg.show_if == null ||
                                        evaluator.evaluate(
                                            subPreg.show_if,
                                            respuestas
                                        )

                            if (
                                subPreg.required &&
                                visible &&
                                !estaRespondida(
                                    respuestas[subPreg.variable]
                                )
                            ) {
                                return subPreg.variable
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // PREGUNTAS NORMALES
                // ─────────────────────────────────────────────────────────────

                else -> {

                    if (
                        !estaRespondida(
                            respuestas[preg.variable]
                        )
                    ) {
                        return preg.variable
                    }

                    preg.options?.forEach { opcion ->

                        val valorPadre =
                            respuestas[preg.variable]

                        val seleccionada =
                            when (valorPadre) {

                                is List<*> ->
                                    valorPadre.map {
                                        it.toString()
                                    }.contains(
                                        opcion.value?.toString()
                                    )

                                else ->
                                    valorPadre?.toString() ==
                                            opcion.value?.toString()
                            }

                        if (seleccionada) {

                            opcion.detail_questions?.forEach { subPreg ->

                                val visible =
                                    subPreg.show_if == null ||
                                            evaluator.evaluate(
                                                subPreg.show_if,
                                                respuestas
                                            )

                                if (
                                    subPreg.required &&
                                    visible &&
                                    !estaRespondida(
                                        respuestas[subPreg.variable]
                                    )
                                ) {
                                    return subPreg.variable
                                }
                            }
                        }
                    }
                }
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
    private val dao: SurveyConglomeradoDao,
) : ViewModel() {

    // ─────────────────────────────────────────────────────────────────────────
    // UI STATE
    // ─────────────────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(SurveyUiState())
    val uiState: StateFlow<SurveyUiState> =
        _uiState.asStateFlow()

    val evaluator = ConditionEvaluator()

    private var muestraId: Int = -1
    private var surveyId: String = ""

    private lateinit var survey: Survey

    // ─────────────────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────────────────

    fun init(
        muestraId: Int,
        jsonString: String
    ) {
        if (this.muestraId == muestraId && _uiState.value.survey != null) {
            return
        }

        this.muestraId = muestraId

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            try {
                survey = SurveyGson.instance.fromJson(jsonString, Survey::class.java)
                surveyId = survey.survey_id

                val entidades = dao.obtenerRespuestasSincronas(muestraId, surveyId)
                val mapa = entidades.associate { it.variable to deserializarValor(it.valor) }

                _uiState.update {
                    it.copy(
                        survey = survey,
                        respuestas = mapa,
                        paginaActual = 0,
                        historial = emptyList(),
                        isCompleted = false,
                        variablesConError = emptySet(),
                        showObsDialog = false,
                        error = null,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al parsear JSON: ${e.message}"
                    )
                }
            }
        }
    }

    fun consumeCompleted() {
        _uiState.update { it.copy(isCompleted = false) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private fun estaRespondida(
        valor: Any?
    ): Boolean {

        return when (valor) {

            null -> false

            is String ->
                valor.isNotBlank()

            is List<*> ->
                valor.isNotEmpty()

            else -> true
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UPDATE ANSWER
    // ─────────────────────────────────────────────────────────────────────────

    fun onUpdateAnswer(
        variable: String,
        valor: Any?,
    ) {
        if (variable == SurveyCompletion.COMPLETED_VARIABLE) return

        val nuevas =
            _uiState.value.respuestas.toMutableMap()

        if (valor == null) {
            nuevas.remove(variable)
        } else {
            nuevas[variable] = valor
        }

        purgarRespuestasFantasmas(nuevas)

        _uiState.update {
            // Quitar la variable del set de errores si ya fue respondida
            val errorActualizado = if (variable in it.variablesConError && estaRespondida(nuevas[variable])) {
                it.variablesConError - variable
            } else {
                it.variablesConError
            }
            it.copy(respuestas = nuevas, variablesConError = errorActualizado)
        }

        if (
            _uiState.value.survey?.config?.guardar_automatico == true
        ) {
            sincronizarRoom(nuevas)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OBTENER VARIABLE EN FOCO
    // ─────────────────────────────────────────────────────────────────────────

    fun obtenerVariableEnFoco(
        evaluator: ConditionEvaluator
    ): String {

        val p = _uiState.value.pagina ?: return ""

        val respuestas =
            _uiState.value.respuestas

        for (preg in p.preguntas) {

            if (!preg.required) continue

            if (
                preg.show_if != null &&
                !evaluator.evaluate(
                    preg.show_if,
                    respuestas
                )
            ) continue

            when (preg.type.lowercase()) {

                "matrix",
                "matrix_scale",
                "matrix_detail" -> {

                    preg.options?.forEach { fila ->

                        val subVar =
                            "${preg.variable}_${fila.variable ?: fila.value ?: ""}"

                        if (
                            !estaRespondida(
                                respuestas[subVar]
                            )
                        ) {
                            return subVar
                        }

                        fila.detail_questions?.forEach { subPreg ->

                            if (
                                subPreg.required &&
                                (
                                        subPreg.show_if == null ||
                                                evaluator.evaluate(
                                                    subPreg.show_if,
                                                    respuestas
                                                )
                                        )
                            ) {

                                if (
                                    !estaRespondida(
                                        respuestas[subPreg.variable]
                                    )
                                ) {
                                    return subPreg.variable
                                }
                            }
                        }
                    }
                }

                else -> {

                    if (
                        !estaRespondida(
                            respuestas[preg.variable]
                        )
                    ) {
                        return preg.variable
                    }

                    preg.options?.forEach { opcion ->

                        val valorPadre =
                            respuestas[preg.variable]

                        val seleccionada =
                            when (valorPadre) {

                                is List<*> ->
                                    valorPadre.map {
                                        it.toString()
                                    }.contains(
                                        opcion.value?.toString()
                                    )

                                else ->
                                    valorPadre?.toString() ==
                                            opcion.value?.toString()
                            }

                        if (seleccionada) {

                            opcion.detail_questions?.forEach { subPreg ->

                                if (
                                    subPreg.required &&
                                    (
                                            subPreg.show_if == null ||
                                                    evaluator.evaluate(
                                                        subPreg.show_if,
                                                        respuestas
                                                    )
                                            )
                                ) {

                                    if (
                                        !estaRespondida(
                                            respuestas[subPreg.variable]
                                        )
                                    ) {
                                        return subPreg.variable
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return ""
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PURGA
    // ─────────────────────────────────────────────────────────────────────────

    private fun purgarRespuestasFantasmas(
        respuestas: MutableMap<String, Any?>
    ) {

        val paginasVisibles =
            calcularPaginasVisibles(respuestas)

        for (pagina in survey.paginas) {

            val paginaVisible =
                paginasVisibles.contains(
                    pagina.id_pagina
                )

            for (preg in pagina.preguntas) {

                val pregVisible =
                    paginaVisible &&
                            (
                                    preg.show_if == null ||
                                            evaluator.evaluate(
                                                preg.show_if,
                                                respuestas
                                            )
                                    )

                if (!pregVisible) {

                    eliminarArbol(
                        preg,
                        respuestas
                    )

                } else {

                    limpiarDetallesInternos(
                        preg,
                        respuestas
                    )
                }
            }
        }
    }

    private fun eliminarArbol(
        preg: Pregunta,
        respuestas: MutableMap<String, Any?>
    ) {

        respuestas.remove(preg.variable)

        preg.options?.forEach { opcion ->

            val claveFila =
                opcion.variable ?: opcion.value

            if (!claveFila.isNullOrEmpty()) {

                respuestas.remove(
                    "${preg.variable}_$claveFila"
                )
            }

            opcion.detail_questions?.forEach { sub ->

                eliminarArbol(
                    sub,
                    respuestas
                )
            }
        }
    }

    private fun limpiarDetallesInternos(
        preg: Pregunta,
        respuestas: MutableMap<String, Any?>
    ) {

        val valorPadre =
            respuestas[preg.variable]

        when {

            preg.type.lowercase().contains("matrix") -> {

                preg.options?.forEach { fila ->

                    fila.detail_questions?.forEach { sub ->

                        val subVisible =
                            sub.show_if == null ||
                                    evaluator.evaluate(
                                        sub.show_if,
                                        respuestas
                                    )

                        if (!subVisible) {

                            eliminarArbol(
                                sub,
                                respuestas
                            )

                        } else {

                            limpiarDetallesInternos(
                                sub,
                                respuestas
                            )
                        }
                    }
                }
            }

            else -> {

                preg.options?.forEach { opcion ->

                    val seleccionada =
                        when (valorPadre) {

                            is List<*> ->
                                valorPadre.map {
                                    it.toString()
                                }.contains(
                                    opcion.value?.toString()
                                )

                            else ->
                                valorPadre?.toString() ==
                                        opcion.value?.toString()
                        }

                    opcion.detail_questions?.forEach { sub ->

                        val subVisible =
                            seleccionada &&
                                    (
                                            sub.show_if == null ||
                                                    evaluator.evaluate(
                                                        sub.show_if,
                                                        respuestas
                                                    )
                                            )

                        if (!subVisible) {

                            eliminarArbol(
                                sub,
                                respuestas
                            )

                        } else {

                            limpiarDetallesInternos(
                                sub,
                                respuestas
                            )
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PAGINAS VISIBLES
    // ─────────────────────────────────────────────────────────────────────────

    fun calcularPaginasVisibles(
        respuestas: Map<String, Any?> =
            _uiState.value.respuestas
    ): Set<Int> {

        val visibles =
            mutableSetOf<Int>()

        val mapa =
            survey.paginas.associateBy {
                it.id_pagina
            }

        var idActual =
            survey.paginas.firstOrNull()?.id_pagina
                ?: return visibles

        while (mapa.containsKey(idActual)) {

            if (visibles.contains(idActual)) break

            visibles.add(idActual)

            val pag =
                mapa[idActual]!!

            var salto: Int? = null

            outer@ for (preg in pag.preguntas) {

                val ans =
                    respuestas[preg.variable]

                for (opt in preg.options ?: emptyList()) {

                    val sel =
                        when (ans) {

                            is List<*> ->
                                ans.map {
                                    it.toString()
                                }.contains(
                                    opt.value?.toString()
                                )

                            else ->
                                ans?.toString() ==
                                        opt.value?.toString()
                        }

                    if (
                        sel &&
                        opt.jump_to_page != null
                    ) {

                        salto = opt.jump_to_page
                        break@outer
                    }
                }
            }

            if (salto == null) {

                for (preg in pag.preguntas) {

                    if (
                        preg.jump_to_page != null &&
                        respuestas.containsKey(
                            preg.variable
                        )
                    ) {

                        val vis =
                            preg.show_if == null ||
                                    evaluator.evaluate(
                                        preg.show_if,
                                        respuestas
                                    )

                        if (vis) {

                            salto =
                                preg.jump_to_page

                            break
                        }
                    }
                }
            }

            idActual =
                salto ?: run {

                    val idx =
                        survey.paginas.indexOf(pag)

                    if (
                        idx < survey.paginas.lastIndex
                    ) {
                        survey.paginas[idx + 1].id_pagina
                    } else {
                        break
                    }
                }
        }

        return visibles
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALIDACION
    // ─────────────────────────────────────────────────────────────────────────

    private fun validarDetalles(
        preg: Pregunta,
        resp: Map<String, Any?>,
        recurse: (Pregunta) -> Boolean
    ): Boolean {

        val valorPadre =
            resp[preg.variable]

        preg.options?.forEach { opcion ->

            val sel =
                when (valorPadre) {

                    is List<*> ->
                        valorPadre.map {
                            it.toString()
                        }.contains(
                            opcion.value?.toString()
                        )

                    else ->
                        valorPadre?.toString() ==
                                opcion.value?.toString()
                }

            opcion.detail_questions?.forEach { sub ->

                if (
                    sel &&
                    (
                            sub.show_if == null ||
                                    evaluator.evaluate(
                                        sub.show_if,
                                        resp
                                    )
                            )
                ) {

                    if (!recurse(sub)) {
                        return false
                    }
                }
            }
        }

        return true
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CALCULAR VARIABLES CON ERROR (para resaltar en rojo)
    // ─────────────────────────────────────────────────────────────────────────

    fun calcularVariablesConError(): Set<String> {

        val p = _uiState.value.pagina ?: return emptySet()
        val resp = _uiState.value.respuestas
        val errores = mutableSetOf<String>()

        fun recolectarErrores(preg: Pregunta) {

            if (preg.show_if != null && !evaluator.evaluate(preg.show_if, resp)) return

            when (preg.type.lowercase()) {

                "info" -> return

                "matrix", "matrix_scale", "matrix_detail" -> {
                    if (!preg.required) return
                    preg.options?.forEach { fila ->
                        val subVar = "${preg.variable}_${fila.variable ?: fila.value ?: ""}"
                        if (!estaRespondida(resp[subVar])) errores.add(subVar)
                        fila.detail_questions?.forEach { sub ->
                            if (sub.required) recolectarErrores(sub)
                        }
                    }
                }

                "multiple", "multiple_binary" -> {
                    if (!preg.required) return
                    val lista = resp[preg.variable] as? List<*>
                    if (lista == null || lista.isEmpty()) errores.add(preg.variable)
                }

                "gps" -> {
                    if (!preg.required) return
                    val gps = resp[preg.variable]?.toString() ?: ""
                    if (!gps.contains("OMITIDO") && gps.isBlank()) errores.add(preg.variable)
                }

                else -> {
                    if (preg.required && !estaRespondida(resp[preg.variable])) {
                        errores.add(preg.variable)
                    }
                    // check detail_questions of selected options
                    val valorPadre = resp[preg.variable]
                    preg.options?.forEach { opcion ->
                        val sel = when (valorPadre) {
                            is List<*> -> valorPadre.map { it.toString() }.contains(opcion.value?.toString())
                            else -> valorPadre?.toString() == opcion.value?.toString()
                        }
                        if (sel) {
                            opcion.detail_questions?.forEach { sub ->
                                if (sub.required) recolectarErrores(sub)
                            }
                        }
                    }
                }
            }
        }

        p.preguntas.forEach { preg -> recolectarErrores(preg) }

        val minObs = survey.config.min_caracteres_observacion
        val obs = resp["OBS_${p.seccion_id}"]?.toString()?.trim() ?: ""
        if (obs.length < minObs) errores.add("OBS_${p.seccion_id}")

        return errores
    }

    private fun buildValidationErrorMessage(errores: Set<String>): String {
        val p = _uiState.value.pagina ?: return "Completa todas las preguntas requeridas."
        val minObs = survey.config.min_caracteres_observacion
        val obsKey = "OBS_${p.seccion_id}"

        val partes = mutableListOf<String>()
        val preguntas = errores.filter { it != obsKey && !it.startsWith("OBS_") }
        if (preguntas.isNotEmpty()) {
            partes.add("preguntas requeridas pendientes")
        }
        if (obsKey in errores && minObs > 0) {
            partes.add("observación de sección (mín. $minObs caracteres, ícono de notas o campo al final de la página)")
        }
        return if (partes.isEmpty()) {
            "Completa todas las preguntas requeridas."
        } else {
            "Falta: ${partes.joinToString(" y ")}."
        }
    }

    private fun paginaEsValida(): Boolean {

        val p =
            _uiState.value.pagina ?: return false

        val resp =
            _uiState.value.respuestas

        val minObs =
            survey.config.min_caracteres_observacion

        val obs =
            resp["OBS_${p.seccion_id}"]
                ?.toString()
                ?: ""

        fun preguntaValida(
            preg: Pregunta
        ): Boolean {

            if (
                preg.show_if != null &&
                !evaluator.evaluate(
                    preg.show_if,
                    resp
                )
            ) {
                return true
            }

            val v =
                resp[preg.variable]

            val base =
                when (preg.type.lowercase()) {

                    "info" -> true

                    "matrix",
                    "matrix_scale",
                    "matrix_detail" -> {

                        preg.options?.all { fila ->

                            val clave =
                                fila.variable ?: fila.value

                            !clave.isNullOrEmpty() &&
                                    estaRespondida(
                                        resp["${preg.variable}_$clave"]
                                    )

                        } ?: true
                    }

                    "multiple",
                    "multiple_binary" -> {

                        val lista =
                            v as? List<*>

                        lista != null &&
                                lista.isNotEmpty()
                    }

                    "gps" -> {

                        val gps =
                            v?.toString() ?: ""

                        gps.contains("OMITIDO") ||
                                gps.isNotBlank()
                    }

                    else ->
                        estaRespondida(v)
                }

            return base &&
                    validarDetalles(
                        preg,
                        resp,
                        ::preguntaValida
                    )
        }

        val preguntasOk =
            p.preguntas.all(::preguntaValida)

        return preguntasOk &&
                obs.trim().length >= minObs
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVEGACION
    // ─────────────────────────────────────────────────────────────────────────

    fun onNextPage() {

        val errores = calcularVariablesConError()
        if (errores.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    error = buildValidationErrorMessage(errores),
                    variablesConError = errores
                )
            }
            return
        }

        // Limpiar errores al avanzar exitosamente
        _uiState.update { it.copy(variablesConError = emptySet()) }

        val state =
            _uiState.value

        val pag =
            state.pagina ?: return

        val paginasVisibles =
            calcularPaginasVisibles()
                .toList()
                .sorted()

        val idxActual =
            survey.paginas.indexOfFirst {
                it.id_pagina == pag.id_pagina
            }

        val siguienteId =
            paginasVisibles.firstOrNull { id ->

                val idx =
                    survey.paginas.indexOfFirst {
                        it.id_pagina == id
                    }

                idx > idxActual
            }

        if (siguienteId == null) {

            _uiState.update {
                it.copy(isSaving = true)
            }

            viewModelScope.launch {
                val respuestasFinal = _uiState.value.respuestas.toMutableMap().apply {
                    put(SurveyCompletion.COMPLETED_VARIABLE, true)
                }
                guardarTodoEnRoom(respuestasFinal)

                _uiState.update {
                    it.copy(
                        respuestas = respuestasFinal,
                        isSaving = false,
                        isCompleted = true
                    )
                }
            }

        } else {

            val nuevaPosicion =
                survey.paginas.indexOfFirst {
                    it.id_pagina == siguienteId
                }

            _uiState.update {

                it.copy(
                    paginaActual = nuevaPosicion,
                    historial = it.historial + it.paginaActual,
                )
            }
        }
    }

    // Navegar sin validación (modo solo lectura)
    fun onNextPageReadOnly() {

        val state = _uiState.value
        val pag   = state.pagina ?: return

        val paginasVisibles = calcularPaginasVisibles().toList().sorted()

        val idxActual = survey.paginas.indexOfFirst { it.id_pagina == pag.id_pagina }

        val siguienteId = paginasVisibles.firstOrNull { id ->
            val idx = survey.paginas.indexOfFirst { it.id_pagina == id }
            idx > idxActual
        }

        if (siguienteId == null) {
            // Última página en modo lectura → simplemente completar para salir
            _uiState.update { it.copy(isCompleted = true) }
        } else {
            val nuevaPosicion = survey.paginas.indexOfFirst { it.id_pagina == siguienteId }
            _uiState.update {
                it.copy(
                    paginaActual = nuevaPosicion,
                    historial    = it.historial + it.paginaActual,
                )
            }
        }
    }

    fun onBackPage() {

        val hist =
            _uiState.value.historial

        if (hist.isEmpty()) return

        _uiState.update {

            it.copy(
                paginaActual = hist.last(),
                historial = hist.dropLast(1)
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OBSERVACION
    // ─────────────────────────────────────────────────────────────────────────

    fun openObsDialog() =
        _uiState.update {
            it.copy(showObsDialog = true)
        }

    fun closeObsDialog() =
        _uiState.update {
            it.copy(showObsDialog = false)
        }

    fun onGuardarObservacion(
        texto: String
    ) {

        val p =
            _uiState.value.pagina ?: return

        onUpdateAnswer(
            "OBS_${p.seccion_id}",
            texto
        )

        closeObsDialog()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GUARDAR
    // ─────────────────────────────────────────────────────────────────────────

    fun onGuardar() {

        viewModelScope.launch {

            _uiState.update {
                it.copy(isSaving = true)
            }

            guardarTodoEnRoom(
                _uiState.value.respuestas
            )

            _uiState.update {
                it.copy(isSaving = false)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ROOM
    // ─────────────────────────────────────────────────────────────────────────

    private fun sincronizarRoom(
        mapaMemoria: Map<String, Any?>
    ) {

        viewModelScope.launch {

            val enBd =
                dao.obtenerRespuestasSincronas(
                    muestraId,
                    surveyId
                )

            val keysMemoria =
                mapaMemoria.keys

            enBd.forEach { entidad ->

                if (
                    entidad.variable !in keysMemoria
                ) {

                    dao.borrarVariable(
                        muestraId,
                        surveyId,
                        entidad.variable
                    )
                }
            }

            dao.upsertAll(

                mapaMemoria.map { (k, v) ->

                    SurveyConglomeradoEntity(
                        muestraId,
                        surveyId,
                        k,
                        serializarValor(v)
                    )
                }
            )
        }
    }

    private suspend fun guardarTodoEnRoom(
        mapa: Map<String, Any?>
    ) {
        dao.upsertAll(
            mapa.map { (k, v) ->
                SurveyConglomeradoEntity(
                    muestraId,
                    surveyId,
                    k,
                    serializarValor(v)
                )
            }
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SERIALIZACION
    // ─────────────────────────────────────────────────────────────────────────

    private fun serializarValor(
        value: Any?
    ): String {

        return SurveyGson.instance.toJson(value)
    }

    private fun deserializarValor(
        raw: String
    ): Any? {

        if (raw.isBlank()) {
            return null
        }

        return try {

            SurveyGson.instance.fromJson(
                raw,
                Any::class.java
            )

        } catch (_: Exception) {

            raw
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ERROR
    // ─────────────────────────────────────────────────────────────────────────

    fun clearError() {

        _uiState.update {
            it.copy(error = null)
        }
    }
}