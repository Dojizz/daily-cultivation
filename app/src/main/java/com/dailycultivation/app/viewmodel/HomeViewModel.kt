package com.dailycultivation.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dailycultivation.app.data.db.AppDatabase
import com.dailycultivation.app.data.entity.TaskEntity
import com.dailycultivation.app.data.entity.TaskStatus
import com.dailycultivation.app.data.repository.TaskRepository
import com.dailycultivation.app.data.repository.TaskWithDeadline
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = TaskRepository(db.taskDao())

    val activeTasks: StateFlow<List<TaskWithDeadline>> = repository.observeActiveTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<TaskEntity>> = repository.observeCompletedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expiredTasks: StateFlow<List<TaskWithDeadline>> = repository.observeExpiredTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 启动时标记过期任务
        viewModelScope.launch {
            val pending = db.taskDao().observeByStatus(TaskStatus.PENDING)
            pending.collect { tasks ->
                val now = System.currentTimeMillis()
                tasks.filter { it.createdAt + com.dailycultivation.app.data.entity.DEADLINE_DURATION_MS < now }
                    .forEach { db.taskDao().updateStatus(it.id, TaskStatus.EXPIRED) }
            }
        }
    }

    fun addTask(title: String, description: String = "") {
        viewModelScope.launch {
            repository.addTask(title, description)
        }
    }

    fun completeTask(id: Long) {
        viewModelScope.launch {
            repository.completeTask(id)
        }
    }

    fun restartTask(id: Long) {
        viewModelScope.launch {
            repository.restartTask(id)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }
}
