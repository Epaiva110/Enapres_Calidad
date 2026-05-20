package com.minedu.gob.pe.enaprescalidad.surveys.adapter


import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyOption
import com.google.gson.*
import java.lang.reflect.Type
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionNode
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionGroup
import com.minedu.gob.pe.enaprescalidad.surveys.models.GroupConditionRule
import com.minedu.gob.pe.enaprescalidad.surveys.models.ConditionRule
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pagina
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyManager
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyResponse

class ConditionNodeAdapter : JsonDeserializer<ConditionNode>, JsonSerializer<ConditionNode> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ConditionNode {
        val jsonObject = json.asJsonObject

        return when {
            // Si tiene "conditions", es un grupo lógico (AND / OR)
            jsonObject.has("conditions") -> {
                context.deserialize(jsonObject, ConditionGroup::class.java)
            }
            // Si tiene "variables" (en plural), es una regla de variables múltiples (Suma, promedio, etc)
            jsonObject.has("variables") -> {
                context.deserialize(jsonObject, GroupConditionRule::class.java)
            }
            // De lo contrario, es una regla simple sobre una sola variable
            else -> {
                context.deserialize(jsonObject, ConditionRule::class.java)
            }
        }
    }

    override fun serialize(src: ConditionNode, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return context.serialize(src)
    }
}

// Interfaz global de utilidad para instanciar tu Gson configurado
object SurveyGson {
    val instance: Gson = GsonBuilder()
        .registerTypeSerializerOrDeserializer(ConditionNode::class.java, ConditionNodeAdapter())
        .setPrettyPrinting()
        .create()
}

// Extensión para registrar tanto deserializador como serializador de forma segura
private fun GsonBuilder.registerTypeSerializerOrDeserializer(type: Type, adapter: Any): GsonBuilder {
    return this.registerTypeAdapter(type, adapter)
}



/**
 * Orquestador de renderizado dinámico en pantalla.
 */
class SurveyUiRenderer(
    private val context: Context,
    private val container: LinearLayout,
    private val manager: SurveyManager
) {

    /**
     * Dibuja o actualiza una página completa en pantalla
     */
    fun renderPage(pagina: Pagina, response: SurveyResponse) {
        container.removeAllViews()

        // Validar si la página debe mostrarse según el flujo dinámico de saltos
        val visiblePages = manager.getVisiblePageIds()
        if (!visiblePages.contains(pagina.id_pagina)) return

        // Añadir encabezados de sección
        val headerView = createHeaderLayout(pagina.titulo_seccion, pagina.titulo)
        container.addView(headerView)

        // Renderizar preguntas válidas
        for (pregunta in pagina.preguntas) {
            val isVisible = pregunta.show_if == null || manager.evaluator.evaluate(pregunta.show_if, response.answers)
            if (!isVisible) continue

            val questionView = createQuestionView(pregunta, response)
            container.addView(questionView)
        }
    }

    private fun createQuestionView(pregunta: Pregunta, response: SurveyResponse): View {
        val questionContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 1. Renderizar el componente base según su tipo estipulado
        val baseComponent = when (pregunta.type.lowercase()) {
            "single" -> renderSingleChoice(pregunta, response, questionContainer)
            "multiple" -> renderMultipleChoice(pregunta, response, questionContainer)
            "matrix", "matrix_scale", "matrix_detail" -> renderMatrixStructure(pregunta, response, questionContainer)
            "text", "number", "decimal" -> renderInputFields(pregunta, response)
            "likert" -> renderLikertComponent(pregunta, response)
            "photo" -> renderPhotoUploader(pregunta, response)
            else -> renderUnsupportedTypePlaceholder(pregunta)
        }

        questionContainer.addView(baseComponent, 0)
        return questionContainer
    }

    private fun renderSingleChoice(pregunta: Pregunta, response: SurveyResponse, parent: LinearLayout): View {
        // Lógica de renderizado de RadioButtons nativos o composables...
        // Cada vez que el usuario seleccione una opción:
        // -> manager.updateAnswer(pregunta.variable, opcionSeleccionada.value)
        // -> si opcionSeleccionada.detail_questions != null, gatillar renderSubQuestions(parent, opcionSeleccionada)
        return View(context) // Placeholder estructural
    }

    private fun renderMatrixStructure(pregunta: Pregunta, response: SurveyResponse, parent: LinearLayout): View {
        // Lógica para grillas complejas, escalas o matrices con detalle modal/bottomsheet
        return View(context)
    }

    private fun renderInputFields(pregunta: Pregunta, response: SurveyResponse): View {
        // Captura de texto/números respetando min_length, max_length, min_value y max_value
        return View(context)
    }

    private fun renderLikertComponent(pregunta: Pregunta, response: SurveyResponse): View {
        // Estrellas, emojis o números según el likert_type asignado
        return View(context)
    }

    private fun renderPhotoUploader(pregunta: Pregunta, response: SurveyResponse): View {
        // Control de cámara respetando max_photos y allow_gallery
        return View(context)
    }

    private fun renderSubQuestions(parent: LinearLayout, option: SurveyOption, response: SurveyResponse) {
        option.detail_questions?.forEach { subPregunta ->
            val isSubVisible = subPregunta.show_if == null || manager.evaluator.evaluate(subPregunta.show_if, response.answers)
            if (isSubVisible) {
                val subView = createQuestionView(subPregunta, response)
                // Estilización con sangría/indentación para denotar jerarquía (Especifique)
                subView.setPadding(40, 10, 10, 10)
                parent.addView(subView)
            }
        }
    }

    private fun createHeaderLayout(seccion: String, titulo: String): View = View(context)
    private fun renderMultipleChoice(p: Pregunta, r: SurveyResponse, l: LinearLayout): View = View(context)
    private fun renderUnsupportedTypePlaceholder(p: Pregunta): View = View(context)
}