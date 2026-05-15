package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraReentrevistaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraViviendaEntity
import com.minedu.gob.pe.enaprescalidad.data.remote.supabase.dto.ConglomeradoFiltroDto
import kotlinx.coroutines.flow.Flow

@Dao
interface MuestraConglomeradoDao {

    // ══════════════════════════════════════════════════════════════════════════════
//  SNIPPET 1 — Agregar en MuestraConglomeradoDao  (MuestraDao.kt)
//  Las 5 queries de filtro + 1 para marcar sincronizada
// ══════════════════════════════════════════════════════════════════════════════

    // Lista filtrada por los 4 combos (reactiva)
    @Query("""
    SELECT m.*
    FROM Muestra_Conglomerado AS m
    INNER JOIN MarcoTrabajo AS t ON m.id_mt = t.id
    WHERE t.user     = :user
      AND t.anio     = :anio
      AND t.mes      = :mes
      AND t.periodo  = :periodo
      AND t.proyecto = :proyecto
    ORDER BY m.conglomerado
""")
    fun getMuestraFiltrada(user: String, anio: Int, mes: Int, periodo: Int, proyecto: Int): Flow<List<MuestraConglomeradoEntity>>

    @Query("SELECT DISTINCT t.anio FROM MarcoTrabajo t WHERE t.user = :user AND t.tipo = 'Conglomerado' ORDER BY t.anio DESC")
    suspend fun getAniosDisponibles(user: String): List<Int>

    @Query("SELECT DISTINCT t.mes FROM MarcoTrabajo t WHERE t.user = :user AND t.tipo = 'Conglomerado' AND t.anio = :anio ORDER BY t.mes")
    suspend fun getMesesDisponibles(user: String, anio: Int): List<Int>

    @Query("SELECT DISTINCT t.periodo FROM MarcoTrabajo t WHERE t.user = :user AND t.tipo = 'Conglomerado' AND t.anio = :anio AND t.mes = :mes ORDER BY t.periodo")
    suspend fun getPeriodosDisponibles(user: String, anio: Int, mes: Int): List<Int>

    @Query("SELECT DISTINCT t.proyecto FROM MarcoTrabajo t WHERE t.user = :user AND t.tipo = 'Conglomerado' AND t.anio = :anio AND t.mes = :mes AND t.periodo = :periodo ORDER BY t.proyecto")
    suspend fun getProyectosDisponibles(user: String, anio: Int, mes: Int, periodo: Int): List<Int>

    // Marca UNA muestra como sincronizada (se llama tras respuesta OK del servidor)
    @Query("UPDATE Muestra_Conglomerado SET sincronizado = 1, fecha_sincronizacion = :fecha WHERE id = :id")
    suspend fun marcarSincronizada(id: Int, fecha: String)

    //---------------------------------//

    @Query("SELECT m.* FROM Muestra_Conglomerado as m left join MarcoTrabajo as t on m.id_mt = t.id WHERE t.user = :user AND t.id = :idmt ORDER BY t.anio, t.mes, t.periodo")
    suspend fun getMuestraUsuario(idmt: Int, user: String): List<MuestraConglomeradoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MuestraConglomeradoEntity>)

    @Query("DELETE FROM Muestra_Conglomerado WHERE id_mt = :id")
    suspend fun deleteById(id: Int)

    /*Listas*/

    @Query("DELETE FROM Muestra_Conglomerado WHERE id_mt in (:id)")
    suspend fun deleteByIdL(id: List<Int>)
}

@Dao
interface MuestraViviendaDao {
    @Query("SELECT m.* FROM Muestra_Vivienda as m left join MarcoTrabajo as t on m.id_mt = t.id WHERE t.user = :user AND t.id = :idmt ORDER BY t.anio, t.mes, t.periodo")
    suspend fun getMuestraUsuario(idmt: Int, user: String): List<MuestraViviendaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MuestraViviendaEntity>)

    @Query("DELETE FROM Muestra_Vivienda WHERE id_mt = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM Muestra_Vivienda WHERE id_mt in (:id)")
    suspend fun deleteByIdL(id: List<Int>)
}

@Dao
interface MuestraReentrevistaDao {

    @Query("SELECT m.* FROM Muestra_Reentrevista as m left join MarcoTrabajo as t on m.id_mt = t.id WHERE t.user = :user AND t.id = :idmt ORDER BY t.anio, t.mes, t.periodo")
    suspend fun getMuestraUsuario(idmt: Int, user: String): List<MuestraReentrevistaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MuestraReentrevistaEntity>)

    @Query("DELETE FROM Muestra_Reentrevista WHERE id_mt = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM Muestra_Reentrevista WHERE id_mt in (:id)")
    suspend fun deleteByIdL(id: List<Int>)
}