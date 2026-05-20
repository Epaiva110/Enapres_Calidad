package com.minedu.gob.pe.enaprescalidad.surveys.ui.component.question

// ================================
// IMPORTS
// ================================

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minedu.gob.pe.enaprescalidad.surveys.models.Pregunta
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


// ================================
// MODEOS DE CAPTURA
// ================================

sealed class DateInputMode {

    // toma automáticamente fecha/hora actual
    data object Automatic : DateInputMode()

    // solo picker
    data object PickerOnly : DateInputMode()

    // picker + escritura manual
    data object PickerOrManual : DateInputMode()
}

// ================================
// DISPATCHER PRINCIPAL
// ================================

@Composable
fun TextQuestion(
    pregunta: Pregunta,
    value: Any?,
    mode: DateInputMode = DateInputMode.PickerOnly,
    onValueChange: (String, Any?) -> Unit
) {

    when (pregunta.type) {

        "text" -> {
            TextQuestionField(
                pregunta = pregunta,
                value = value as? String ?: "",
                onValueChange = onValueChange
            )
        }

        "date" -> {
            DateQuestionField(
                pregunta = pregunta,
                value = value as? LocalDate,
                mode = mode,
                onValueChange = onValueChange
            )
        }

        "time" -> {
            TimeQuestionField(
                pregunta = pregunta,
                value = value as? LocalTime,
                mode = mode,
                onValueChange = onValueChange
            )
        }

        "datetime" -> {
            DateTimeQuestionField(
                pregunta = pregunta,
                value = value as? LocalDateTime,
                mode = mode,
                onValueChange = onValueChange
            )
        }
    }
}

// ================================
// TEXT FIELD
// ================================

@Composable
fun TextQuestionField(
    pregunta: Pregunta,
    value: String,
    onValueChange: (String, Any?) -> Unit
) {

    OutlinedTextField(
        value = value,

        onValueChange = {
            onValueChange(pregunta.variable, it)
        },

        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),

        placeholder = {
            Text(
                pregunta.hint ?: "",
                fontSize = 13.sp
            )
        },

        maxLines = 5,

        shape = RoundedCornerShape(10.dp),

        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),

        supportingText = {
            pregunta.max_length?.let {

                Text(
                    text = "${value.length}/$it",

                    color =
                        if (value.length > it)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,

                    fontSize = 10.sp
                )
            }
        }
    )
}

// ================================
// DATE FIELD
// ================================

@Composable
fun DateQuestionField(
    pregunta: Pregunta,
    value: LocalDate?,
    mode: DateInputMode,
    onValueChange: (String, Any?) -> Unit
) {

    val context = LocalContext.current

    val formatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    var text by remember {
        mutableStateOf(value?.format(formatter) ?: "")
    }

    LaunchedEffect(mode) {

        if (mode is DateInputMode.Automatic && value == null) {

            val now = LocalDate.now()

            text = now.format(formatter)

            onValueChange(
                pregunta.variable,
                now
            )
        }
    }

    fun openDatePicker() {

        val current = value ?: LocalDate.now()

        DatePickerDialog(
            context,
            { _, year, month, day ->

                val selected = LocalDate.of(
                    year,
                    month + 1,
                    day
                )

                text = selected.format(formatter)

                onValueChange(
                    pregunta.variable,
                    selected
                )

            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    OutlinedTextField(

        value = text,

        onValueChange = {

            if (mode is DateInputMode.PickerOrManual) {

                text = it
            }
        },

        modifier = Modifier.fillMaxWidth(),

        readOnly = mode != DateInputMode.PickerOrManual,

        placeholder = {
            Text("dd/mm/aaaa")
        },

        trailingIcon = {

            IconButton(
                onClick = {
                    openDatePicker()
                }
            ) {

                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null
                )
            }
        },

        shape = RoundedCornerShape(10.dp)
    )
}




// ================================
// TIME FIELD
// ================================

@Composable
fun TimeQuestionField(
    pregunta: Pregunta,
    value: LocalTime?,
    mode: DateInputMode,
    onValueChange: (String, Any?) -> Unit
) {

    val context = LocalContext.current

    val formatter = remember {
        DateTimeFormatter.ofPattern("HH:mm")
    }

    var text by remember {
        mutableStateOf(value?.format(formatter) ?: "")
    }

    LaunchedEffect(mode) {

        if (mode is DateInputMode.Automatic && value == null) {

            val now = LocalTime.now()

            text = now.format(formatter)

            onValueChange(
                pregunta.variable,
                now
            )
        }
    }

    fun openTimePicker() {

        val current = value ?: LocalTime.now()

        TimePickerDialog(
            context,
            { _, hour, minute ->

                val selected = LocalTime.of(
                    hour,
                    minute
                )

                text = selected.format(formatter)

                onValueChange(
                    pregunta.variable,
                    selected
                )

            },
            current.hour,
            current.minute,
            true
        ).show()
    }

    OutlinedTextField(

        value = text,

        onValueChange = {

            if (mode is DateInputMode.PickerOrManual) {

                text = it
            }
        },

        modifier = Modifier.fillMaxWidth(),

        readOnly = mode != DateInputMode.PickerOrManual,

        placeholder = {
            Text("hh:mm")
        },

        trailingIcon = {

            IconButton(
                onClick = {
                    openTimePicker()
                }
            ) {

                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null
                )
            }
        },

        shape = RoundedCornerShape(10.dp)
    )
}




// ================================
// DATETIME FIELD
// ================================

@Composable
fun DateTimeQuestionField(
    pregunta: Pregunta,
    value: LocalDateTime?,
    mode: DateInputMode,
    onValueChange: (String, Any?) -> Unit
) {

    val context = LocalContext.current

    val formatter = remember {
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    }

    var dateTime by remember {
        mutableStateOf(value)
    }

    var text by remember {
        mutableStateOf(
            value?.format(formatter) ?: ""
        )
    }

    LaunchedEffect(mode) {

        if (mode is DateInputMode.Automatic && value == null) {

            val now = LocalDateTime.now()

            dateTime = now

            text = now.format(formatter)

            onValueChange(
                pregunta.variable,
                now
            )
        }
    }

    fun openDatePicker() {

        val current = dateTime ?: LocalDateTime.now()

        DatePickerDialog(
            context,
            { _, year, month, day ->

                val selectedDate = LocalDate.of(
                    year,
                    month + 1,
                    day
                )

                TimePickerDialog(
                    context,
                    { _, hour, minute ->

                        val selected = LocalDateTime.of(
                            selectedDate,
                            LocalTime.of(hour, minute)
                        )

                        dateTime = selected

                        text = selected.format(formatter)

                        onValueChange(
                            pregunta.variable,
                            selected
                        )

                    },
                    current.hour,
                    current.minute,
                    true
                ).show()

            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    OutlinedTextField(

        value = text,

        onValueChange = {

            if (mode is DateInputMode.PickerOrManual) {

                text = it
            }
        },

        modifier = Modifier.fillMaxWidth(),

        readOnly = mode != DateInputMode.PickerOrManual,

        placeholder = {
            Text("dd/mm/aaaa hh:mm")
        },

        trailingIcon = {

            IconButton(
                onClick = {
                    openDatePicker()
                }
            ) {

                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null
                )
            }
        },

        shape = RoundedCornerShape(10.dp)
    )
}