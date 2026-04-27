package com.minedu.gob.pe.enaprescalidad.data.local.dao

import androidx.room.Dao

@Dao
interface MuestraConglomeradoDao {

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