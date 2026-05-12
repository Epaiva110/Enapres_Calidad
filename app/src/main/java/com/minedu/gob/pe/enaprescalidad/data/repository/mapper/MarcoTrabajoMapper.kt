package com.minedu.gob.pe.enaprescalidad.data.repository.mapper

import com.minedu.gob.pe.enaprescalidad.data.domain.MarcoTrabajo
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MarcoTrabajoDto

fun MarcoTrabajoDto.toEntity() = MarcoTrabajoEntity(id = id,user = user,orden = orden,tipo = tipo,fecha_programacion = fecha_programacion,anio = anio,mes = mes,periodo = periodo,
    meta = meta,descargas = descargas,sincronizado = sincronizado,fecha_sincronizacion = fecha_sincronizacion,proyecto = proyecto)

fun MarcoTrabajoEntity.toDto() = MarcoTrabajoDto (id = id,user = user,orden = orden,tipo = tipo,fecha_programacion = fecha_programacion,anio = anio,mes = mes,periodo = periodo,
    meta = meta,descargas = descargas,sincronizado = sincronizado,fecha_sincronizacion = fecha_sincronizacion,proyecto = proyecto)
fun MarcoTrabajoDto.toDomain() = MarcoTrabajo(id = id,user = user,orden = orden,tipo = tipo,fecha_programacion = fecha_programacion,anio = anio,mes = mes,periodo = periodo,
    meta = meta,descargas = descargas,sincronizado = sincronizado,fecha_sincronizacion = fecha_sincronizacion,proyecto = proyecto)
fun MarcoTrabajoEntity.toDomain() = MarcoTrabajo(id = id,user = user,orden = orden,tipo = tipo,fecha_programacion = fecha_programacion,anio = anio,mes = mes,periodo = periodo,
    meta = meta,descargas = descargas,sincronizado = sincronizado,fecha_sincronizacion = fecha_sincronizacion,proyecto = proyecto)