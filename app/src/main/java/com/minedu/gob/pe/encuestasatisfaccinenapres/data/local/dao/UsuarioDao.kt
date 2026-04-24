package com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.dao

import androidx.room.*
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuario WHERE usuario = :user LIMIT 1")
    suspend fun getUser(user: String): UsuarioEntity?

    @Query("DELETE FROM usuario")
    suspend fun clear()
}