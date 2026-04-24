package com.minedu.gob.pe.encuestasatisfaccinenapres.data.local

import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.dao.TaskDao
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.local.entity.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<Task>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasks()

    suspend fun getTaskById(id: Int): Task? = taskDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    suspend fun toggleTaskCompletion(task: Task) {
        taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
    }
}
