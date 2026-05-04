package com.minedu.gob.pe.enaprescalidad.data.repository.mapper

import com.minedu.gob.pe.enaprescalidad.data.domain.MuestraConglomerado
import com.minedu.gob.pe.enaprescalidad.data.domain.Usuario
import com.minedu.gob.pe.enaprescalidad.data.local.entity.CargaTrabajoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraViviendaEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraViviendaDto
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.UsuarioDto

// ── Muestra ───────────────────────────────────────────────────────
fun MuestraConglomeradoDto.toEntity() = MuestraConglomeradoEntity(idcongsup = idcongsup,anioSup = anioSup,mesSup = mesSup,perSup = perSup, anio = anio,
    mes = mes,conglomerado = conglomerado,departamento = departamento,provincia = provincia,distrito = distrito,odeiEnapres = odeiEnapres,
    usuario = usuario,fechaCreacion = fechaCreacion,fechaEnvio = fechaEnvio,enviado = enviado,cerrado = cerrado,tipoMuestra = tipoMuestra)

fun MuestraConglomeradoEntity.toDto() = MuestraConglomeradoDto (idcongsup = idcongsup,anioSup = anioSup,mesSup = mesSup,perSup = perSup, anio = anio,
    mes = mes,conglomerado = conglomerado,departamento = departamento,provincia = provincia,distrito = distrito,odeiEnapres = odeiEnapres,
    usuario = usuario,fechaCreacion = fechaCreacion,fechaEnvio = fechaEnvio,enviado = enviado,cerrado = cerrado,tipoMuestra = tipoMuestra)

fun MuestraConglomeradoDto.toDomain() = MuestraConglomerado(idcongsup = idcongsup,anioSup = anioSup,mesSup = mesSup,perSup = perSup, anio = anio,
    mes = mes,conglomerado = conglomerado,departamento = departamento,provincia = provincia,distrito = distrito,odeiEnapres = odeiEnapres,
    usuario = usuario,fechaCreacion = fechaCreacion,fechaEnvio = fechaEnvio,enviado = enviado,cerrado = cerrado,tipoMuestra = tipoMuestra)

fun MuestraViviendaDto.toEntity() = MuestraViviendaEntity(
    idcongsup, anioSup, mesSup, perSup, anio, mes,
    conglomerado, departamento, provincia, distrito,
    odeiEnapres, usuario, fechaCreacion, fechaEnvio, enviado, cerrado, tipoMuestra
)

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