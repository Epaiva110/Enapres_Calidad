package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraReentrevistaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraViviendaEntity

@Dao
interface MuestraConglomeradoDao {
    @Query("SELECT m.* FROM Muestra_Conglomerado as m left join MarcoTrabajo as t on m.id_mt = t.id WHERE t.user = :user ORDER BY t.anio, t.mes, t.periodo")
    suspend fun getMuestraUsuario(user: String): List<MuestraConglomeradoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MuestraConglomeradoEntity>)

    @Query("DELETE FROM Muestra_Conglomerado WHERE id_mt IN (SELECT id FROM MarcoTrabajo WHERE user = :user)")
    suspend fun deleteByUsuario(user: String)
}

@Dao
interface MuestraViviendaDao {
    @Query("SELECT m.* FROM Muestra_Vivienda as m left join MarcoTrabajo as t on m.id_mt = t.id WHERE t.user = :user ORDER BY t.anio, t.mes, t.periodo")
    suspend fun getMuestraUsuario(user: String): List<MuestraViviendaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MuestraViviendaEntity>)

    @Query("DELETE FROM Muestra_Vivienda WHERE id_mt IN (SELECT id FROM MarcoTrabajo WHERE user = :user)")
    suspend fun deleteByUsuario(user: String)
}

@Dao
interface MuestraReentrevistaDao {

    @Query("SELECT m.* FROM Muestra_Reentrevista as m left join MarcoTrabajo as t on m.id_mt = t.id WHERE t.user = :user ORDER BY t.anio, t.mes, t.periodo")
    suspend fun getMuestraUsuario(user: String): List<MuestraReentrevistaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MuestraReentrevistaEntity>)

    @Query("DELETE FROM Muestra_Reentrevista WHERE id_mt IN (SELECT id FROM MarcoTrabajo WHERE user = :user)")
    suspend fun deleteByUsuario(user: String)
}