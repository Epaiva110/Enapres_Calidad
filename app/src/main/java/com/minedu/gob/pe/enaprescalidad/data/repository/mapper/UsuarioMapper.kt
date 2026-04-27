package com.minedu.gob.pe.enaprescalidad.data.repository.mapper

import com.minedu.gob.pe.enaprescalidad.data.domain.Usuario
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.UsuarioDto

// ── Usuario ───────────────────────────────────────────────────────

fun UsuarioDto.toDomain() = Usuario(
    usuario = usuario,
    password = password,
    activo = activo
)

fun UsuarioEntity.toDomain() = Usuario(
    usuario = usuario,
    password = password,
    activo = activo
)

fun UsuarioDto.toEntity() = UsuarioEntity(
    usuario = usuario,
    password = password,
    activo = activo,
    lastUpdated = System.currentTimeMillis()
)