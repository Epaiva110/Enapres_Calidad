package com.minedu.gob.pe.enaprescalidad.data.local.database.datasource

import com.minedu.gob.pe.enaprescalidad.data.local.dao.UsuarioDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import jakarta.inject.Inject

class UsuarioLocalDataSource @Inject constructor(
    private val dao: UsuarioDao
) {

    suspend fun save(user: UsuarioEntity) {
        dao.insert(user)
    }

    suspend fun get(user: String): UsuarioEntity? {
        return dao.getUser(user)
    }

    suspend fun logout() {
        dao.clear()
    }
}