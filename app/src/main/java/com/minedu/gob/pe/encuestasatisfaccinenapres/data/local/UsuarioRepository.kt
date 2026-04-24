package com.minedu.gob.pe.encuestasatisfaccinenapres.data.local

import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.dao.UsuarioDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.entity.UsuarioEntity
class UsuarioRepository(private val dao: UsuarioDao) {

    suspend fun save(usuario: UsuarioEntity) {
        dao.insert(usuario)
    }

    suspend fun get(user: String): UsuarioEntity? {
        return dao.getUser(user)
    }

    suspend fun logout() {
        dao.clear()
    }
}
