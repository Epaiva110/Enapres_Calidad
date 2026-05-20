package com.minedu.gob.pe.enaprescalidad.surveys.models

class SurveyManager(
    private val survey: Survey,
    private val response: SurveyResponse
) {
    val evaluator = ConditionEvaluator()

    /**
     * Registra una respuesta y automáticamente ejecuta la poda/limpieza
     * de flujos alterados y condicionales ocultos.
     */
    fun updateAnswer(variable: String, value: Any?) {
        if (value == null) {
            response.answers.remove(variable)
        } else {
            response.answers[variable] = value
        }
        response.updated_at = System.currentTimeMillis()

        // GURÚ DE LIMPIEZA: Sincroniza el estado de las variables válidas
        cleanOrphanAnswers()
    }

    /**
     * Inspecciona la totalidad del árbol de la encuesta y elimina
     * las respuestas de los flujos que ya no son visibles.
     */
    private fun cleanOrphanAnswers() {
        val activePages = getVisiblePageIds()

        for (pagina in survey.paginas) {
            val isPageVisible = activePages.contains(pagina.id_pagina)

            for (pregunta in pagina.preguntas) {
                // Caso 1: La página completa fue saltada u oculta, o la pregunta tiene un show_if falso
                val isPreguntaVisible = isPageVisible &&
                        (pregunta.show_if == null || evaluator.evaluate(pregunta.show_if, response.answers))

                if (!isPreguntaVisible) {
                    removeQuestionAnswers(pregunta)
                } else {
                    // Caso 2: Evaluar si tiene sub-preguntas (Detail/Especifique de matrices u opciones)
                    evaluateAndCleanDetailQuestions(pregunta)
                }
            }
        }
    }

    /**
     * Limpia recursivamente sub-preguntas / sub-matrices si la opción padre no está seleccionada.
     */
    private fun evaluateAndCleanDetailQuestions(pregunta: Pregunta) {
        val parentValue = response.answers[pregunta.variable]

        pregunta.options?.forEach { option ->
            // Verificar si esta opción específica es la que está seleccionada actualmente
            val isOptionSelected = when (parentValue) {
                is List<*> -> parentValue.map { it.toString() }.contains(option.value?.toString())
                else -> parentValue?.toString() == option.value?.toString()
            }

            // Si la opción tiene sub-preguntas (ej. un especifique de una opción o celda matrix)
            option.detail_questions?.forEach { subPregunta ->
                val isSubVisible = isOptionSelected &&
                        (subPregunta.show_if == null || evaluator.evaluate(subPregunta.show_if, response.answers))

                if (!isSubVisible) {
                    removeQuestionAnswers(subPregunta)
                } else {
                    // Recursión por si hay sub-sub preguntas anidadas
                    evaluateAndCleanDetailQuestions(subPregunta)
                }
            }
        }
    }

    /**
     * Remueve físicamente del mapa de respuestas la variable y sus hijos
     */
    private fun removeQuestionAnswers(pregunta: Pregunta) {
        response.answers.remove(pregunta.variable)

        // Limpieza en cascada si tenía opciones con más preguntas hijas internas
        pregunta.options?.forEach { option ->
            option.detail_questions?.forEach { subPregunta ->
                removeQuestionAnswers(subPregunta)
            }
        }
    }

    /**
     * Calcula dinámicamente qué páginas son visibles respetando los saltos de página (jump_to_page) //[cite: 21]
     */
    fun getVisiblePageIds(): Set<Int> {
        val visiblePages = mutableSetOf<Int>()
        val paginasMap = survey.paginas.associateBy { it.id_pagina }

        if (survey.paginas.isEmpty()) return visiblePages

        var currentPageId = survey.paginas.first().id_pagina
        val totalPaginas = survey.paginas.size

        while (paginasMap.containsKey(currentPageId)) {
            visiblePages.add(currentPageId)
            val paginaActual = paginasMap[currentPageId]!!

            var jumpTarget: Int? = null

            // 1. Evaluar si hay un salto condicional por respuesta de opción en la página
            for (pregunta in paginaActual.preguntas) {
                val answer = response.answers[pregunta.variable]
                pregunta.options?.forEach { option ->
                    val isSelected = when (answer) {
                        is List<*> -> answer.map { it.toString() }.contains(option.value?.toString())
                        else -> answer?.toString() == option.value?.toString()
                    }
                    if (isSelected && option.jump_to_page != null) {
                        jumpTarget = option.jump_to_page
                    }
                }
            }

            // 2. Si no hubo salto por opción, evaluar si la última pregunta de la página tiene salto directo //[cite: 21]
            if (jumpTarget == null) {
                for (pregunta in paginaActual.preguntas) {
                    if (pregunta.jump_to_page != null) {
                        // El salto directo solo se activa si la pregunta está visible y respondida
                        val isPreguntaVisible = pregunta.show_if == null || evaluator.evaluate(pregunta.show_if, response.answers)
                        if (isPreguntaVisible && response.answers.containsKey(pregunta.variable)) {
                            jumpTarget = pregunta.jump_to_page
                        }
                    }
                }
            }

            // Determinar cuál es la siguiente página a procesar
            if (jumpTarget != null) {
                currentPageId = jumpTarget!!
            } else {
                // Flujo secuencial natural
                val currentIndex = survey.paginas.indexOf(paginaActual)
                if (currentIndex < totalPaginas - 1) {
                    currentPageId = survey.paginas[currentIndex + 1].id_pagina
                } else {
                    break // Llegamos al final de la encuesta
                }
            }

            // Evitar bucles infinitos si un JSON mal configurado apunta a sí mismo
            if (visiblePages.contains(currentPageId)) {
                break
            }
        }

        return visiblePages
    }
}