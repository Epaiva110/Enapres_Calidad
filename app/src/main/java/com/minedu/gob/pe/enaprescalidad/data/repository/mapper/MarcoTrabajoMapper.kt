package com.minedu.gob.pe.enaprescalidad.data.repository.mapper

import com.minedu.gob.pe.enaprescalidad.data.domain.MarcoTrabajo
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MarcoTrabajoDto

fun MarcoTrabajoDto.toEntity() = MarcoTrabajoEntity(id = id,usuario = usuario,orden = orden,tipo = tipo,fechaProgramacion = fechaProgramacion,anio = anio,mes = mes,periodo = periodo,
    totalMuestra = totalMuestra,totalActualizado = totalActualizado,actualizado = actualizado,fechaActualizacion = fechaActualizacion,tipoMuestra = tipoMuestra)

fun MarcoTrabajoEntity.toDto() = MarcoTrabajoDto (id = id,usuario = usuario,orden = orden,tipo = tipo,fechaProgramacion = fechaProgramacion,anio = anio,mes = mes,periodo = periodo,
    totalMuestra = totalMuestra,totalActualizado = totalActualizado,actualizado = actualizado,fechaActualizacion = fechaActualizacion,tipoMuestra = tipoMuestra)
fun MarcoTrabajoDto.toDomain() = MarcoTrabajo(id = id,usuario = usuario,orden = orden,tipo = tipo,fechaProgramacion = fechaProgramacion,anio = anio,mes = mes,periodo = periodo,
    totalMuestra = totalMuestra,totalActualizado = totalActualizado,actualizado = actualizado,fechaActualizacion = fechaActualizacion,tipoMuestra = tipoMuestra)


fun MarcoTrabajoEntity.toDomain() = MarcoTrabajo (id = id,usuario = usuario,orden = orden,tipo = tipo,fechaProgramacion = fechaProgramacion,anio = anio,mes = mes,periodo = periodo,
    totalMuestra = totalMuestra,totalActualizado = totalActualizado,actualizado = actualizado,fechaActualizacion = fechaActualizacion,tipoMuestra = tipoMuestra)
