package com.minedu.gob.pe.enaprescalidad.data.local.dao.surveys
import androidx.room.*
import com.minedu.gob.pe.enaprescalidad.data.local.entity.surveys.SurveyConglomeradoEntity
import kotlinx.coroutines.flow.Flow

// ══════════════════════════════════════════════════════════════════════════════
//  RespuestaDao
// ══════════════════════════════════════════════════════════════════════════════

@Dao
interface SurveyConglomeradoDao {
//
//     Observa TODAS las respuestas de una muestra + cuestionario en tiempo real.
//     El Flow emite cada vez que hay un INSERT o UPDATE → la UI se actualiza sola.
    @Query("""
        SELECT * FROM Survey_Conglomerado
        WHERE muestra_id = :muestraId AND survey_id = :surveyId
    """)
    fun observarRespuestas(muestraId: Int, surveyId: String): Flow<List<SurveyConglomeradoEntity>>

    // Upsert: inserta o reemplaza si ya existe (misma clave primaria)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(respuesta: SurveyConglomeradoEntity)

    // Upsert de varias a la vez (útil para restaurar estado)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(respuestas: List<SurveyConglomeradoEntity>)

    // Borrar todas las respuestas de una muestra (resetear cuestionario)
    @Query("DELETE FROM Survey_Conglomerado WHERE muestra_id = :muestraId AND survey_id = :surveyId")
    suspend fun borrarRespuestas(muestraId: Int, surveyId: String)

    // Consulta puntual (sin Flow) para verificar si ya hay respuestas guardadas
    @Query("""
        SELECT COUNT(*) FROM Survey_Conglomerado
        WHERE muestra_id = :muestraId AND survey_id = :surveyId
    """)
    suspend fun contarRespuestas(muestraId: Int, surveyId: String): Int
}
