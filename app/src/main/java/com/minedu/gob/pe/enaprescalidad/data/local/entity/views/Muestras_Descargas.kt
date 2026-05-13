package com.minedu.gob.pe.enaprescalidad.data.local.entity.views

//import androidx.room.DatabaseView
//
//@DatabaseView("""
//    SELECT
//        mt.id,
//        CASE
//            WHEN mt.tipo = 'Conglomerado' THEN (
//                SELECT COUNT(*)
//                FROM Muestra_Conglomerado mc
//                WHERE mc.id_mt = mt.id
//            )
//
//            WHEN mt.tipo = 'Vivienda' THEN (
//                SELECT COUNT(*)
//                FROM Muestra_Vivienda mv
//                WHERE mv.id_mt = mt.id
//            )
//            WHEN mt.tipo = 'Reentrevista' THEN (
//                SELECT COUNT(*)
//                FROM Muestra_Reentrevista mr
//                WHERE mr.id_mt = mt.id
//            )
//
//            ELSE 0
//        END AS descargas
//
//    FROM MarcoTrabajo mt
//""")
//data class DescargasMarcoTrabajoView(
//    val id: Int,
//    val descargas: Int
//)