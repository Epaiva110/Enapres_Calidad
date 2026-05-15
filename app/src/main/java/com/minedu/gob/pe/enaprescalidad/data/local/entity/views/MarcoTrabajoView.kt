package com.minedu.gob.pe.enaprescalidad.data.local.entity.views

import androidx.room.DatabaseView
import kotlinx.serialization.Serializable

@DatabaseView("""
    SELECT
        mt.*,
        CASE
            WHEN mt.tipo = 'Conglomerado' THEN (
                SELECT COUNT(*)
                FROM Muestra_Conglomerado mc
                WHERE mc.id_mt = mt.id
            )

            WHEN mt.tipo = 'Vivienda' THEN (
                SELECT COUNT(*)
                FROM Muestra_Vivienda mv
                WHERE mv.id_mt = mt.id
            )
            WHEN mt.tipo = 'Reentrevista' THEN (
                SELECT COUNT(*)
                FROM Muestra_Reentrevista mr
                WHERE mr.id_mt = mt.id
            )
            ELSE 0
        END AS descargas,
        
        CASE
            WHEN mt.tipo = 'Conglomerado' THEN (
                SELECT CASE WHEN COUNT(*)>0 THEN 1 ELSE 0 END
                FROM Muestra_Conglomerado mc
                WHERE mc.id_mt = mt.id
            )

            WHEN mt.tipo = 'Vivienda' THEN (
                SELECT CASE WHEN COUNT(*)>0 THEN 1 ELSE 0 END
                FROM Muestra_Vivienda mv
                WHERE mv.id_mt = mt.id
            )
            WHEN mt.tipo = 'Reentrevista' THEN (
                SELECT CASE WHEN COUNT(*)>0 THEN 1 ELSE 0 END
                FROM Muestra_Reentrevista mr
                WHERE mr.id_mt = mt.id
            )
            ELSE 0
        END AS sincronizado

    FROM MarcoTrabajo mt
""")
@Serializable
data class MarcoTrabajoView(
    val id: Int,
    val user: String,
    val orden: Int,
    val tipo: String,
    val fecha_programacion: String,
    val anio: Int,
    val mes: Int,
    val periodo: Int,
    val meta: Int,
    val descargas: Int,
    val sincronizado: Boolean,
    val fecha_sincronizacion: Long?,
    val proyecto: Int
)