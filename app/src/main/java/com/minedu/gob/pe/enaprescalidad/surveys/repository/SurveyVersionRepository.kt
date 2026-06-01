package com.minedu.gob.pe.enaprescalidad.surveys.repository

import android.content.Context
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SurveyVersionDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SurveyVersionEntity
import com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyCatalog
import com.minedu.gob.pe.enaprescalidad.surveys.catalog.SurveyType
import com.minedu.gob.pe.enaprescalidad.surveys.models.SurveyContext
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

// ─────────────────────────────────────────────────────────────────────────────
//  RESULTADO DE RESOLUCIÓN
// ─────────────────────────────────────────────────────────────────────────────

data class ResolvedSurvey(
    val json: String,
    val version: String,
    val origen: String,
    val esSnapshot: Boolean,
)

// ─────────────────────────────────────────────────────────────────────────────
//  FUENTE REMOTA (contrato + stub de desarrollo)
// ─────────────────────────────────────────────────────────────────────────────

data class RemoteSurveyResult(val json: String, val version: String)

interface SurveyRemoteSource {
    suspend fun fetchLatest(surveyType: SurveyType): RemoteSurveyResult?
}

class SurveyRemoteSourceStub : SurveyRemoteSource {
    override suspend fun fetchLatest(surveyType: SurveyType): RemoteSurveyResult? = null
}

// ─────────────────────────────────────────────────────────────────────────────
//  SURVEY VERSION REPOSITORY
//
//  Algoritmo resolveJson(context):
//   1. ¿Existe snapshot para (surveyType, contextKey)? → retornarlo siempre
//   2. Si no → descargar del servidor (o assets si falla)
//   3. Guardar snapshot con INSERT IGNORE (idempotente)
//   4. Retornar JSON
// ─────────────────────────────────────────────────────────────────────────────

@Singleton
class SurveyVersionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val versionDao: SurveyVersionDao,
    private val catalog: SurveyCatalog,
    private val remoteSource: SurveyRemoteSource,
) {
    suspend fun resolveJson(surveyContext: SurveyContext): ResolvedSurvey {
        val type = surveyContext.surveyType
        val key  = surveyContext.contextKey

        val existente = versionDao.obtenerSnapshot(type.name, key)
        if (existente != null) {
            return ResolvedSurvey(
                json       = existente.jsonSnapshot,
                version    = existente.version,
                origen     = existente.origen,
                esSnapshot = true,
            )
        }

        val (json, version, origen) = obtenerJsonActivo(type)

        versionDao.guardarSnapshot(
            SurveyVersionEntity(
                surveyType   = type.name,
                contextKey   = key,
                version      = version,
                jsonSnapshot = json,
                origen       = origen,
                snapshotEn   = System.currentTimeMillis(),
            )
        )

        return ResolvedSurvey(json = json, version = version, origen = origen, esSnapshot = false)
    }

    private suspend fun obtenerJsonActivo(type: SurveyType): Triple<String, String, String> {
        return try {
            val remoto = remoteSource.fetchLatest(type)
            if (remoto != null) Triple(remoto.json, remoto.version, "servidor")
            else jsonDesdeAssets(type)
        } catch (_: Exception) {
            jsonDesdeAssets(type)
        }
    }

    private fun jsonDesdeAssets(type: SurveyType): Triple<String, String, String> {
        val json    = catalog.getJsonString(type)
        val version = Regex(""""version"\s*:\s*"([^"]+)"""").find(json)
            ?.groupValues?.getOrNull(1) ?: "1.0.0"
        return Triple(json, version, "assets")
    }
}