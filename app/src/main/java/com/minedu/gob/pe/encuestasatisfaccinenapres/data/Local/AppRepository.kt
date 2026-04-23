package com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local

import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Dao.TaskDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Dao.UsuarioDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.Task
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.UsuarioRoom
import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: UsuarioDao) {

    suspend fun save(usuario: UsuarioRoom) {
        dao.insert(usuario)
    }

    suspend fun get(user: String): UsuarioRoom? {
        return dao.getUser(user)
    }

    suspend fun logout() {
        dao.clear()
    }
}
