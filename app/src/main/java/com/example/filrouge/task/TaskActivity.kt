package com.example.filrouge.task

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.filrouge.R
import com.example.filrouge.tasklist.Task
import java.io.Serializable
import java.util.*
import android.app.Activity
import android.widget.EditText

class TaskActivity : AppCompatActivity() {
    companion object {
        const val TASK_KEY = "newTask"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task)

        val validateButton = this.findViewById<Button>(R.id.validate)
        val titleInput = this.findViewById<EditText>(R.id.title)
        val descriptionInput = this.findViewById<EditText>(R.id.description)
        val task = intent?.getSerializableExtra(TASK_KEY) as? Task

        System.out.println((task))

        validateButton.setOnClickListener {
            // Instanciation d'un nouvel objet [Task]
            val newTask = Task(id = UUID.randomUUID().toString(), title = titleInput.text.toString(), description = descriptionInput.text.toString())
            intent?.putExtra("newTask", newTask)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}


