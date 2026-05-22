package com.minedu.gob.pe.enaprescalidad.surveys.catalog

import android.content.Context
import com.minedu.gob.pe.enaprescalidad.surveys.SurveyCompletion
import com.minedu.gob.pe.enaprescalidad.surveys.adapter.SurveyGson
import com.minedu.gob.pe.enaprescalidad.surveys.models.Survey
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class ConglomeradoSurveyCatalog @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val survey: Survey by lazy {
        context.assets.open("survey_conglomerado.json").bufferedReader().use { reader ->
            SurveyGson.instance.fromJson(reader.readText(), Survey::class.java)
        }
    }

    val surveyId: String get() = survey.survey_id.ifBlank { SurveyCompletion.SURVEY_ID_CONGLOMERADO }
}
