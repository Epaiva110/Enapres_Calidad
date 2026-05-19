package com.minedu.gob.pe.enaprescalidad.utils

import android.content.Context
import java.io.IOException

fun Context.readJsonFromAssets(fileName: String): String? {
    return try {
        // Abre el archivo desde la carpeta assets y lo lee completo como texto
        assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (ioException: IOException) {
        ioException.printStackTrace()
        null
    }
}