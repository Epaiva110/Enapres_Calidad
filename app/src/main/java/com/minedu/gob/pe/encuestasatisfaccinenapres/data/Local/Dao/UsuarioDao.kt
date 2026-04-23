package com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Dao

import androidx.room.*
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.Task
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.UsuarioRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioRoom)

    @Query("SELECT * FROM usuario WHERE usuario = :user LIMIT 1")
    suspend fun getUser(user: String): UsuarioRoom?

    @Query("DELETE FROM usuario")
    suspend fun clear()
}