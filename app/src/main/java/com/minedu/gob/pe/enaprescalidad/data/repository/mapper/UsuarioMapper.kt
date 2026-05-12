package com.minedu.gob.pe.enaprescalidad.data.repository.mapper

import com.minedu.gob.pe.enaprescalidad.data.domain.Usuario
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.UsuarioDto


// ── Usuario ───────────────────────────────────────────────────────



fun UsuarioDto.toDomain() = Usuario(
    id = id,
    user = user,
    password = password,
    user_name = user_name,
    role = role,
    active = active,
    last_connection = last_connection
)

fun UsuarioEntity.toDomain() = Usuario(
    id = id,
    user = user,
    password = password,
    user_name = user_name,
    role = role,
    active = active,
    last_connection = last_connection
)

fun UsuarioDto.toEntity() = UsuarioEntity(
    id = id,
    user = user,
    password = password,
    user_name = user_name,
    role = role,
    active = active,
    last_connection = System.currentTimeMillis()
)