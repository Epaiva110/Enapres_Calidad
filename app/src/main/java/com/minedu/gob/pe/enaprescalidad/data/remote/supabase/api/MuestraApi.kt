package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.api

import com.minedu.gob.pe.enaprescalidad.data.domain.MuestraReentrevista
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraReentrevistaDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraViviendaDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import jakarta.inject.Inject

class MuestraApi @Inject constructor(
    private val client: SupabaseClient
) {
    private val postgrest = client.postgrest

    suspend fun marcarSincronizadoC(id: Int, fecha: String) {
        postgrest.from("Muestra_Conglomerado")
            .update(
                mapOf(
                    "sincronizado" to true,
                    "fecha_sincronizacion" to fecha,
                )
            ) {
                filter { eq("id", id) }
            }
    }

    /*
    Obteer muestras por listas
    */

    suspend fun getMuestraCL(
        idmt: List<Int>
    ): List<MuestraConglomeradoDto> {

        return postgrest
            .from("Muestra_Conglomerado")
            .select {
                filter {
                    isIn("id_mt", idmt)
                }
            }
            .decodeList()
    }

    suspend fun getMuestraVL(
        idmt: List<Int>
    ): List<MuestraViviendaDto> {

        return postgrest
            .from("Muestra_Vivienda")
            .select {
                filter {
                    isIn("id_mt", idmt)
                }
            }
            .decodeList()
    }

    suspend fun getMuestraRL(
        idmt: List<Int>
    ): List<MuestraReentrevistaDto> {

        return postgrest
            .from("Muestra_Reentrevista")
            .select {
                filter {
                    isIn("id_mt", idmt)
                }
            }
            .decodeList()
    }

    /*
    Obteer muestras por valor unico
    */

    suspend fun getMuestraC(idmt: Int): List<MuestraConglomeradoDto> {
        return postgrest.from("Muestra_Conglomerado")
            .select {
                filter {
                    eq("id_mt", idmt)
                }
            }
            .decodeList()
    }

    suspend fun getMuestraV(idmt: Int): List<MuestraViviendaDto> {
        return postgrest.from("Muestra_Vivienda")
            .select {
                filter {
                    eq("id_mt", idmt)
                }
            }
            .decodeList()
    }

    suspend fun getMuestraR(idmt: Int): List<MuestraReentrevistaDto> {
        return postgrest.from("Muestra_Reentrevista")
            .select {
                filter {
                    eq("id_mt", idmt)
                }
            }
            .decodeList()
    }
}