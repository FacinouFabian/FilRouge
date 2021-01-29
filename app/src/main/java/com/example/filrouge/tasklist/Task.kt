package com.example.filrouge.tasklist

import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class Task(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    var title: String,
    @SerialName("description")
    var description: String = "Task_$this.id"
): Serializable {}