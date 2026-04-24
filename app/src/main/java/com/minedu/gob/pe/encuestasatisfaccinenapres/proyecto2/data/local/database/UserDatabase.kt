package com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.dao.MuestraDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.dao.UsuarioDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.MuestraEntity
import com.minedu.gob.pe.encuestasatisfaccinenapres.proyecto2.data.local.entity.UsuarioEntity

@Database(
    entities = [UsuarioEntity::class, MuestraEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun muestraDao(): MuestraDao
}
