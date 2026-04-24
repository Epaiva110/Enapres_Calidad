package com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.dao.UsuarioDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.entity.UsuarioEntity

import kotlin.jvm.java

@Database(
    entities = [UsuarioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "App_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
