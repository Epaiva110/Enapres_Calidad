package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity

@Dao
interface MuestraConglomeradoDao {
    @Query("SELECT * FROM muestraConglomerado WHERE usuario = :user ORDER BY anioSup, mesSup, perSup")
    suspend fun getMuestraUsuario(user: String): List<MuestraConglomeradoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MuestraConglomeradoEntity>)

    @Query("DELETE FROM muestraConglomerado WHERE usuario = :usuario")
    suspend fun deleteByUsuario(usuario: String)
}

//interface MuestraViviendaDao {
//    @Query("SELECT * FROM muestraConglomerado WHERE usuario = :user ORDER BY anioSup, mesSup, perSup")
//    suspend fun getMuestraUsuario(user: String): List<MuestraViviendaEntity>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(data: List<MuestraViviendaEntity>)
//
//    @Query("DELETE FROM muestraConglomerado WHERE usuario = :usuario")
//    suspend fun deleteByUsuario(usuario: String)
//}
//
//interface MuestraReentrevistaDao {
//    @Query("SELECT * FROM muestraConglomerado WHERE usuario = :user ORDER BY anioSup, mesSup, perSup")
//    suspend fun getMuestraUsuario(user: String): List<MuestraViviendaEntity>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(data: List<MuestraViviendaEntity>)
//
//    @Query("DELETE FROM muestraConglomerado WHERE usuario = :usuario")
//    suspend fun deleteByUsuario(usuario: String)
//}