package com.minedu.gob.pe.enaprescalidad.data.repository.mapper

import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.MuestraConglomeradoDto

// ── Muestra ───────────────────────────────────────────────────────
fun MuestraConglomeradoDto.toEntity() = MuestraConglomeradoEntity(idcongsup = idcongsup,anioSup = anioSup,mesSup = mesSup,perSup = perSup, anio = anio,
    mes = mes,conglomerado = conglomerado,departamento = departamento,provincia = provincia,distrito = distrito,odeiEnapres = odeiEnapres,
    usuario = usuario,fechaCreacion = fechaCreacion,fechaEnvio = fechaEnvio,enviado = enviado,cerrado = cerrado,tipoMuestra = tipoMuestra)

fun MuestraConglomeradoEntity.toDto() = MuestraConglomeradoDto (idcongsup = idcongsup,anioSup = anioSup,mesSup = mesSup,perSup = perSup, anio = anio,
    mes = mes,conglomerado = conglomerado,departamento = departamento,provincia = provincia,distrito = distrito,odeiEnapres = odeiEnapres,
    usuario = usuario,fechaCreacion = fechaCreacion,fechaEnvio = fechaEnvio,enviado = enviado,cerrado = cerrado,tipoMuestra = tipoMuestra)
