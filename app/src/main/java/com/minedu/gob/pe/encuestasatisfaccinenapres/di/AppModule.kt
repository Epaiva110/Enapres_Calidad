package com.minedu.gob.pe.encuestasatisfaccinenapres.di

import android.content.Context
import androidx.room.Room
import com.minedu.gob.pe.encuestasatisfaccinenapres.BuildConfig
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.dao.MuestraDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.dao.UsuarioDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.database.UserDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
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
            install(Realtime)
        }

    @Provides
    @Singleton
    fun providePostgrest(client: SupabaseClient) =
        client.postgrest

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UserDatabase =
        Room.databaseBuilder(context, UserDatabase::class.java, "user_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUsuarioDao(db: UserDatabase): UsuarioDao = db.usuarioDao()

    @Provides
    fun provideMuestraDao(db: UserDatabase): MuestraDao = db.muestraDao()
}