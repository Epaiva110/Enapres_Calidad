package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minedu.gob.pe.enaprescalidad.data.local.entity.CargaTrabajoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CargaTrabajoDao {
    @Query("SELECT * FROM carga_trabajo WHERE tipo = :tipo and usuarii = :usuario ORDER BY anio, periodo")
    fun observeByTipo(tipo: String, usuario: String): Flow<List<CargaTrabajoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CargaTrabajoEntity>)

    @Query("DELETE FROM carga_trabajo WHERE tipo = :tipo")
    suspend fun deleteByTipo(tipo: String)
}