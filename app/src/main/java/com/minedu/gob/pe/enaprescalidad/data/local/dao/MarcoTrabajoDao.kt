package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarcoTrabajoDao {
    @Query("SELECT * FROM MarcoTrabajo WHERE user = :user ORDER BY anio, periodo")
    fun getMarcoTrabajo(user: String): Flow<List<MarcoTrabajoEntity>>

    @Query("SELECT * FROM MarcoTrabajo WHERE tipo = :tipo and user = :user ORDER BY anio, periodo")
    fun getMarcoTrabajoTipo(tipo: String, user: String): Flow<List<MarcoTrabajoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MarcoTrabajoEntity>)

    @Query("DELETE FROM MarcoTrabajo WHERE user = :user")
    suspend fun deleteByUsuario(user: String)

    @Query("DELETE FROM MarcoTrabajo WHERE user = :user AND tipo = :tipo")
    suspend fun deleteByUsuarioAndTipo(user: String, tipo: String)

    @Query("""
    UPDATE MarcoTrabajo
    SET 
        descargas = (
            SELECT COUNT(*)
            FROM Muestra_Conglomerado
            WHERE id_mt = :id 
              AND user = :user
        ),
        sincronizado = :sincronizado,
        fecha_sincronizacion = :fecha_sincronizacion
    WHERE id = :id 
      AND user = :user
""")
    suspend fun updateMarcoTrabajo(
        id: Int,
        user: String,
        sincronizado: Boolean,
        fecha_sincronizacion: String
    )

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(items: List<MarcoTrabajoEntity>)
//
//    @Query("DELETE FROM MarcoTrabajo WHERE tipo = :tipo")
//    suspend fun deleteByTipo(tipo: String)
}