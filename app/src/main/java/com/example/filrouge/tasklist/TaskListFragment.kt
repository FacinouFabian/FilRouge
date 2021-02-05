package com.example.filrouge.tasklist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filrouge.R
import com.example.filrouge.network.Api
import com.example.filrouge.network.TasksRepository
import com.example.filrouge.task.TaskActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.*

class TaskListViewModel: ViewModel() {
    private val repository = TasksRepository()
    private val _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>> = _taskList

    fun loadTasks() {
        viewModelScope.launch {
            _taskList.value = repository.loadTasks()
        }
    }
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.removeTask(task)
        }
    }
    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.createTask(task)
        }
    }
    fun editTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }
}

class TaskListFragment : Fragment() {
    private val ADD_TASK_REQUEST_CODE = 666
    private val EDIT_TASK_REQUEST_CODE = 667
    private val viewModel: TaskListViewModel by viewModels()
    private var adapter = TaskListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Pour une [RecyclerView] ayant l'id "recycler_view":
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val addButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButton5)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
        addButton.setOnClickListener {
            val intent = Intent(activity, TaskActivity::class.java)
            startActivityForResult(intent, ADD_TASK_REQUEST_CODE)
        }
        adapter.onDeleteTask = { task ->
            viewModel.deleteTask(task)
            adapter.notifyDataSetChanged()
        }
        adapter.onEditTask = { task ->
            val intent = Intent(activity, TaskActivity::class.java)
            intent.putExtra("editedTask", task)
            startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
        }
        viewModel.taskList.observe(viewLifecycleOwner) { newList ->
            adapter.taskList = newList
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == ADD_TASK_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val task = data!!.getSerializableExtra(TaskActivity.TASK_KEY) as Task
            viewModel.addTask(task)
            adapter.notifyDataSetChanged()
        }

        else if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val task = data!!.getSerializableExtra(TaskActivity.TASK_KEY) as Task
            viewModel.taskList.value?.find { it.id == task.id }!!.title = task.title
            viewModel.taskList.value?.find { it.id == task.id }!!.description = task.description
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            // Ici on ne va pas gérer les cas d'erreur donc on force le crash avec "!!"
            val my_text_view = activity?.findViewById<TextView>(R.id.userInfos)
            val userInfo = Api.userService.getInfo().body()!!
            my_text_view?.text = "${userInfo.firstName} ${userInfo.lastName}"
        }

        lifecycleScope.launch {
            viewModel.loadTasks()
        }
    }
}

class TaskListAdapter() : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {
    var taskList: List<Task> = emptyList()
    var onDeleteTask: ((Task) -> Unit)? = null
    var onEditTask: ((Task) -> Unit)? = null

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(task: Task) {
            itemView.apply {
                val tView = itemView.findViewById<TextView>(R.id.task_title)
                val deleteButton = itemView.findViewById<ImageButton>(R.id.delete)
                val editButton = itemView.findViewById<ImageButton>(R.id.edit)
                tView.text = task.title + '\n' + task.description
                deleteButton.setOnClickListener {
                    onDeleteTask?.invoke(task)
                }
                editButton.setOnClickListener {
                    onEditTask?.invoke(task)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return taskList.count()
    }
}