package com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.dao

import androidx.room.*
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.MuestraEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuario ORDER BY usuario ASC")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>

    //@Query("SELECT * FROM usuario WHERE id = :id")
    //suspend fun getUsuarioById(id: Int): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usuarios: List<UsuarioEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity)

    @Update
    suspend fun update(usuario: UsuarioEntity)

    @Delete
    suspend fun delete(usuario: UsuarioEntity)

    @Query("DELETE FROM usuario")
    suspend fun deleteAll()
}

@Dao
interface MuestraDao {
    @Query("SELECT * FROM muestra WHERE usuario = :usuarioId ORDER BY idcong ASC")
    fun getMuestrasByUsuario(usuarioId: String): Flow<List<MuestraEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(muestras: List<MuestraEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(muestra: MuestraEntity)

    @Delete
    suspend fun delete(muestra: MuestraEntity)

    @Query("DELETE FROM muestra WHERE usuario = :usuarioId")
    suspend fun deleteByUsuario(usuarioId: String)
}
