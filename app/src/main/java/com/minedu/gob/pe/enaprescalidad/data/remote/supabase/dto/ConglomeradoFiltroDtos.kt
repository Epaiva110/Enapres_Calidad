package com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto

import kotlinx.serialization.Serializable

@Serializable
data class ConglomeradoFiltroDto(
    val anio: Int?,
    val mes: Int?,
    val periodo: Int?,
    val proyecto: Int?
)