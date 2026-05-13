package com.minedu.gob.pe.enaprescalidad.data.repository.mapper

import com.minedu.gob.pe.enaprescalidad.data.domain.MuestraConglomerado
import com.minedu.gob.pe.enaprescalidad.data.domain.MuestraReentrevista
import com.minedu.gob.pe.enaprescalidad.data.domain.MuestraVivienda
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraReentrevistaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraViviendaEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraReentrevistaDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraViviendaDto

// ── Muestra Conglomerado ───────────────────────────────────────────────────────
fun MuestraConglomeradoDto.toEntity() = MuestraConglomeradoEntity(id = id, id_mt = id_mt, anio = anio, mes = mes, idcong = idcong, conglomerado = conglomerado,
    departamento = departamento, provincia = provincia, distrito = distrito, odeienapres = odeienapres, sincronizado = sincronizado, cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)

fun MuestraConglomeradoEntity.toDto() = MuestraConglomeradoDto (id = id, id_mt = id_mt, anio = anio, mes = mes, idcong = idcong, conglomerado = conglomerado,
    departamento = departamento, provincia = provincia, distrito = distrito, odeienapres = odeienapres, sincronizado = sincronizado, cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)

fun MuestraConglomeradoDto.toDomain() = MuestraConglomerado(id = id, id_mt = id_mt, anio = anio, mes = mes, idcong = idcong, conglomerado = conglomerado,
    departamento = departamento, provincia = provincia, distrito = distrito, odeienapres = odeienapres, sincronizado = sincronizado, cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)

// ── Muestra Vivienda───────────────────────────────────────────────────────

fun MuestraViviendaDto.toEntity() = MuestraViviendaEntity(id = id, id_mt = id_mt, anio = anio, mes = mes, idviv = idviv, conglomerado = conglomerado,
    departamento = departamento, provincia = provincia, distrito = distrito, odeienapres = odeienapres, sincronizado = sincronizado, cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)

fun MuestraViviendaEntity.toDto() = MuestraViviendaDto(id = id, id_mt = id_mt, anio = anio, mes = mes, idviv = idviv, conglomerado = conglomerado,
    departamento = departamento, provincia = provincia, distrito = distrito, odeienapres = odeienapres, sincronizado = sincronizado, cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)
fun MuestraViviendaDto.toDomain() = MuestraVivienda(id = id, id_mt = id_mt, anio = anio, mes = mes, idviv = idviv, conglomerado = conglomerado,
    departamento = departamento, provincia = provincia, distrito = distrito, odeienapres = odeienapres, sincronizado = sincronizado, cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)

// ── Muestra Reentrevista───────────────────────────────────────────────────────

fun MuestraReentrevistaDto.toEntity() = MuestraReentrevistaEntity(id = id,id_mt = id_mt,anio = anio,mes = mes,idviv = idviv,conglomerado = conglomerado,
    departamento = departamento,provincia = provincia,distrito = distrito,odeienapres = odeienapres,sincronizado = sincronizado,cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)

fun MuestraReentrevistaEntity.toDto() = MuestraReentrevistaDto(id = id, id_mt = id_mt, anio = anio, mes = mes, idviv = idviv, conglomerado = conglomerado,
    departamento = departamento, provincia = provincia, distrito = distrito, odeienapres = odeienapres, sincronizado = sincronizado, cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)
fun MuestraReentrevistaDto.toDomain() = MuestraReentrevista(id = id, id_mt = id_mt, anio = anio, mes = mes, idviv = idviv, conglomerado = conglomerado,
    departamento = departamento, provincia = provincia, distrito = distrito, odeienapres = odeienapres, sincronizado = sincronizado, cerrado = cerrado,
    fecha_sincronizacion = fecha_sincronizacion)

//fun MuestraViviendaDto.toEntity() = MuestraViviendaEntity(
//    idcongsup, anioSup, mesSup, perSup, anio, mes,
//    conglomerado, departamento, provincia, distrito,
//    odeiEnapres, usuario, fechaCreacion, fechaEnvio, enviado, cerrado, tipoMuestra
//)

// Construir resumen por periodo a partir de una lista de muestras
//fun List<MuestraConglomeradoDto>.toCargaEntities(tipo: String): List<CargaTrabajoEntity> {
//    return groupBy { Triple(it.anio, it.mesSup, it.perSup) }
//        .map { (key, items) ->
//            val (anio, mes, periodo) = key
//            val totalMuestras = items.size
//            val totalActualizado = items.count { it.enviado }
//            CargaTrabajoEntity(
//                id = 2,
//                tipo = tipo,
//                fechaProgramacion = items.first().fechaCreacion,
//                anio = anio,
//                mes = mesNombre(mes),
//                periodo = periodo,
//                totalMuestras = totalMuestras,
//                totalActualizado = totalActualizado,
//                actualizado = totalActualizado >= totalMuestras && totalMuestras > 0,
//                fechaActualizacion = items.maxOfOrNull { it.fechaCreacion }
//            )
//        }
//}

private fun mesNombre(mes: Int) = listOf(
    "Enero","Febrero","Marzo","Abril","Mayo","Junio",
    "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
).getOrElse(mes - 1) { "Mes $mes" }