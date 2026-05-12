package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.*
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UsuarioEntity)

    @Query("SELECT * FROM usuario WHERE user = :user LIMIT 1")
    suspend fun getUser(user: String): UsuarioEntity?

    @Query("DELETE FROM usuario")
    suspend fun clear()
}