package com.minedu.gob.pe.enaprescalidad.surveys.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyOption

@Composable
fun DynamicQuestionAdapter(
    pregunta: Pregunta,
    respuestas: Map<String, String>,
    variableEnFoco: String,
    onValueChange: (String, String) -> Unit
) {
    val valorActual = respuestas[pregunta.variable] ?: ""
    val estaEnFoco = pregunta.variable == variableEnFoco

    QuestionCard(
        pregunta = pregunta,
        estaEnFoco = estaEnFoco
    ) {

        when (pregunta.type) {
            "matrix" -> QuestionMatrix(pregunta, respuestas, onValueChange)

            "single" -> SingleQuestion(
                pregunta,
                valorActual,
                onValueChange
            )

            "multiple_binary" -> MultipleBinaryQuestion(
                pregunta,
                valorActual,
                onValueChange
            )

            "gps" -> GpsQuestion(
                pregunta,
                valorActual,
                onValueChange
            )

            "photo" -> PhotoQuestion(
                pregunta,
                valorActual,
                onValueChange
            )

            "text" -> TextQuestion(
                pregunta,
                valorActual,
                onValueChange
            )
        }
    }
}

@Composable
fun PhotoQuestion(
    pregunta: Pregunta,
    valorActual: String,
    onValueChange: (String, String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Button(
            onClick = {
                // Aquí luego puedes conectar CameraX o intent de cámara
                val fileName = "IMG_${System.currentTimeMillis()}.jpg"
                onValueChange(pregunta.variable, fileName)
            }
        ) {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "Tomar foto"
            )
            Spacer(Modifier.width(6.dp))
            Text("Foto")
        }

        if (valorActual.isNotEmpty()) {
            Column {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
                Text(
                    text = "Guardado",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun QuestionCard(
    pregunta: Pregunta,
    estaEnFoco: Boolean,
    content: @Composable () -> Unit
) {
    val highlightColor = Color(0xFFFFF9C4)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = if (estaEnFoco) 2.5.dp else 0.5.dp,
                color = if (estaEnFoco) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (estaEnFoco) highlightColor else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (pregunta.type != "matrix") {
                Text(
                    "${pregunta.variable}. ${pregunta.label}",
                    fontWeight = FontWeight.Bold
                )
                pregunta.hint?.let {
                    Text(it, fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(12.dp))
            }

            content()
        }
    }
}

@Composable
fun SingleQuestion(
    pregunta: Pregunta,
    valorActual: String,
    onChange: (String, String) -> Unit
) {
    pregunta.options?.forEach { opt ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChange(pregunta.variable, opt.value!!) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = valorActual == opt.value,
                onClick = { onChange(pregunta.variable, opt.value!!) }
            )
            Text(opt.label)
        }
    }
}

@Composable
fun MultipleBinaryQuestion(
    pregunta: Pregunta,
    valorActual: String,
    onChange: (String, String) -> Unit
) {
    val partes = valorActual.split("|")
    val seleccionados = partes.getOrNull(0)
        ?.split(",")
        ?.filter { it.isNotBlank() }
        ?.toMutableList() ?: mutableListOf()

    val textoOtros = partes.getOrNull(1) ?: ""

    pregunta.options?.forEach { opt ->
        val checked = seleccionados.contains(opt.variable)

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        actualizarMulLogica(
                            pregunta, opt, it,
                            seleccionados, textoOtros, onChange
                        )
                    }
                )
                Text(opt.label)
            }

            if (opt.is_other == true && checked) {
                OutlinedTextField(
                    value = textoOtros,
                    onValueChange = {
                        onChange(
                            pregunta.variable,
                            "${seleccionados.joinToString(",")}|$it"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun GpsQuestion(
    pregunta: Pregunta,
    valorActual: String,
    onChange: (String, String) -> Unit
) {
    val p = valorActual.split("|")
    val lat = p.getOrNull(0) ?: ""
    val lon = p.getOrNull(1) ?: ""

    Column {
        Button(
            onClick = {
                onChange(
                    pregunta.variable,
                    "-12.046372|-77.042781|||AUTO"
                )
            }
        ) {
            Text("GPS")
        }

        Row {
            GpsField("Latitud", lat, true, Modifier.weight(1f)) {
                onChange(pregunta.variable, "$it|$lon|||MANUAL")
            }
            GpsField("Longitud", lon, true, Modifier.weight(1f)) {
                onChange(pregunta.variable, "$lat|$it|||MANUAL")
            }
        }
        Row {
            GpsField("Altitud", lon, true, Modifier.weight(1f)) {
                onChange(pregunta.variable, "$lat|$it|||MANUAL")
            }
            GpsField("Precisión", lon, true, Modifier.weight(1f)) {
                onChange(pregunta.variable, "$lat|$it|||MANUAL")
            }
        }
    }
}

@Composable
fun TextQuestion(
    pregunta: Pregunta,
    valor: String,
    onChange: (String, String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { onChange(pregunta.variable, it) },
        modifier = Modifier.fillMaxWidth()
    )
}

private fun actualizarMulLogica(p: Pregunta, o: SurveyOption, c: Boolean, s: MutableList<String>, t: String, onChange: (String, String) -> Unit) {
    var nt = t
    if (c) {
        if (o.is_none == true) { s.clear(); s.add(o.variable!!); nt = "" }
        else { s.removeIf { v -> p.options?.find { it.variable == v }?.is_none == true }; s.add(o.variable!!) }
    } else { s.remove(o.variable); if (o.is_other == true) nt = "" }
    onChange(p.variable, "${s.joinToString(",")}|$nt")
}

@Composable
fun QuestionMatrix(pregunta: Pregunta, respuestas: Map<String, String>, onValueChange: (String, String) -> Unit) {
    Column {
        Text(pregunta.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth().background(Color(0xFFE0E0E0)).padding(4.dp)) {
            Spacer(Modifier.weight(1.5f))
            listOf("SÍ", "NO", "N.T.").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
        pregunta.options?.forEach { row ->
            val valRow = respuestas[row.variable] ?: ""
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(row.label, Modifier.weight(1.5f), fontSize = 12.sp)
                listOf("1", "2", "3").forEach { col ->
                    val esAuto = row.variable == "P101_2" || row.variable == "P101_3"
                    val habilitado = !(col == "3" && !esAuto)
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (habilitado) RadioButton(selected = valRow == col, onClick = { onValueChange(row.variable!!, col) }) else Text("-", color = Color.LightGray)
                    }
                }
            }
            if (row.is_other == true && valRow == "1") {
                OutlinedTextField(
                    value = respuestas["${row.variable}_ESP"] ?: "",
                    onValueChange = { onValueChange("${row.variable}_ESP", it) },
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
                    label = { Text("Especifique...") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
        }
    }
}

@Composable
fun GpsField(l: String, v: String, e: Boolean, m: Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(value = v, onValueChange = onValueChange, enabled = e, label = { Text(l, fontSize = 9.sp) }, modifier = m, singleLine = true, textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}