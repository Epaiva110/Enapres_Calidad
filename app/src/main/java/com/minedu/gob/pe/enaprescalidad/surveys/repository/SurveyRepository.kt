package com.minedu.gob.pe.enaprescalidad.surveys.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.minedu.gob.pe.enaprescalidad.surveys.adapter.SurveyGson
import com.minedu.gob.pe.enaprescalidad.surveys.models.Survey
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyResponse

/**<-No se usa-**/

class SurveyRepository(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "survey_engine.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_SURVEYS = "surveys"
        private const val TABLE_RESPONSES = "survey_responses"

        private const val COLUMN_ID = "survey_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_STRUCTURE = "structure_json"
        private const val COLUMN_ANSWERS = "answers_json"
        private const val COLUMN_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_SURVEYS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT,
                $COLUMN_STRUCTURE TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $TABLE_RESPONSES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_ANSWERS TEXT,
                $COLUMN_UPDATED_AT INTEGER
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SURVEYS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESPONSES")
        onCreate(db)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // OPERACIONES DE ESCRITURA Y LECTURA
    // ─────────────────────────────────────────────────────────────────────────────

    fun saveSurveyStructure(survey: Survey) {
        val db = writableDatabase
        val jsonStr = SurveyGson.instance.toJson(survey)

        val values = ContentValues().apply {
            put(COLUMN_ID, survey.survey_id)
            put(COLUMN_TITLE, survey.title)
            put(COLUMN_STRUCTURE, jsonStr)
        }
        db.insertWithOnConflict(TABLE_SURVEYS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getSurveyStructure(surveyId: String): Survey? {
        val db = readableDatabase
        val cursor = db.query(TABLE_SURVEYS, null, "$COLUMN_ID = ?", arrayOf(surveyId), null, null, null)

        return cursor.use {
            if (it.moveToFirst()) {
                val jsonStr = it.getString(it.getColumnIndexOrThrow(COLUMN_STRUCTURE))
                SurveyGson.instance.fromJson(jsonStr, Survey::class.java)
            } else null
        }
    }

    fun saveResponseProgress(response: SurveyResponse) {
        val db = writableDatabase
        // Serializa el mapa MutableMap<String, Any?> de forma limpia
        val jsonAnswers = SurveyGson.instance.toJson(response.answers)

        val values = ContentValues().apply {
            put(COLUMN_ID, response.survey_id)
            put(COLUMN_ANSWERS, jsonAnswers)
            put(COLUMN_UPDATED_AT, response.updated_at)
        }
        db.insertWithOnConflict(TABLE_RESPONSES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getResponseProgress(surveyId: String): SurveyResponse? {
        val db = readableDatabase
        val cursor = db.query(TABLE_RESPONSES, null, "$COLUMN_ID = ?", arrayOf(surveyId), null, null, null)

        return cursor.use {
            if (it.moveToFirst()) {
                val jsonAnswersStr = it.getString(it.getColumnIndexOrThrow(COLUMN_ANSWERS))
                val updatedAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_UPDATED_AT))

                // Tipo de token para deserializar mapas genéricos con Gson de forma correcta
                val mapType = object : com.google.gson.reflect.TypeToken<MutableMap<String, Any?>>() {}.type
                val answersMap: MutableMap<String, Any?> = SurveyGson.instance.fromJson(jsonAnswersStr, mapType)

                SurveyResponse(
                    survey_id = surveyId,
                    answers = answersMap,
                    updated_at = updatedAt
                )
            } else null
        }
    }
}