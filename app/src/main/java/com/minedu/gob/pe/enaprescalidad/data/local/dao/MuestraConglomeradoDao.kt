package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minedu.gob.pe.enaprescalidad.data.local.entity.MuestraConglomeradoEntity
import com.minedu.gob.pe.enaprescalidad.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MuestraConglomeradoDao {
    @Query("SELECT * FROM muestraConglomerado WHERE usuario = :user ORDER BY anioSup, mesSup, perSup")
    suspend fun getMuestraUsuario(user: String): List<MuestraConglomeradoEntity>

//    @Query("SELECT * FROM muestraConglomerado WHERE usuario = :usuario")
//    suspend fun getByUsuario(usuario: String): List<MuestraConglomeradoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(data: List<MuestraConglomeradoEntity>)

    @Query("DELETE FROM muestraConglomerado WHERE usuario = :usuario")
    suspend fun deleteByUsuario(usuario: String)


//    @Query("SELECT * FROM usuario WHERE usuario = :user LIMIT 1")
//    suspend fun getUser(user: String): UsuarioEntity?
//
    //@Query("SELECT * FROM muestraConglomerado ORDER BY anioSup,mesSup, perSup")
    //fun getAllMuestraConglomerado(): Flow<List<MuestraConglomeradoEntity>>

    //@Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    //fun getPendingTasks(): Flow<List<Task>>

    //@Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY createdAt DESC")
    //fun getCompletedTasks(): Flow<List<Task>>

    //@Query("SELECT * FROM tasks WHERE id = :id")
    //suspend fun getTaskById(id: Int): Task?

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //suspend fun insertTask(task: Task): Long

    //@Update
    //suspend fun updateTask(task: Task)

    //@Delete
    //suspend fun deleteTask(task: Task)

    //@Query("DELETE FROM tasks WHERE isCompleted = 1")
    //suspend fun deleteCompletedTasks()
}