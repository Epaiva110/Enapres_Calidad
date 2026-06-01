package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.*
import com.minedu.gob.pe.enaprescalidad.data.local.entity.HogarEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.PersonaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SurveyResponseEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SurveyVersionEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.VisitaConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.VisitaHogarEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.ViviendaEntity
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY VERSION DAO  (actualizado con contextKey)
// ─────────────────────────────────────────────────────────────────────────────

@Dao
interface SurveyVersionDao {

    @Query("""
        SELECT * FROM survey_versions 
        WHERE surveyType = :surveyType AND contextKey = :contextKey
        LIMIT 1
    """)
    suspend fun obtenerSnapshot(surveyType: String, contextKey: String): SurveyVersionEntity?

    /** INSERT IGNORE: si ya existe el snapshot no lo sobreescribe nunca */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun guardarSnapshot(snapshot: SurveyVersionEntity)

    @Query("""
        SELECT surveyType, version, COUNT(*) as total 
        FROM survey_versions 
        GROUP BY surveyType, version
    """)
    suspend fun auditarVersiones(): List<VersionAuditRow>
}

data class VersionAuditRow(
    val surveyType: String,
    val version: String,
    val total: Int,
)

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY RESPONSE DAO  (actualizado con contextKey)
// ─────────────────────────────────────────────────────────────────────────────

@Dao
interface SurveyResponseDao {

    // ── Lectura ──────────────────────────────────────────────────────────────

    @Query("""
        SELECT * FROM survey_responses 
        WHERE surveyType = :surveyType AND contextKey = :contextKey
    """)
    suspend fun obtenerRespuestas(
        surveyType: String,
        contextKey: String,
    ): List<SurveyResponseEntity>

    @Query("""
        SELECT * FROM survey_responses 
        WHERE surveyType = :surveyType AND contextKey = :contextKey
    """)
    fun observarRespuestas(
        surveyType: String,
        contextKey: String,
    ): Flow<List<SurveyResponseEntity>>

    // ── Escritura — upsert puntual (tiempo real) ─────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRespuesta(respuesta: SurveyResponseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRespuestas(respuestas: List<SurveyResponseEntity>)

    // ── Borrado — purga de variables huérfanas ───────────────────────────────

    @Query("""
        DELETE FROM survey_responses 
        WHERE surveyType = :surveyType AND contextKey = :contextKey AND variable = :variable
    """)
    suspend fun borrarVariable(surveyType: String, contextKey: String, variable: String)

    @Query("""
        DELETE FROM survey_responses 
        WHERE surveyType = :surveyType AND contextKey = :contextKey AND variable IN (:variables)
    """)
    suspend fun borrarVariables(
        surveyType: String,
        contextKey: String,
        variables: List<String>,
    )

    @Query("""
        DELETE FROM survey_responses 
        WHERE surveyType = :surveyType AND contextKey = :contextKey
    """)
    suspend fun borrarTodo(surveyType: String, contextKey: String)

    // ── Consultas cruzadas (útiles para progreso en pantallas de lista) ──────

    /**
     * Cuenta variables respondidas para un contexto.
     * Útil para mostrar progreso en la lista de hogares/personas/visitas.
     */
    @Query("""
        SELECT COUNT(*) FROM survey_responses 
        WHERE surveyType = :surveyType AND contextKey = :contextKey
    """)
    suspend fun contarRespuestas(surveyType: String, contextKey: String): Int

    /**
     * Verifica si la encuesta de un contexto está completada.
     * Busca la variable __survey_completed__ = "true".
     */
    @Query("""
        SELECT COUNT(*) FROM survey_responses 
        WHERE surveyType = :surveyType 
          AND contextKey = :contextKey 
          AND variable = '__survey_completed__' 
          AND valor = 'true'
    """)
    suspend fun estaCompletada(surveyType: String, contextKey: String): Int

    /**
     * Obtiene el estado de múltiples contextos a la vez.
     * Útil para cargar el progreso de todos los hogares de una vivienda
     * en una sola query en lugar de N queries individuales.
     */
    @Query("""
        SELECT contextKey, 
               COUNT(*) as totalVariables,
               SUM(CASE WHEN variable = '__survey_completed__' AND valor = 'true' THEN 1 ELSE 0 END) as completada
        FROM survey_responses
        WHERE surveyType = :surveyType AND contextKey IN (:contextKeys)
        GROUP BY contextKey
    """)
    suspend fun obtenerProgresoMultiple(
        surveyType: String,
        contextKeys: List<String>,
    ): List<ContextProgressRow>
}

data class ContextProgressRow(
    val contextKey: String,
    val totalVariables: Int,
    val completada: Int,  // 1 si completada, 0 si no
) {
    val estaCompletada: Boolean get() = completada > 0
}

// ═════════════════════════════════════════════════════════════════════════════
//  DAOs DE JERARQUÍA
// ═════════════════════════════════════════════════════════════════════════════

@Dao
interface ViviendaDao {
    @Query("SELECT * FROM viviendas WHERE muestraConglomeradoId = :id ORDER BY numeroOrden")
    fun observar(id: Int): Flow<List<ViviendaEntity>>

    @Query("SELECT * FROM viviendas WHERE id = :id")
    suspend fun porId(id: Int): ViviendaEntity?

    @Query("SELECT COUNT(*) FROM viviendas WHERE muestraConglomeradoId = :id")
    suspend fun contar(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(v: ViviendaEntity): Long

    @Update
    suspend fun actualizar(v: ViviendaEntity)

    @Query("UPDATE viviendas SET estadoEncuesta=:estado, actualizadoEn=:ts WHERE id=:id")
    suspend fun actualizarEstado(id: Int, estado: String, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM viviendas WHERE id = :id")
    suspend fun eliminar(id: Int)
}

@Dao
interface HogarDao {
    @Query("SELECT * FROM hogares WHERE viviendaId = :id ORDER BY numeroOrden")
    fun observar(id: Int): Flow<List<HogarEntity>>

    @Query("SELECT * FROM hogares WHERE id = :id")
    suspend fun porId(id: Int): HogarEntity?

    @Query("SELECT COUNT(*) FROM hogares WHERE viviendaId = :id")
    suspend fun contar(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(h: HogarEntity): Long

    @Update
    suspend fun actualizar(h: HogarEntity)

    @Query("UPDATE hogares SET nombreJefeHogar=:nombre, actualizadoEn=:ts WHERE id=:id")
    suspend fun actualizarJefe(id: Int, nombre: String, ts: Long = System.currentTimeMillis())

    @Query("UPDATE hogares SET estadoEncuesta=:estado, actualizadoEn=:ts WHERE id=:id")
    suspend fun actualizarEstado(id: Int, estado: String, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM hogares WHERE id = :id")
    suspend fun eliminar(id: Int)
}

@Dao
interface PersonaDao {
    @Query("SELECT * FROM personas WHERE hogarId = :id ORDER BY numeroOrden")
    fun observar(id: Int): Flow<List<PersonaEntity>>

    @Query("SELECT * FROM personas WHERE id = :id")
    suspend fun porId(id: Int): PersonaEntity?

    @Query("SELECT * FROM personas WHERE hogarId = :id AND numeroOrden = 1 LIMIT 1")
    suspend fun jefeHogar(id: Int): PersonaEntity?

    @Query("SELECT COUNT(*) FROM personas WHERE hogarId = :id")
    suspend fun contar(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(p: PersonaEntity): Long

    @Update
    suspend fun actualizar(p: PersonaEntity)

    @Query("UPDATE personas SET estadoEncuesta=:estado, actualizadoEn=:ts WHERE id=:id")
    suspend fun actualizarEstado(id: Int, estado: String, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM personas WHERE id = :id")
    suspend fun eliminar(id: Int)
}

@Dao
interface VisitaConglomeradoDao {
    @Query("SELECT * FROM visitas_conglomerado WHERE muestraConglomeradoId=:id ORDER BY numeroVisita")
    fun observar(id: Int): Flow<List<VisitaConglomeradoEntity>>

    @Query("SELECT COUNT(*) FROM visitas_conglomerado WHERE muestraConglomeradoId=:id")
    suspend fun contar(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(v: VisitaConglomeradoEntity): Long

    @Update
    suspend fun actualizar(v: VisitaConglomeradoEntity)

    @Query("DELETE FROM visitas_conglomerado WHERE id=:id")
    suspend fun eliminar(id: Int)
}

@Dao
interface VisitaHogarDao {
    @Query("SELECT * FROM visitas_hogar WHERE hogarId=:id ORDER BY numeroVisita")
    fun observar(id: Int): Flow<List<VisitaHogarEntity>>

    @Query("SELECT COUNT(*) FROM visitas_hogar WHERE hogarId=:id")
    suspend fun contar(id: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(v: VisitaHogarEntity): Long

    @Update
    suspend fun actualizar(v: VisitaHogarEntity)

    @Query("DELETE FROM visitas_hogar WHERE id=:id")
    suspend fun eliminar(id: Int)
}

// ═════════════════════════════════════════════════════════════════════════════
//  DAOs DE ENCUESTA (versionado + respuestas)
// ═════════════════════════════════════════════════════════════════════════════

//@Dao
//interface SurveyResponseDao {
//
//    @Query("SELECT * FROM survey_responses WHERE surveyType=:type AND contextKey=:key")
//    suspend fun obtenerRespuestas(type: String, key: String): List<SurveyResponseEntity>
//
//    @Query("SELECT * FROM survey_responses WHERE surveyType=:type AND contextKey=:key")
//    fun observarRespuestas(type: String, key: String): Flow<List<SurveyResponseEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun upsertRespuesta(r: SurveyResponseEntity)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun upsertRespuestas(rs: List<SurveyResponseEntity>)
//
//    @Query("DELETE FROM survey_responses WHERE surveyType=:type AND contextKey=:key AND variable=:variable")
//    suspend fun borrarVariable(type: String, key: String, variable: String)
//
//    @Query("DELETE FROM survey_responses WHERE surveyType=:type AND contextKey=:key AND variable IN (:variables)")
//    suspend fun borrarVariables(type: String, key: String, variables: List<String>)
//
//    @Query("DELETE FROM survey_responses WHERE surveyType=:type AND contextKey=:key")
//    suspend fun borrarTodo(type: String, key: String)
//
//    @Query("""
//        SELECT contextKey,
//               COUNT(*) as totalVariables,
//               SUM(CASE WHEN variable='__survey_completed__' AND valor='true' THEN 1 ELSE 0 END) as completada
//        FROM survey_responses
//        WHERE surveyType=:type AND contextKey IN (:keys)
//        GROUP BY contextKey
//    """)
//    suspend fun obtenerProgresoMultiple(type: String, keys: List<String>): List<ContextProgressRow>
//}