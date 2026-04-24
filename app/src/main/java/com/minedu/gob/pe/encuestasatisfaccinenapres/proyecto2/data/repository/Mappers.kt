package com.example.userapp.data.repository

import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.MuestraEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto.MuestraDto
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto.UsuarioDto

// ── Usuario ───────────────────────────────────────────────────────
fun UsuarioDto.toEntity() = UsuarioEntity(usuario = usuario, password = password, estado = estado)
fun UsuarioEntity.toDto() = UsuarioDto(usuario = usuario, password = password, estado = estado)

// ── Muestra ───────────────────────────────────────────────────────
fun MuestraDto.toEntity() = MuestraEntity(idcong = idcong, usuario = usuario)
fun MuestraEntity.toDto() = MuestraDto(idcong = idcong, usuario = usuario)
