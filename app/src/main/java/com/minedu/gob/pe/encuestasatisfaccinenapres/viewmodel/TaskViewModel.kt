package com.minedu.gob.pe.encuestasatisfaccinenapres.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.TaskRepository
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Database.TaskDatabase
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.Priority
import com.minedu.gob.pe.encuestasatisfaccinenapres.data.Local.Entity.Task
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FilterType { ALL, PENDING, COMPLETED }

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val filter: FilterType = FilterType.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val _filter = MutableStateFlow(FilterType.ALL)

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        val dao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(dao)
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            _filter.flatMapLatest { filter ->
                when (filter) {
                    FilterType.ALL -> repository.allTasks
                    FilterType.PENDING -> repository.pendingTasks
                    FilterType.COMPLETED -> repository.completedTasks
                }
            }.collect { tasks ->
                _uiState.update { it.copy(tasks = tasks, filter = _filter.value) }
            }
        }
    }

    fun setFilter(filter: FilterType) {
        _filter.value = filter
    }

    fun addTask(title: String, description: String, priority: Priority) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                Task(title = title.trim(), description = description.trim(), priority = priority)
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun toggleCompletion(task: Task) {
        viewModelScope.launch { repository.toggleTaskCompletion(task) }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch { repository.deleteCompletedTasks() }
    }
}
