package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta

@Composable
fun NumberQuestionField(
    pregunta: Pregunta,
    value: Any?,
    isDecimal: Boolean,
    onValueChange: (String, Any?) -> Unit,
) {

    if (isDecimal) {

        DecimalQuestionField(
            pregunta = pregunta,
            value = value as? Double,
            onValueChange = onValueChange
        )

    } else {

        IntegerQuestionField(
            pregunta = pregunta,
            value = value as? Int,
            onValueChange = onValueChange
        )
    }
}

@Composable
fun IntegerQuestionField(
    pregunta: Pregunta,
    value: Int?,
    onValueChange: (String, Any?) -> Unit,
) {

    var text by remember {
        mutableStateOf(value?.toString() ?: "")
    }

    var hasError by remember {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        OutlinedTextField(

            value = text,

            onValueChange = { input ->

                // SOLO números
                val filtered = input.filter {
                    it.isDigit()
                }

                text = filtered

                val number = filtered.toIntOrNull()

                hasError =
                    number != null &&
                            (
                                    (pregunta.min_value != null && number < pregunta.min_value) ||
                                            (pregunta.max_value != null && number > pregunta.max_value)
                                    )

                onValueChange(
                    pregunta.variable,
                    number
                )
            },

            modifier = Modifier.fillMaxWidth(),

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),

            singleLine = true,

            isError = hasError,

            placeholder = {
                Text("0")
            },

            shape = RoundedCornerShape(10.dp),

            suffix = pregunta.hint?.let {
                { Text(it, fontSize = 12.sp) }
            }
        )

        RangeText(
            pregunta = pregunta,
            hasError = hasError
        )
    }
}

@Composable
fun DecimalQuestionField(
    pregunta: Pregunta,
    value: Double?,
    onValueChange: (String, Any?) -> Unit,
) {

    var text by remember {
        mutableStateOf(value?.toString() ?: "")
    }

    var hasError by remember {
        mutableStateOf(false)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        OutlinedTextField(

            value = text,

            onValueChange = { input ->

                // solo números y UN punto
                val filtered = buildString {

                    var dotCount = 0

                    input.forEach {

                        if (it.isDigit()) {
                            append(it)
                        }

                        else if (it == '.' && dotCount == 0) {
                            append(it)
                            dotCount++
                        }
                    }
                }

                text = filtered

                val number = filtered.toDoubleOrNull()

                hasError =
                    number != null &&
                            (
                                    (pregunta.min_value != null && number < pregunta.min_value) ||
                                            (pregunta.max_value != null && number > pregunta.max_value)
                                    )

                onValueChange(
                    pregunta.variable,
                    number
                )
            },

            modifier = Modifier.fillMaxWidth(),

            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            ),

            singleLine = true,

            isError = hasError,

            placeholder = {
                Text("0.00")
            },

            shape = RoundedCornerShape(10.dp),

            suffix = pregunta.hint?.let {
                { Text(it, fontSize = 12.sp) }
            }
        )

        RangeText(
            pregunta = pregunta,
            hasError = hasError
        )
    }
}

@Composable
private fun RangeText(
    pregunta: Pregunta,
    hasError: Boolean
) {

    val min = pregunta.min_value
    val max = pregunta.max_value

    if (min != null || max != null) {

        Text(

            buildString {

                if (min != null) {
                    append("Mín: $min")
                }

                if (min != null && max != null) {
                    append("  •  ")
                }

                if (max != null) {
                    append("Máx: $max")
                }
            },

            fontSize = 10.sp,

            color =
                if (hasError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}