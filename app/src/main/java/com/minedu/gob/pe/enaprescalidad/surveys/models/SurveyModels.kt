package com.minedu.gob.pe.enaprescalidad.surveys.models

// ─────────────────────────────────────────────────────────────────────────────
// SURVEY & CONFIG
// ─────────────────────────────────────────────────────────────────────────────
/**
 * Estructura raíz que define una Encuesta completa.
 * Modela la metadata, las configuraciones globales de comportamiento y las pantallas.
 */
data class Survey(
    val survey_id: String,           // Identificador único de la encuesta (UUID / Backend ID)
    val title: String,               // Título público de la encuesta
    val config: SurveyConfig,        // Configuración global de comportamiento y estilos de UI
    val paginas: List<Pagina>        // Flujo ordenado de pantallas que componen la encuesta
)

/**
 * Configuraciones globales del comportamiento de la encuesta en el dispositivo.
 */
data class SurveyConfig(
    val color_resaltado: String = "#1565C0",     // Color hexadecimal principal para botones y acentos de UI
    val min_caracteres_observacion: Int = 3,    // Mínimo de texto requerido en campos abiertos/observaciones
    val guardar_automatico: Boolean = true,     // Indica si se debe persistir localmente ante cada respuesta
    val mostrar_progreso: Boolean = true         // Muestra u oculta la barra de avance (porcentaje/páginas)
)

/**
 * Representa una pantalla o vista única en la aplicación.
 * Permite agrupar preguntas lógicamente para un renderizado paginado.
 */
data class Pagina(
    val id_pagina: Int,             // Índice o número de página (utilizado para navegación y saltos)
    val seccion_id: String,         // ID del bloque modular al que pertenece (ej: "modulo_demografico")
    val titulo_seccion: String,     // Encabezado del bloque o categoría superior
    val titulo: String,             // Título específico de esta pantalla
    val preguntas: List<Pregunta>   // Listado de reactivos que se renderizarán en esta vista
)

// ─────────────────────────────────────────────────────────────────────────────
// PREGUNTA Y OPCIONES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Definición del átomo del motor: La Pregunta.
 * Contiene propiedades de UI, restricciones de negocio, metadatos y reglas de ramificación.
 */
data class Pregunta(
    val id: String? = null,                  // Identificador único del reactivo (opcional si se usa 'variable')
    val variable: String,                    // Llave clave (Key) con la que se guardará la respuesta en el mapa
    val type: String,                        // Tipo de componente visual (text, number, radio, checkbox, info, etc.)
    val label: String,                       // Enunciado o pregunta que se le muestra al usuario
    val required: Boolean = false,           // Restricción: true si el campo no puede quedar vacío
    val hint: String? = null,                // Texto de ayuda, placeholder o sugerencia inferior
    val options: List<SurveyOption>? = null, // Catálogo de opciones de respuesta para componentes de selección

    // ── Validaciones de longitud/valores numéricos ──
    val min_length: Int? = null,             // Mínimo de caracteres permitidos (para campos de texto)
    val max_length: Int? = null,             // Límite máximo de caracteres (para evitar desbordamientos de datos)
    val min_value: Double? = null,           // Límite numérico inferior permitido (para type: number)
    val max_value: Double? = null,           // Límite numérico superior permitido
    val step: Double? = 1.0,                 // Incremento/decremento permitido (ej: 0.5 en 0.5)

    // ── Matrix / Likert / Escalas ──
    val scale_min: Int? = 1,                 // Valor inicial para componentes tipo Slider o Escala Continua
    val scale_max: Int? = 5,                 // Valor final para componentes tipo Slider o Escala Continua
    val scale_labels: List<String>? = null,  // Textos en los extremos de la escala (ej: ["Malo", "Excelente"])
    val likert_type: String? = "stars",      // Estilo visual Likert ("stars", "smiley", "numeric_buttons")
    val likert_count: Int? = 5,              // Cantidad de niveles/opciones en la escala de opinión

    // ── Captura Multimedia ──
    val max_photos: Int? = 1,                // Cantidad máxima de imágenes que se pueden capturar/adjuntar
    val allow_gallery: Boolean? = false,     // Restricción: true permite subir desde carrete, false obliga a usar cámara

    // ── Flags de comportamiento visual y de negocio ──
    val allow_other: Boolean? = false,       // Inyecta dinámicamente la opción "Otro (Especifique)" al final
    val allow_skip: Boolean? = false,        // Permite omitir la pregunta mediante un botón explícito de "Omitir"
    val allow_manual: Boolean? = false,      // Permite entrada manual en componentes que automatizan captura (ej: GPS)

    // ── Lógica Dinámica y Navegación ──
    val show_if: ConditionNode? = null,      // Árbol de condiciones que dictamina si la pregunta es visible o se oculta
    val jump_to_page: Int? = null            // Destino de salto directo (ID de página) al responder esta pregunta
)

/**
 * Representa una opción seleccionable dentro de una pregunta (Radio, Checkbox, Dropdown).
 */
data class SurveyOption(
    val value: String? = null,               // Valor duro que se almacenará en la base de datos (ej: "1")
    val variable: String? = null,            // Sub-variable opcional para almacenamiento complejo o cruzado
    val label: String,                       // Texto visible de la opción (ej: "Masculino")
    val jump_to_page: Int? = null,           // Flujo alternativo: Si se selecciona esta opción, salta a esta página
    val is_none: Boolean? = false,           // Flag de exclusividad: Si se marca, desmarca el resto de opciones (ej: "Ninguna")
    val is_other: Boolean? = false,          // Disparador de campo abierto para especificar texto libre
    val disabled_if_cols: List<String>? = null, // Lógica matricial: Deshabilita columnas si ya se usaron en filas previas

    // ── Preguntas anidadas (Sub-formularios / Cascadas) ──
    val detail_questions: List<Pregunta>? = null, // Preguntas ocultas que aparecen solo al marcar esta opción
    val detail_display: String? = "dialog",       // Presentación del sub-formulario: "dialog" (emergente) o "inline" (expandible)
    val open_detail_if_selected: Boolean = false  // Forzar la apertura inmediata del detalle al hacer click
)

// ─────────────────────────────────────────────────────────────────────────────
// ESTADO DE RESPUESTAS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Almacén del estado de la encuesta actual. Desacoplada de la estructura fija.
 */
data class SurveyResponse(
    val survey_id: String,                                // ID de la encuesta que se está respondiendo
    val answers: MutableMap<String, Any?> = mutableMapOf(), // Mapa Reactivo: Key = variable de la pregunta, Value = respuesta ingresada
    val started_at: Long = System.currentTimeMillis(),    // Timestamp del inicio de la sesión de captura
    var updated_at: Long = System.currentTimeMillis()     // Timestamp del último cambio registrado en el formulario
)

/**
 * Clase sellada (Sealed) que implementa el Patrón Composite.
 * Define la raíz jerárquica para construir árboles lógicos complejos de validación y visibilidad.
 */
sealed class ConditionNode

/**
 * Representa una compuerta lógica (AND / OR) que agrupa y evalúa una lista de condiciones anidadas.
 */
data class ConditionGroup(
    val operator: String,                  // Operador de control: "AND" (Todas deben cumplir) o "OR" (Al menos una)
    val conditions: List<ConditionNode>    // Nodos hijos que pueden ser otros Grupos o Reglas atómicas (Estructura de Árbol)
) : ConditionNode()

/**
 * Regla de evaluación atómica y unitaria. Compara una variable del mapa contra un valor estático.
 */
data class ConditionRule(
    val variable: String,                  // Nombre de la variable en el mapa de respuestas a evaluar (Ej: "EDAD")
    val operator: String,                  // Operador de comparación (eq, neq, gt, lt, in, regex, etc.)
    val value: Any? = null                 // Valor de control fijo contra el que se realiza la comparación (Ej: 18)
) : ConditionNode()

/**
 * Regla de validación cruzada y matricial.
 * Aplica operaciones aritméticas, de conteo o de cuantificación sobre múltiples variables al mismo tiempo.
 */
data class GroupConditionRule(
    val variables: List<String>,           // Conjunto de variables involucradas en el análisis (Ej: ["P1", "P2", "P3"])
    val operator: String,                  // Operador matricial (any_eq, count_gt, sum_lte, avg_gte, etc.)
    val value: Any? = null,                // Valor objetivo o de comparación estática
    val count: Int? = null                 // Umbral numérico requerido exclusivamente por operadores de tipo 'count_*'
) : ConditionNode()

// ─────────────────────────────────────────────────────────────────────────────
// EVALUADOR DEL MOTOR DE REGLAS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Procesador encargado de resolver el árbol de condiciones (`ConditionNode`)
 * traduciendo configuraciones de datos en resultados booleanos en tiempo de ejecución.
 */
class ConditionEvaluator {

    /**
     * Punto de entrada principal para evaluar cualquier nodo del árbol de condiciones.
     * Evalúa recursivamente según el tipo de nodo instanciado.
     */
    fun evaluate(node: ConditionNode, answers: Map<String, Any?>): Boolean =
        when (node) {
            is ConditionGroup      -> evaluateGroup(node, answers)
            is ConditionRule       -> evaluateRule(node, answers)
            is GroupConditionRule  -> evaluateGroupRule(node, answers)
        }

    /**
     * Resuelve compuertas lógicas complejas utilizando cortocircuitos lógicos nativos.
     */
    private fun evaluateGroup(group: ConditionGroup, answers: Map<String, Any?>): Boolean =
        if (group.operator.equals("OR", ignoreCase = true))
        // OR: Retorna true inmediatamente al encontrar la primera condición verdadera
            group.conditions.any { evaluate(it, answers) }
        else
        // AND: Retorna false inmediatamente si una sola condición del grupo no se cumple
            group.conditions.all { evaluate(it, answers) }

    /**
     * Resuelve la lógica para una variable individual frente a un valor constante.
     */
    private fun evaluateRule(rule: ConditionRule, answers: Map<String, Any?>): Boolean {
        val raw = answers[rule.variable]            // Obtiene el valor real del mapa de respuestas
        val rawStr = raw?.toString() ?: ""          // Normaliza la respuesta a String para homologar validaciones de texto
        val ruleStr = rule.value?.toString() ?: ""  // Normaliza el valor de control configurado en la regla

        return when (rule.operator.lowercase()) {
            "is_null"     -> raw == null                    // True si la pregunta no ha sido respondida
            "not_null"    -> raw != null                    // True si la pregunta ya posee una respuesta
            "is_empty"    -> rawStr.isEmpty()               // True si la respuesta es una cadena vacía ""
            "not_empty"   -> rawStr.isNotEmpty()            // True si hay caracteres capturados
            "eq"          -> rawStr == ruleStr              // Igualdad estricta de cadenas de texto
            "neq"         -> rawStr != ruleStr              // Diferencia estricta de cadenas de texto
            "gt"          -> compareNum(raw, rule.value) { it > 0 }  // Mayor que (>) numérico
            "gte"         -> compareNum(raw, rule.value) { it >= 0 } // Mayor o igual que (>=) numérico
            "lt"          -> compareNum(raw, rule.value) { it < 0 }  // Menor que (<) numérico
            "lte"         -> compareNum(raw, rule.value) { it <= 0 } // Menor o igual que (<=) numérico
            "contains"    -> rawStr.contains(ruleStr, ignoreCase = true)    // Búsqueda parcial (Case-Insensitive)
            "not_contains"-> !rawStr.contains(ruleStr, ignoreCase = true)   // Exclusión parcial de texto
            "starts_with" -> rawStr.startsWith(ruleStr)     // Comprobación de prefijo (Sensible a mayúsculas)
            "ends_with"   -> rawStr.endsWith(ruleStr)       // Comprobación de sufijo (Sensible a mayúsculas)
            "in"          -> {
                // Evalúa si la respuesta está dentro de un arreglo o un String con valores separados por comas
                val lista = toStringList(rule.value)
                lista.contains(rawStr)
            }
            "not_in"      -> {
                // CORRECCIÓN BUG 3: Evita desbordamiento (Crash por !!) controlando listas vacías de forma segura
                val lista = toStringList(rule.value)
                !lista.contains(rawStr)
            }
            // Ejecución segura de expresiones regulares. Si la regex está rota en base de datos, captura el error y retorna false.
            "regex"       -> runCatching { rawStr.matches(Regex(ruleStr)) }.getOrElse { false }
            else          -> false                          // Si el operador no existe, la regla no se cumple por seguridad
        }
    }

    /**
     * Resuelve validaciones avanzadas que cruzan información de múltiples variables a la vez.
     */
    private fun evaluateGroupRule(rule: GroupConditionRule, answers: Map<String, Any?>): Boolean {
        val values    = rule.variables.map { answers[it] } // Mapea las variables solicitadas a sus valores actuales
        val strValues = values.map { it?.toString() }      // Conversión homogénea a String para evaluaciones lógicas
        val targetStr = rule.value?.toString()             // Valor estático de comparación convertido a String

        return when (rule.operator.lowercase()) {
            // ── Cuantificadores Lógicos ──
            "any_eq"   -> strValues.any { it == targetStr }    // True si alguna variable del grupo coincide con el objetivo
            "all_eq"   -> strValues.all { it == targetStr }    // True si absolutamente todas coinciden con el objetivo
            "none_eq"  -> strValues.none { it == targetStr }   // True si ninguna variable es igual al objetivo
            "any_neq"  -> strValues.any { it != targetStr }    // True si al menos una variable es diferente
            "all_neq"  -> strValues.all { it != targetStr }    // True si ninguna coincide con el objetivo original
            "all_null" -> values.all { it == null }            // True si todo el bloque de preguntas está sin responder
            "any_null" -> values.any { it == null }            // True si falta responder al menos una del grupo
            "all_empty"-> strValues.all { it.isNullOrEmpty() } // True si todas están vacías o nulas
            "any_empty"-> strValues.any { it.isNullOrEmpty() } // True si al menos una del grupo está vacía o nula

            // ── Algoritmos de Conteo Específico ──
            // Cuentan cuántas preguntas cumplen el criterio y lo comparan contra el entero requerido ('rule.count')
            "count_eq" -> strValues.count { it == targetStr } == (rule.count ?: 0)
            "count_gte"-> strValues.count { it == targetStr } >= (rule.count ?: 0)
            "count_lte"-> strValues.count { it == targetStr } <= (rule.count ?: 0)
            "count_gt" -> strValues.count { it == targetStr } > (rule.count ?: 0)
            "count_lt" -> strValues.count { it == targetStr } < (rule.count ?: 0)

            // ── Operaciones Aritméticas Agregadas ──
            // Filtran el grupo extrayendo solo números, calculan la operación y la evalúan contra el valor objetivo
            "sum_gt"   -> numericOp(values) { it > (targetDouble(rule)) }  // Sumatoria total mayor que (>)
            "sum_gte"  -> numericOp(values) { it >= (targetDouble(rule)) } // Sumatoria total mayor o igual que (>=)
            "sum_lt"   -> numericOp(values) { it < (targetDouble(rule)) }  // Sumatoria total menor que (<)
            "sum_lte"  -> numericOp(values) { it <= (targetDouble(rule)) } // Sumatoria total menor o igual que (<=)
            "sum_eq"   -> numericOp(values) { it == (targetDouble(rule)) } // Sumatoria total exactamente igual a (==)
            "avg_gt"   -> avgOp(values) { it > (targetDouble(rule)) }      // Promedio del grupo mayor que (>)
            "avg_gte"  -> avgOp(values) { it >= (targetDouble(rule)) }     // Promedio del grupo mayor o igual que (>=)
            "avg_lt"   -> avgOp(values) { it < (targetDouble(rule)) }      // Promedio del grupo menor que (<)
            "avg_lte"  -> avgOp(values) { it <= (targetDouble(rule)) }     // Promedio del grupo menor o igual que (<=)
            "avg_eq"   -> avgOp(values) { it == (targetDouble(rule)) }     // Promedio del grupo exactamente igual a (==)
            else       -> false
        }
    }

    // ── Helpers de Conversión y Tolerancia a Fallos ───────────────────────────

    /**
     * Compara de forma segura dos objetos transformándolos a Double en tiempo de ejecución.
     * Previene fallas de conversión si la UI retorna tipos mezclados (Int, Float, String).
     * @param op Expresión lambda que recibe el entero del `.compareTo()` para aplicar la lógica relacional.
     */
    private fun compareNum(a: Any?, b: Any?, op: (Int) -> Boolean): Boolean {
        val na = a?.toString()?.toDoubleOrNull() ?: return false
        val nb = b?.toString()?.toDoubleOrNull() ?: return false
        return op(na.compareTo(nb))
    }

    /**
     * Parsea polimórficamente un objeto dinámico para construir un listado homogéneo de cadenas.
     * Útil para los operadores "in" y "not_in". Soporta objetos tipo `List` nativos o cadenas delimitadas por comas.
     */
    private fun toStringList(value: Any?): List<String> = when (value) {
        is List<*> -> value.map { it.toString() }                       // Mapea cada elemento interno a String
        is String  -> value.split(",").map { it.trim() }                // Rompe la cadena por comas y remueve espacios fantasmas
        else       -> emptyList()                                       // Ante tipos no soportados, retorna una lista vacía inmune a fallas
    }

    /**
     * Helper de extracción: Convierte el valor objetivo de una regla grupal a un Double válido.
     * Si no es convertible, por defecto retorna 0.0 de forma segura.
     */
    private fun targetDouble(rule: GroupConditionRule) =
        rule.value?.toString()?.toDoubleOrNull() ?: 0.0

    /**
     * Ejecuta una suma acumulativa sobre un conjunto de valores.
     * `mapNotNull` se encarga de ignorar y remover cualquier texto no convertible para que la encuesta no sufra crashes.
     */
    private fun numericOp(values: List<Any?>, check: (Double) -> Boolean): Boolean {
        val sum = values.mapNotNull { it?.toString()?.toDoubleOrNull() }.sum()
        return check(sum)
    }

    /**
     * Calcula el promedio aritmético de las variables que contienen respuestas numéricas.
     * Si ninguna de las variables del grupo ha sido respondida con números, retorna false para mitigar divisiones por cero.
     */
    private fun avgOp(values: List<Any?>, check: (Double) -> Boolean): Boolean {
        val nums = values.mapNotNull { it?.toString()?.toDoubleOrNull() }
        return if (nums.isEmpty()) false else check(nums.average())
    }
}