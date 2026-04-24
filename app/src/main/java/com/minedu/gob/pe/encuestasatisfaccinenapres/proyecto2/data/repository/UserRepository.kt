package com.example.userapp.data.repository

import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.dao.MuestraDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.dao.UsuarioDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.MuestraEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.api.SupabaseApi
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto.MuestraDto
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.remote.dto.UsuarioDto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val muestraDao: MuestraDao,
    private val api: SupabaseApi
) {
    // ── Local Flows ───────────────────────────────────────────────
    val usuarios: Flow<List<UsuarioEntity>> = usuarioDao.getAllUsuarios()

    fun muestrasByUsuario(usuarioId: Int): Flow<List<MuestraEntity>> =
        muestraDao.getMuestrasByUsuario(usuarioId.toString())

    // ── Sync: Remote → Local ──────────────────────────────────────
    suspend fun syncUsuarios() {
        val remote = api.getUsuarios()
        usuarioDao.deleteAll()
        usuarioDao.insertAll(remote.map { it.toEntity() })
    }

    suspend fun syncMuestras(usuarioId: Int) {
        val remote = api.getMuestrasByUsuario(usuarioId.toString())
        muestraDao.deleteByUsuario(usuarioId.toString())
        muestraDao.insertAll(remote.map { it.toEntity() })
    }

    // ── Usuario CRUD ──────────────────────────────────────────────
    suspend fun insertUsuario(dto: UsuarioDto) {
        api.insertUsuario(dto)
        syncUsuarios()
    }

    suspend fun updateUsuario(entity: UsuarioEntity) {
        api.updateUsuario(entity.toDto())
        usuarioDao.update(entity)
    }

    //suspend fun deleteUsuario(entity: UsuarioEntity) {
    //    api.deleteUsuario(entity.id)
    //    usuarioDao.delete(entity)
    //}

    // ── Muestra CRUD ──────────────────────────────────────────────
    suspend fun insertMuestra(dto: MuestraDto) {
        api.insertMuestra(dto)
        syncMuestras(dto.usuario.toInt())
    }

    suspend fun deleteMuestra(entity: MuestraEntity) {
        api.deleteMuestra(entity.usuario)
        muestraDao.delete(entity)
    }
}
