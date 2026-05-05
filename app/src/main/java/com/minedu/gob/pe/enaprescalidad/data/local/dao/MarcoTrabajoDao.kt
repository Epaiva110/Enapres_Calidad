package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarcoTrabajoDao {
    @Query("SELECT * FROM MarcoTrabajo WHERE usuario = :user ORDER BY anio, periodo")
    fun getMarcoTrabajo(user: String): Flow<List<MarcoTrabajoEntity>>

    @Query("SELECT * FROM MarcoTrabajo WHERE tipo = :tipo and usuario = :user ORDER BY anio, periodo")
    fun getMarcoTrabajoTipo(tipo: String, user: String): Flow<List<MarcoTrabajoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MarcoTrabajoEntity>)

    @Query("DELETE FROM MarcoTrabajo WHERE usuario = :user")
    suspend fun deleteByUsuario(user: String)

    @Query("DELETE FROM MarcoTrabajo WHERE usuario = :usuario AND tipo = :tipo")
    suspend fun deleteByUsuarioAndTipo(usuario: String, tipo: String)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(items: List<MarcoTrabajoEntity>)
//
//    @Query("DELETE FROM MarcoTrabajo WHERE tipo = :tipo")
//    suspend fun deleteByTipo(tipo: String)
}