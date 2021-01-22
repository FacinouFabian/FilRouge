package com.example.filrouge.tasklist

data class Task(val id: String, val title: String, val description: String = "Task_$id")