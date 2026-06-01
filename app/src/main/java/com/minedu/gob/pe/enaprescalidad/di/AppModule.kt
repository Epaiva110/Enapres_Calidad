package com.minedu.gob.pe.enaprescalidad.di

import android.content.Context
import androidx.room.Room
import com.minedu.gob.pe.enaprescalidad.BuildConfig
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
import com.minedu.gob.pe.enaprescalidad.data.local.database.AppDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.Postgrest
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest)
        }

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient): Postgrest = client.postgrest

    // Ya no necesitas "provideSupabaseApi" si UsuarioApi ya tiene @Inject

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDataBase =
        Room.databaseBuilder(context, AppDataBase::class.java, "App_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUsuarioDao(db: AppDataBase): UsuarioDao = db.usuarioDao()

    @Provides
    fun provideMuestraConglomeradoDao (db: AppDataBase): MuestraConglomeradoDao = db.muestraconglomeradoDao()

    @Provides
    fun provideSyncDao (db: AppDataBase): SyncDao = db.syncDao()

    @Provides
    fun provideMuestraViviendaDao(db: AppDataBase): MuestraViviendaDao = db.muestraViviendaDao()

    @Provides
    fun provideMuestraReentrevistaDao(db: AppDataBase): MuestraReentrevistaDao = db.muestraReentrevistaDao()

    @Provides
    fun provideMarcoTrabajoDao(db: AppDataBase): MarcoTrabajoDao = db.marcoTrabajoDao()

    @Provides
    fun provideSurveyConglomeradoDao(db: AppDataBase): SurveyConglomeradoDao = db.surveyconglomeradoDao()

    @Provides
    fun provideViviendaDao(db: AppDataBase): ViviendaDao = db.viviendaDao()

    @Provides
    fun provideHogarDao(db: AppDataBase): HogarDao = db.hogarDao()

    @Provides
    fun providePersonaDao(db: AppDataBase): PersonaDao = db.personaDao()

    @Provides
    fun provideVisitaConglomeradoDao(db: AppDataBase): VisitaConglomeradoDao = db.visitaConglomeradoDao()

    @Provides
    fun provideVisitaHogarDao(db: AppDataBase): VisitaHogarDao = db.visitaHogarDao()
    @Provides
    fun provideSurveyVersionDao(db: AppDataBase): SurveyVersionDao = db.surveyVersionDao()
    @Provides
    fun provideSurveyResponseDao(db: AppDataBase): SurveyResponseDao = db.surveyResponseDao()

}
