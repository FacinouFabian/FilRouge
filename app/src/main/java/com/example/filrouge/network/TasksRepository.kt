package com.example.filrouge.network

import com.example.filrouge.tasklist.Task

class TasksRepository {
    // Le web service requête le serveur
    private val webService = Api.tasksWebService

    suspend fun loadTasks(): List<Task>? {
        val response = webService.getTasks()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun removeTask(task: Task) {
        webService.deleteTask(task.id)
    }
    suspend fun createTask(task: Task) {
        webService.createTask(task)
    }

    suspend fun updateTask(task: Task) {
        webService.updateTask(task, id = task.id)
    }
}