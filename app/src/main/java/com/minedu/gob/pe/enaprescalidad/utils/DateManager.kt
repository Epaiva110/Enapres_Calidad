package com.minedu.gob.pe.enaprescalidad.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss"

fun formatDate(timestamp: Long): String {

    val formatter = DateTimeFormatter.ofPattern(DEFAULT_PATTERN)

    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(formatter)
}

//fun parseDate(date: String): Long {
//
//    val formatter = DateTimeFormatter.ofPattern(DEFAULT_PATTERN)
//
//    return LocalDateTime.parse(date, formatter)
//        .atZone(ZoneId.systemDefault())
//        .toInstant()
//        .toEpochMilli()
//}

fun parseDate(date: String): Long {
    return LocalDateTime.parse(date)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}