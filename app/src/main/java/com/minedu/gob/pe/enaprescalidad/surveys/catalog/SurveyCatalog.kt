package com.minedu.gob.pe.enaprescalidad.surveys.catalog

import android.content.Context
import com.minedu.gob.pe.enaprescalidad.surveys.adapter.SurveyGson
import com.minedu.gob.pe.enaprescalidad.surveys.models.Survey
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

enum class SurveyType(
    val fileName: String,
    val supervisionAsset: String,
    val defaultSurveyId: String,
) {
    CONGLOMERADO(
        fileName         = "survey_conglomerado.json",
        supervisionAsset = "supervision_conglomerado.json",
        defaultSurveyId  = "ENAPRES_VERIFICACION_CONGLOMERADO_2026",
    ),
    VIVIENDA(
        fileName         = "survey_vivienda.json",
        supervisionAsset = "supervision_vivienda.json",
        defaultSurveyId  = "ENAPRES_VERIFICACION_VIVIENDA_2026",
    ),
    REENTREVISTA(
        fileName         = "survey_reentrevista.json",
        supervisionAsset = "supervision_reentrevista.json",
        defaultSurveyId  = "ENAPRES_VERIFICACION_REENTREVISTA_2026",
    );

    companion object {
        fun fromString(value: String): SurveyType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CONGLOMERADO
    }
}

@Singleton
class SurveyCatalog @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val surveyCache = mutableMapOf<SurveyType, Survey>()
    private val jsonCache   = mutableMapOf<SurveyType, String>()
    fun getSurvey(type: SurveyType): Survey =
        surveyCache.getOrPut(type) {
            SurveyGson.instance.fromJson(getJsonString(type), Survey::class.java)
        }

    fun getJsonString(type: SurveyType): String =
        jsonCache.getOrPut(type) {
            context.assets.open(type.fileName).bufferedReader().use { it.readText() }
        }

    fun getSurveyId(type: SurveyType): String =
        getSurvey(type).survey_id.ifBlank { type.defaultSurveyId }

    fun invalidate(type: SurveyType) {
        surveyCache.remove(type)
        jsonCache.remove(type)
    }
}
