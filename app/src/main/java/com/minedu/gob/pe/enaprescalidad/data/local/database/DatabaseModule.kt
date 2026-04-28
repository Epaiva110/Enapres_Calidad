package com.minedu.gob.pe.enaprescalidad.data.local.database

import android.content.Context
import androidx.room.Room
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.UsuarioDaos
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
//object DatabaseModule {
//
//    @Provides
//    @Singleton
//    fun provideDatabase(
//        @ApplicationContext context: Context
//    ): AppDataBase {
//        return Room.databaseBuilder(
//            context,
//            AppDataBase::class.java,
//            "App_database"
//        ).fallbackToDestructiveMigration()
//            .build()
//    }
//
//    @Provides
//    fun provideUsuarioDao(db: AppDataBase): UsuarioDaos {
//        return db.usuarioDao()
//    }
//
//    @Provides
//    fun provideMuestraConglomeradoDao(db: AppDataBase): MuestraConglomeradoDao {
//        return db.muestraconglomeradoDao()
//    }
//}
