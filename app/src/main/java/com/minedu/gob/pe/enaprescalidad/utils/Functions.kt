package com.minedu.gob.pe.enaprescalidad.utils

fun obtenerNombreMes(mes: Int): String {
    return when(mes) {
        1 -> "Enero"
        2 -> "Febrero"
        3 -> "Marzo"
        4 -> "Abril"
        5 -> "Mayo"
        6 -> "Junio"
        7 -> "Julio"
        8 -> "Agosto"
        9 -> "Septiembre"
        10 -> "Octubre"
        11 -> "Noviembre"
        12 -> "Diciembre"
        else -> "Error mes no encontrado"
    }
}

fun obtenerNombreMesLim(mes: Int): String {
    return when(mes) {
        1 -> "Ene"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Abr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Ago"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dic"
        else -> "Error"
    }
}

fun obtenerNombreProyecto(mes: Int): String {
    return when(mes) {
        1 -> "ENAPRES"
        2 -> "Seguridad"
        else -> "Proyecto no encontrado"
    }
}