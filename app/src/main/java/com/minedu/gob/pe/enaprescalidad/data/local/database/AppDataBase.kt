package com.minedu.gob.pe.enaprescalidad.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.minedu.gob.pe.enaprescalidad.data.local.dao.HogarDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MarcoTrabajoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraReentrevistaDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraViviendaDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.PersonaDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SurveyResponseDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SurveyVersionDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SyncDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.UsuarioDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.VisitaConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.VisitaHogarDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.ViviendaDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.surveys.SurveyConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraReentrevistaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraViviendaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MarcoTrabajoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.views.MarcoTrabajoView
import com.minedu.gob.pe.enaprescalidad.data.local.entity.surveys.SurveyConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.ViviendaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.HogarEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.PersonaEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.VisitaConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.VisitaHogarEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SurveyVersionEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SurveyResponseEntity

@Database(
    entities = [
        UsuarioEntity::class,
        MuestraConglomeradoEntity::class,
        SyncEntity::class,
        MuestraViviendaEntity::class,
        MuestraReentrevistaEntity::class,
        MarcoTrabajoEntity::class,
        SurveyConglomeradoEntity::class,
        ViviendaEntity::class,
        HogarEntity::class,
        PersonaEntity::class,
        VisitaConglomeradoEntity::class,
        VisitaHogarEntity::class,
        SurveyVersionEntity::class,
        SurveyResponseEntity::class,
               ],
    views = [MarcoTrabajoView::class],
    version = 19,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun muestraconglomeradoDao(): MuestraConglomeradoDao
    abstract fun syncDao(): SyncDao

    abstract fun muestraViviendaDao(): MuestraViviendaDao
    abstract fun muestraReentrevistaDao(): MuestraReentrevistaDao
    abstract fun marcoTrabajoDao(): MarcoTrabajoDao
    abstract fun surveyconglomeradoDao(): SurveyConglomeradoDao   // ← agregar
    abstract fun viviendaDao(): ViviendaDao
    abstract fun personaDao(): PersonaDao
    abstract fun hogarDao(): HogarDao
    abstract fun visitaConglomeradoDao(): VisitaConglomeradoDao
    abstract fun visitaHogarDao(): VisitaHogarDao
    abstract fun surveyVersionDao(): SurveyVersionDao
    abstract fun surveyResponseDao(): SurveyResponseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "App_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
