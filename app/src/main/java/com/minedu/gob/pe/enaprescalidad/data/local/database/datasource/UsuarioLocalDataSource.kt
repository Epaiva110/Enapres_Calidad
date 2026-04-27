package com.minedu.gob.pe.enaprescalidad.data.local.database.datasource

import com.minedu.gob.pe.enaprescalidad.data.local.dao.UsuarioDaos
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import jakarta.inject.Inject

class UsuarioLocalDataSource @Inject constructor(
    private val dao: UsuarioDaos
) {

    suspend fun save(user: UsuarioEntity) {
        dao.insert(user)
    }

    suspend fun get(usuario: String): UsuarioEntity? {
        return dao.getUser(usuario)
    }

    suspend fun logout() {
        dao.clear()
    }
}