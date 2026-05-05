package com.minedu.gob.pe.enaprescalidad.di

import android.content.Context
import androidx.room.Room
import com.minedu.gob.pe.enaprescalidad.BuildConfig
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MarcoTrabajoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraReentrevistaDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraViviendaDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SyncDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.UsuarioDaos
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
import javax.inject.Singleton

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
    fun provideUsuarioDao(db: AppDataBase): UsuarioDaos = db.usuarioDao()

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

}
