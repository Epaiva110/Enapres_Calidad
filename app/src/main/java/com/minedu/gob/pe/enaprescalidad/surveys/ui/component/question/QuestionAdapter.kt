package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.surveys.models.TipoPregunta
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity.EntityModuleCallbacks
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity.HogaresModule
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity.HogarRowUi
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity.PersonaRowUi
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity.PersonasModule
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity.VisitaRowUi
import com.minedu.gob.pe.enaprescalidad.surveys.ui.component.entity.VisitasModule

// ─────────────────────────────────────────────────────────────────────────────
//  CALLBACKS DE MÓDULOS DE ENTIDAD
//
//  El SurveyPage los recibe desde el ViewModel/Screen y los pasa hasta aquí.
//  Cada módulo llama al callback correspondiente cuando el usuario presiona
//  "Agregar" o "Editar", y el Screen muestra el BottomSheet adecuado.
// ─────────────────────────────────────────────────────────────────────────────

data class EntityModuleCallbacks(
    // Hogares
    val hogaresProvider: ((String) -> List<HogarRowUi>)? = null,
    val onAgregarHogar: ((variable: String) -> Unit)? = null,
    val onEditarHogar: ((variable: String, id: Int) -> Unit)? = null,

    // Personas
    val personasProvider: ((String) -> List<PersonaRowUi>)? = null,
    val onAgregarPersona: ((variable: String) -> Unit)? = null,
    val onEditarPersona: ((variable: String, id: Int) -> Unit)? = null,

    // Visitas
    val visitasProvider: ((String) -> List<VisitaRowUi>)? = null,
    val onRegistrarVisita: ((variable: String) -> Unit)? = null,
    val onEditarVisita: ((variable: String, id: Int) -> Unit)? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
//  DYNAMIC QUESTION ADAPTER
//
//  Dispatcher principal. Detecta si la pregunta es un módulo de entidad
//  (type == "entity_hogar" | "entity_persona" | "entity_visita") y
//  renderiza la pantalla especial en lugar de un input normal.
//
//  Para tipos estándar delega en el QuestionCard + componente específico,
//  igual que antes.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DynamicQuestionAdapter(
    pregunta: Pregunta,
    respuestas: Map<String, Any?>,
    variableEnFoco: String,
    variablesConError: Set<String> = emptySet(),
    editable: Boolean = true,
    onValueChange: (String, Any?) -> Unit,
    modifier: Modifier = Modifier,
    // Callbacks de módulos de entidad (null en encuestas sin entity_*)
    entityCallbacks: EntityModuleCallbacks = EntityModuleCallbacks(),
) {
    // ── Módulos de entidad embebidos ─────────────────────────────────────────
    if (pregunta.esModuloEntidad) {
        EntityModuleDispatcher(
            pregunta        = pregunta,
            variablesConError = variablesConError,
            editable        = editable,
            entityCallbacks = entityCallbacks,
            modifier        = modifier,
        )
        return
    }

    // ── Tipos estándar de input ───────────────────────────────────────────────
    val onChange: (String, Any?) -> Unit = if (editable) onValueChange else { _, _ -> }
    val valorActual = respuestas[pregunta.variable]
    val estaEnFoco  = pregunta.variable == variableEnFoco
    val tieneError  = pregunta.variable in variablesConError

    QuestionCard(
        pregunta   = pregunta,
        estaEnFoco = estaEnFoco,
        tieneError = tieneError,
        modifier   = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (pregunta.type.lowercase()) {
                TipoPregunta.INFO ->
                    InfoQuestion(pregunta)

                TipoPregunta.TEXT,
                TipoPregunta.DATE,
                TipoPregunta.TIME,
                TipoPregunta.DATETIME ->
                    TextQuestion(pregunta, valorActual as? String ?: "", DateInputMode.PickerOrManual, onChange, editable)

                TipoPregunta.DECIMAL ->
                    NumberQuestionField(pregunta, valorActual, isDecimal = true, onChange, editable)

                TipoPregunta.NUMBER ->
                    NumberQuestionField(pregunta, valorActual, isDecimal = false, onChange, editable)

                TipoPregunta.SINGLE ->
                    SingleChoiceQuestion(pregunta, valorActual, respuestas, onChange, editable)

                TipoPregunta.MULTIPLE,
                TipoPregunta.MULTIPLE_BINARY ->
                    MultipleChoiceQuestion(pregunta, valorActual as? List<*> ?: emptyList<Any>(), respuestas, onChange, editable)

                TipoPregunta.MATRIX,
                TipoPregunta.MATRIX_SCALE ->
                    MatrixQuestionAdapter(pregunta, respuestas, variablesConError, onChange, editable)

                TipoPregunta.SLIDER ->
                    SliderQuestion(pregunta, valorActual as? Float ?: 0f, onChange, editable)

                TipoPregunta.LIKERT ->
                    LikertQuestion(pregunta, valorActual, onChange, editable)

                TipoPregunta.RANKING ->
                    RankingQuestion(pregunta, valorActual as? String ?: "", onChange, editable)

                TipoPregunta.GPS ->
                    GpsQuestion(pregunta, valorActual as? String ?: "", onChange, editable)

                TipoPregunta.PHOTO ->
                    PhotoQuestion(pregunta, valorActual as? List<*> ?: emptyList<Any>(), onChange, editable)

                else ->
                    Text("Tipo no soportado: ${pregunta.type}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ENTITY MODULE DISPATCHER
//
//  Renderiza el módulo correcto (Hogares / Personas / Visitas) directamente,
//  SIN QuestionCard wrapper, porque estos módulos tienen su propio contenedor
//  visual (EntityModuleContainer con borde y encabezado propio).
//
//  La variable de la pregunta se usa como identificador para que los callbacks
//  sepan de qué módulo viene la acción (útil si hay varios módulos en la misma
//  encuesta).
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EntityModuleDispatcher(
    pregunta: Pregunta,
    variablesConError: Set<String>,
    editable: Boolean,
    entityCallbacks: EntityModuleCallbacks,
    modifier: Modifier,
) {
    val hasError     = pregunta.variable in variablesConError
    val minRegistros = pregunta.entity_config?.min_registros ?: 1
    val maxRegistros = pregunta.entity_config?.max_registros

    // Título de sección visible encima del módulo (label de la pregunta)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (pregunta.label.isNotBlank()) {
            Text(
                text  = pregunta.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        pregunta.hint?.let {
            Text(it, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        when (pregunta.type.lowercase()) {

            TipoPregunta.ENTITY_HOGAR -> HogaresModule(
                hogares          = entityCallbacks.hogaresProvider?.invoke(pregunta.variable) ?: emptyList(),
                minRegistros     = minRegistros,
                maxRegistros     = maxRegistros,
                soloLectura      = !editable,
                hasError         = hasError,
                onAgregarHogar   = { entityCallbacks.onAgregarHogar?.invoke(pregunta.variable) },
                onEditarHogar    = { id -> entityCallbacks.onEditarHogar?.invoke(pregunta.variable, id) },
            )

            TipoPregunta.ENTITY_PERSONA -> PersonasModule(
                personas         = entityCallbacks.personasProvider?.invoke(pregunta.variable) ?: emptyList(),
                minRegistros     = minRegistros,
                maxRegistros     = maxRegistros,
                soloLectura      = !editable,
                hasError         = hasError,
                onAgregarPersona = { entityCallbacks.onAgregarPersona?.invoke(pregunta.variable) },
                onEditarPersona  = { id -> entityCallbacks.onEditarPersona?.invoke(pregunta.variable, id) },
            )

            TipoPregunta.ENTITY_VISITA -> VisitasModule(
                visitas           = entityCallbacks.visitasProvider?.invoke(pregunta.variable) ?: emptyList(),
                minRegistros      = minRegistros,
                maxRegistros      = maxRegistros,
                soloLectura       = !editable,
                hasError          = hasError,
                onRegistrarVisita = { entityCallbacks.onRegistrarVisita?.invoke(pregunta.variable) },
                onEditarVisita    = { id -> entityCallbacks.onEditarVisita?.invoke(pregunta.variable, id) },
            )

            else -> Text(
                "Módulo de entidad no soportado: ${pregunta.type}",
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}