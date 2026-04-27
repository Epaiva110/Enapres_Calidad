package com.minedu.gob.pe.enaprescalidad.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.UsuarioDaos
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity

@Database(
    entities = [UsuarioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDaos
    abstract fun muestraconglomeradoDao(): MuestraConglomeradoDao


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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
