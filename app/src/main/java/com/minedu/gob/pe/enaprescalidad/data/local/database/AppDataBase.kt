package com.minedu.gob.pe.enaprescalidad.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.minedu.gob.pe.enaprescalidad.data.local.dao.MuestraConglomeradoDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.SyncDao
import com.minedu.gob.pe.enaprescalidad.data.local.dao.UsuarioDaos
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.SyncEntity
//@Database(
//    entities = [
//        UsuarioEntity::class,
//        MuestraConglomeradoEntity::class
//    ],
//    version = 2,
//    exportSchema = false
//)
//abstract class AppDataBase : RoomDatabase() {
//
//    abstract fun usuarioDao(): UsuarioDaos
//    abstract fun muestraconglomeradoDao(): MuestraConglomeradoDao
//}

@Database(
    entities = [
        UsuarioEntity::class,
        MuestraConglomeradoEntity::class,
        SyncEntity::class
               ],
    version = 5,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDaos
    abstract fun muestraconglomeradoDao(): MuestraConglomeradoDao
    abstract fun syncDao(): SyncDao

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
