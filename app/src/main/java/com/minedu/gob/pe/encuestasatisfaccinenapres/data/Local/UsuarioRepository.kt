package com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local

import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Dao.UsuarioDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.UsuarioEntity
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
