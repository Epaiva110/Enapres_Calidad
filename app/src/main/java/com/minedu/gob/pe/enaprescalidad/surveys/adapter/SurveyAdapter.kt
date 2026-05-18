package com.minedu.gob.pe.enaprescalidad.surveys.adapter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyOption
import com.minedu.gob.pe.enaprescalidad.surveys.viewmodel.evaluarCondicion

// ─────────────────────────────────────────────────────────────────────────────
//  DISPATCHER PRINCIPAL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DynamicQuestionAdapter(
    pregunta: Pregunta,
    respuestas: Map<String, String>,
    variableEnFoco: String,
    onValueChange: (String, String) -> Unit,
) {
    // Evaluar show_if antes de mostrar
    if (!evaluarCondicion(pregunta.show_if, respuestas)) return

    val valorActual  = respuestas[pregunta.variable] ?: ""
    val estaEnFoco   = pregunta.variable == variableEnFoco

    QuestionCard(pregunta = pregunta, estaEnFoco = estaEnFoco) {
        when (pregunta.type) {
            "single"           -> SingleQuestion(pregunta, valorActual, onValueChange)
            "multiple"         -> MultipleQuestion(pregunta, valorActual, onValueChange)
            "multiple_binary"  -> MultipleBinaryQuestion(pregunta, valorActual, onValueChange)
            "matrix"           -> MatrixQuestion(pregunta, respuestas, onValueChange)
            "matrix_scale"     -> MatrixScaleQuestion(pregunta, respuestas, onValueChange)
            "text"             -> TextQuestion(pregunta, valorActual, onValueChange)
            "number"           -> NumberQuestion(pregunta, valorActual, onValueChange, isDecimal = false)
            "decimal"          -> NumberQuestion(pregunta, valorActual, onValueChange, isDecimal = true)
            "date"             -> DateQuestion(pregunta, valorActual, onValueChange)
            "time"             -> TimeQuestion(pregunta, valorActual, onValueChange)
            "datetime"         -> DateTimeQuestion(pregunta, valorActual, onValueChange)
            "gps"              -> GpsQuestion(pregunta, valorActual, onValueChange)
            "photo"            -> PhotoQuestion(pregunta, valorActual, onValueChange)
            "ranking"          -> RankingQuestion(pregunta, valorActual, onValueChange)
            "slider"           -> SliderQuestion(pregunta, valorActual, onValueChange)
            "likert"           -> LikertQuestion(pregunta, valorActual, onValueChange)
            "info"             -> InfoQuestion(pregunta)
            else               -> TextQuestion(pregunta, valorActual, onValueChange)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CARD CONTENEDOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun QuestionCard(
    pregunta: Pregunta,
    estaEnFoco: Boolean,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = if (estaEnFoco) 2.dp else 0.5.dp,
                color = if (estaEnFoco) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (estaEnFoco)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(if (estaEnFoco) 2.dp else 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (pregunta.type != "matrix" && pregunta.type != "matrix_scale" && pregunta.type != "info") {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        pregunta.variable,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp, end = 6.dp),
                    )
                    Column {
                        Text(pregunta.label, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium)
                        pregunta.hint?.let {
                            Text(it, fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (pregunta.required) {
                        Text(" *", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  SINGLE — RadioButton
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SingleQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    var otroTexto by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        pregunta.options?.forEach { opt ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onChange(pregunta.variable, opt.value!!) }
                    .background(
                        if (valorActual == opt.value)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = valorActual == opt.value,
                    onClick = { onChange(pregunta.variable, opt.value!!) })
                Text(opt.label, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
            }
        }
        // Campo "Otro especifique"
        if (pregunta.allow_other == true) {
            val otraOpc = pregunta.options?.find { it.is_other == true }
            if (otraOpc != null && valorActual == otraOpc.value) {
                OutlinedTextField(
                    value = otroTexto,
                    onValueChange = {
                        otroTexto = it
                        onChange("${pregunta.variable}_OTRO", it)
                    },
                    label = { Text("Especifique") },
                    modifier = Modifier.fillMaxWidth().padding(start = 48.dp),
                    singleLine = false, maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MULTIPLE — Checkboxes simples
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MultipleQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    val seleccionados = remember(valorActual) {
        valorActual.split(",").filter { it.isNotBlank() }.toMutableStateList()
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        pregunta.options?.forEach { opt ->
            val checked = opt.value in seleccionados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (checked) seleccionados.remove(opt.value)
                        else opt.value?.let { seleccionados.add(it) }
                        onChange(pregunta.variable, seleccionados.joinToString(","))
                    }
                    .background(
                        if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = checked, onCheckedChange = {
                    if (it) opt.value?.let { v -> seleccionados.add(v) }
                    else seleccionados.remove(opt.value)
                    onChange(pregunta.variable, seleccionados.joinToString(","))
                })
                Text(opt.label, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MULTIPLE BINARY — con Ninguno y Otro especifique
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MultipleBinaryQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    val partes       = valorActual.split("|")
    val seleccionados = remember(partes[0]) {
        partes[0].split(",").filter { it.isNotBlank() }.toMutableStateList()
    }
    var textoOtro by remember { mutableStateOf(partes.getOrNull(1) ?: "") }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        pregunta.options?.forEach { opt ->
            val checked = opt.variable in seleccionados
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            actualizarMulti(pregunta, opt, !checked, seleccionados, textoOtro, onChange)
                        }
                        .background(
                            if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = checked, onCheckedChange = {
                        actualizarMulti(pregunta, opt, it, seleccionados, textoOtro, onChange)
                    })
                    Text(opt.label, style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                    if (opt.is_none == true) {
                        Text("Excl.", fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                AnimatedVisibility(visible = opt.is_other == true && checked) {
                    OutlinedTextField(
                        value = textoOtro,
                        onValueChange = {
                            textoOtro = it
                            onChange(pregunta.variable, "${seleccionados.joinToString(",")}|$it")
                        },
                        label = { Text("Especifique") },
                        modifier = Modifier.fillMaxWidth().padding(start = 48.dp, bottom = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }
        }
    }
}

private fun actualizarMulti(
    p: Pregunta, o: SurveyOption, checked: Boolean,
    seleccionados: MutableList<String>, textoOtro: String,
    onChange: (String, String) -> Unit,
) {
    var nt = textoOtro
    if (checked) {
        if (o.is_none == true) { seleccionados.clear(); seleccionados.add(o.variable!!); nt = "" }
        else {
            seleccionados.removeIf { v -> p.options?.find { it.variable == v }?.is_none == true }
            if (o.variable !in seleccionados) seleccionados.add(o.variable!!)
        }
    } else {
        seleccionados.remove(o.variable)
        if (o.is_other == true) nt = ""
    }
    onChange(p.variable, "${seleccionados.joinToString(",")}|$nt")
}

// ─────────────────────────────────────────────────────────────────────────────
//  MATRIX — SÍ / NO / NT con columnas configurables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MatrixQuestion(pregunta: Pregunta, respuestas: Map<String, String>, onValueChange: (String, String) -> Unit) {
    val columnas = listOf("SÍ" to "1", "NO" to "2", "N.T." to "3")
    Column {
        Text(pregunta.label, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp))
        // Header
        Row(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 6.dp, horizontal = 8.dp),
        ) {
            Spacer(Modifier.weight(2f))
            columnas.forEach { (label, _) ->
                Text(label, Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        pregunta.options?.forEachIndexed { i, row ->
            val valRow = respuestas[row.variable] ?: ""
            val bgColor = if (i % 2 == 0) Color.Transparent
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            Row(
                Modifier.fillMaxWidth().background(bgColor)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(row.label, Modifier.weight(2f), fontSize = 12.sp)
                columnas.forEach { (_, code) ->
                    val deshabilitado = row.disabled_if_cols?.contains(code) == true
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (deshabilitado) {
                            Text("—", color = MaterialTheme.colorScheme.outlineVariant,
                                textAlign = TextAlign.Center)
                        } else {
                            RadioButton(
                                selected  = valRow == code,
                                onClick   = { onValueChange(row.variable!!, code) },
                            )
                        }
                    }
                }
            }
            // Campo especifique si se elige SÍ y is_other=true
            AnimatedVisibility(visible = row.is_other == true && valRow == "1") {
                OutlinedTextField(
                    value     = respuestas["${row.variable}_ESP"] ?: "",
                    onValueChange = { onValueChange("${row.variable}_ESP", it) },
                    modifier  = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, bottom = 4.dp),
                    label     = { Text("Especifique") },
                    shape     = RoundedCornerShape(8.dp),
                )
            }
            if (i < (pregunta.options?.lastIndex ?: 0)) {
                HorizontalDivider(thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MATRIX SCALE — tabla con escala numérica configurable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MatrixScaleQuestion(pregunta: Pregunta, respuestas: Map<String, String>, onValueChange: (String, String) -> Unit) {
    val min = pregunta.scale_min ?: 1
    val max = pregunta.scale_max ?: 5
    val columnas = (min..max).map { it.toString() }
    val labels   = pregunta.scale_labels ?: emptyList()

    Column {
        Text(pregunta.label, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 4.dp))
        // Etiquetas de extremos
        if (labels.size >= 2) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)) {
                Text(labels[0], fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(2f))
                Spacer(Modifier.weight(columnas.size.toFloat()))
                Text(labels[1], fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End)
            }
        }
        // Header numérico
        Row(Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 6.dp, horizontal = 8.dp)) {
            Spacer(Modifier.weight(2f))
            columnas.forEach { c ->
                Text(c, Modifier.weight(1f), textAlign = TextAlign.Center,
                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        pregunta.options?.forEachIndexed { i, row ->
            val valRow = respuestas[row.variable] ?: ""
            val bgColor = if (i % 2 == 0) Color.Transparent
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            Row(Modifier.fillMaxWidth().background(bgColor)
                .padding(vertical = 4.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(row.label, Modifier.weight(2f), fontSize = 12.sp)
                columnas.forEach { code ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        RadioButton(selected = valRow == code,
                            onClick = { onValueChange(row.variable!!, code) })
                    }
                }
            }
            if (i < (pregunta.options?.lastIndex ?: 0)) {
                HorizontalDivider(thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TEXT
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TextQuestion(pregunta: Pregunta, valor: String, onChange: (String, String) -> Unit) {
    val maxLen = pregunta.max_length
    OutlinedTextField(
        value         = valor,
        onValueChange = {
            if (maxLen == null || it.length <= maxLen) onChange(pregunta.variable, it)
        },
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { pregunta.hint?.let { h -> Text(h) } },
        singleLine    = false,
        maxLines      = 4,
        shape         = RoundedCornerShape(8.dp),
        supportingText = if (maxLen != null) {{ Text("${valor.length}/$maxLen") }} else null,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  NUMBER / DECIMAL
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NumberQuestion(
    pregunta: Pregunta, valor: String,
    onChange: (String, String) -> Unit, isDecimal: Boolean,
) {
    var error by remember { mutableStateOf<String?>(null) }
    OutlinedTextField(
        value         = valor,
        onValueChange = { raw ->
            val n = if (isDecimal) raw.toDoubleOrNull() else raw.toLongOrNull()?.toDouble()
            error = when {
                raw.isEmpty()                                  -> null
                n == null                                      -> "Valor inválido"
                pregunta.min_value != null && n < pregunta.min_value -> "Mínimo ${pregunta.min_value}"
                pregunta.max_value != null && n > pregunta.max_value -> "Máximo ${pregunta.max_value}"
                else                                           -> null
            }
            if (error == null) onChange(pregunta.variable, raw)
        },
        modifier      = Modifier.fillMaxWidth(),
        isError       = error != null,
        supportingText = error?.let {{ Text(it, color = MaterialTheme.colorScheme.error) }},
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number),
        shape         = RoundedCornerShape(8.dp),
        singleLine    = true,
        placeholder   = {
            val hint = buildString {
                pregunta.min_value?.let { append("Mín: $it  ") }
                pregunta.max_value?.let { append("Máx: $it") }
            }
            if (hint.isNotBlank()) Text(hint, fontSize = 12.sp)
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  DATE
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateQuestion(pregunta: Pregunta, valor: String, onChange: (String, String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberDatePickerState()

    OutlinedTextField(
        value         = valor.ifEmpty { "Seleccionar fecha" },
        onValueChange = {},
        readOnly      = true,
        modifier      = Modifier.fillMaxWidth().clickable { showPicker = true },
        trailingIcon  = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.CalendarMonth, null)
            }
        },
        shape = RoundedCornerShape(8.dp),
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val fecha = java.text.SimpleDateFormat("dd/MM/yyyy",
                            java.util.Locale.getDefault()).format(java.util.Date(it))
                        onChange(pregunta.variable, fecha)
                    }
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = state) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TIME
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeQuestion(pregunta: Pregunta, valor: String, onChange: (String, String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val state = rememberTimePickerState()

    OutlinedTextField(
        value         = valor.ifEmpty { "Seleccionar hora" },
        onValueChange = {},
        readOnly      = true,
        modifier      = Modifier.fillMaxWidth().clickable { showPicker = true },
        trailingIcon  = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.AccessTime, null)
            }
        },
        shape = RoundedCornerShape(8.dp),
    )

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(pregunta.variable,
                        "%02d:%02d".format(state.hour, state.minute))
                    showPicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = state) },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  DATETIME — fecha + hora combinados
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DateTimeQuestion(pregunta: Pregunta, valor: String, onChange: (String, String) -> Unit) {
    val partes = valor.split(" ")
    val fecha  = partes.getOrNull(0) ?: ""
    val hora   = partes.getOrNull(1) ?: ""

    // Reutilizamos DateQuestion y TimeQuestion con variables auxiliares
    val fechaPregunta = pregunta.copy(variable = "${pregunta.variable}_FECHA", type = "date")
    val horaPregunta  = pregunta.copy(variable = "${pregunta.variable}_HORA",  type = "time",
        label = "Hora", hint = null)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Fecha", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        DateQuestion(fechaPregunta, fecha) { _, v ->
            onChange(pregunta.variable, "$v $hora".trim())
        }
        Text("Hora", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        TimeQuestion(horaPregunta, hora) { _, v ->
            onChange(pregunta.variable, "$fecha $v".trim())
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  GPS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GpsQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    // Formato: "lat|lon|alt|precision|MODO"  donde MODO = AUTO | MANUAL | OMITIDO
    val p         = valorActual.split("|")
    var lat       by remember { mutableStateOf(p.getOrNull(0) ?: "") }
    var lon       by remember { mutableStateOf(p.getOrNull(1) ?: "") }
    var alt       by remember { mutableStateOf(p.getOrNull(2) ?: "") }
    var precision by remember { mutableStateOf(p.getOrNull(3) ?: "") }
    val modo      = p.getOrNull(4) ?: ""

    fun emitir(modo: String) = onChange(pregunta.variable, "$lat|$lon|$alt|$precision|$modo")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Botones de acción
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    // TODO: conectar con LocationViewModel real
                    // Por ahora emite las coordenadas del dispositivo cuando se integre
                    lat = ""; lon = ""; alt = ""; precision = ""
                    emitir("AUTO")
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.MyLocation, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Capturar GPS", fontSize = 12.sp)
            }
            if (pregunta.allow_skip == true) {
                OutlinedButton(
                    onClick  = { onChange(pregunta.variable, "OMITIDO") },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Omitir", fontSize = 12.sp)
                }
            }
        }

        // Chip de estado del modo
        if (modo.isNotEmpty()) {
            val (color, label) = when (modo) {
                "AUTO"    -> Color(0xFF22C55E) to "GPS capturado automáticamente"
                "MANUAL"  -> Color(0xFFF59E0B) to "Coordenadas ingresadas manualmente"
                "OMITIDO" -> Color(0xFF94A3B8) to "GPS omitido"
                else      -> Color.Gray to modo
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Campos manuales (visibles siempre para permitir corrección)
        if (modo != "OMITIDO") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GpsField("Latitud",  lat,  Modifier.weight(1f)) { lat = it;  emitir("MANUAL") }
                GpsField("Longitud", lon,  Modifier.weight(1f)) { lon = it;  emitir("MANUAL") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GpsField("Altitud",  alt,  Modifier.weight(1f)) { alt = it;  emitir("MANUAL") }
                GpsField("Precisión",precision, Modifier.weight(1f)) { precision = it; emitir("MANUAL") }
            }
        }
    }
}

@Composable
private fun GpsField(label: String, valor: String, modifier: Modifier,
                     onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value         = valor,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 10.sp) },
        modifier      = modifier,
        singleLine    = true,
        textStyle     = LocalTextStyle.current.copy(fontSize = 12.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape         = RoundedCornerShape(8.dp),
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  PHOTO
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PhotoQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    val fotos = remember(valorActual) {
        valorActual.split(",").filter { it.isNotBlank() }.toMutableStateList()
    }
    val maxFotos = pregunta.max_photos ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick  = {
                    if (fotos.size < maxFotos) {
                        val nombre = "IMG_${System.currentTimeMillis()}.jpg"
                        fotos.add(nombre)
                        onChange(pregunta.variable, fotos.joinToString(","))
                    }
                    // TODO: conectar CameraX
                },
                enabled  = fotos.size < maxFotos,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.AddAPhoto, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Tomar foto", fontSize = 12.sp)
            }
            if (pregunta.allow_gallery == true) {
                OutlinedButton(onClick = { /* TODO galería */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PhotoLibrary, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Galería", fontSize = 12.sp)
                }
            }
        }
        fotos.forEachIndexed { i, foto ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(foto, Modifier.weight(1f), fontSize = 12.sp, maxLines = 1)
                IconButton(onClick = {
                    fotos.removeAt(i)
                    onChange(pregunta.variable, fotos.joinToString(","))
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                }
            }
        }
        Text("${fotos.size}/$maxFotos foto${if (maxFotos != 1) "s" else ""}",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  RANKING — arrastrar para ordenar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RankingQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    val orden = remember(valorActual) {
        if (valorActual.isBlank()) {
            pregunta.options?.map { it.value ?: it.variable ?: "" }?.toMutableStateList()
                ?: mutableStateListOf()
        } else {
            valorActual.split(",").toMutableStateList()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Arrastra para ordenar de mayor a menor prioridad",
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        orden.forEachIndexed { i, valor ->
            val label = pregunta.options?.find { it.value == valor || it.variable == valor }?.label ?: valor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("${i + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(20.dp))
                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Column {
                    IconButton(onClick = {
                        if (i > 0) { orden.swap(i, i - 1); onChange(pregunta.variable, orden.joinToString(",")) }
                    }, modifier = Modifier.size(24.dp), enabled = i > 0) {
                        Icon(Icons.Default.KeyboardArrowUp, null, Modifier.size(16.dp))
                    }
                    IconButton(onClick = {
                        if (i < orden.lastIndex) { orden.swap(i, i + 1); onChange(pregunta.variable, orden.joinToString(",")) }
                    }, modifier = Modifier.size(24.dp), enabled = i < orden.lastIndex) {
                        Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

private fun <T> MutableList<T>.swap(a: Int, b: Int) { val t = this[a]; this[a] = this[b]; this[b] = t }

// ─────────────────────────────────────────────────────────────────────────────
//  SLIDER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SliderQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    val min   = pregunta.min_value?.toFloat() ?: 0f
    val max   = pregunta.max_value?.toFloat() ?: 10f
    val steps = ((max - min) / (pregunta.step?.toFloat() ?: 1f)).toInt() - 1
    var valor by remember { mutableStateOf(valorActual.toFloatOrNull() ?: min) }

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(min.toInt().toString(), fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text       = if (valor == min && valorActual.isBlank()) "—" else valor.toInt().toString(),
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
            )
            Text(max.toInt().toString(), fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value        = valor,
            onValueChange = { v ->
                valor = v
                onChange(pregunta.variable, v.toInt().toString())
            },
            valueRange   = min..max,
            steps        = steps.coerceAtLeast(0),
            modifier     = Modifier.fillMaxWidth(),
        )
        val labels = pregunta.scale_labels
        if (labels?.size == 2) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(labels[0], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(labels[1], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  LIKERT — estrellas, emojis o números
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LikertQuestion(pregunta: Pregunta, valorActual: String, onChange: (String, String) -> Unit) {
    val count  = pregunta.likert_count ?: 5
    val tipo   = pregunta.likert_type ?: "stars"
    val emojis = listOf("😞", "😕", "😐", "🙂", "😄")

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        (1..count).forEach { i ->
            val selected = valorActual.toIntOrNull() == i
            when (tipo) {
                "stars" -> IconButton(onClick = { onChange(pregunta.variable, i.toString()) }) {
                    Icon(
                        if (i <= (valorActual.toIntOrNull() ?: 0)) Icons.Default.Star
                        else Icons.Default.StarBorder,
                        contentDescription = "$i",
                        tint   = if (i <= (valorActual.toIntOrNull() ?: 0)) Color(0xFFFBC02D)
                        else MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(32.dp),
                    )
                }
                "emoji" -> Text(
                    text     = emojis.getOrElse(i - 1) { i.toString() },
                    fontSize = if (selected) 32.sp else 24.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .padding(4.dp)
                        .clickable { onChange(pregunta.variable, i.toString()) },
                )
                else -> OutlinedButton(
                    onClick  = { onChange(pregunta.variable, i.toString()) },
                    modifier = Modifier.size(40.dp),
                    shape    = CircleShape,
                    colors   = if (selected) ButtonDefaults.buttonColors()
                    else ButtonDefaults.outlinedButtonColors(),
                    contentPadding = PaddingValues(0.dp),
                ) { Text(i.toString(), fontWeight = FontWeight.Bold) }
            }
        }
    }
    val labels = pregunta.scale_labels
    if (labels?.size == 2) {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(labels[0], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(labels[1], fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  INFO — solo texto, sin respuesta
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InfoQuestion(pregunta: Pregunta) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(Icons.Default.Info, null,
            tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
        Column {
            Text(pregunta.label, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
            pregunta.hint?.let {
                Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}