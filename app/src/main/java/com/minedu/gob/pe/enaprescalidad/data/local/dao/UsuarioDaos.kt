package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.*
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDaos {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuario WHERE usuario = :user LIMIT 1")
    suspend fun getUser(user: String): UsuarioEntity?

    @Query("DELETE FROM usuario")
    suspend fun clear()
}