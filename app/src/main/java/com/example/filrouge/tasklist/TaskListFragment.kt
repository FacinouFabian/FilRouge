package com.example.filrouge.tasklist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filrouge.R
import com.example.filrouge.network.Api
import com.example.filrouge.network.TasksRepository
import com.example.filrouge.task.TaskActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.util.*


class TaskListFragment : Fragment() {
    private val taskList = mutableListOf<Task>()
    private val adapter = TaskListAdapter(taskList)
    private val ADD_TASK_REQUEST_CODE = 666
    private val EDIT_TASK_REQUEST_CODE = 667
    private val tasksRepository = TasksRepository()

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
        tasksRepository.taskList.observe(viewLifecycleOwner) { list ->
            list.map {
                taskList.add(it)
            }
            adapter.notifyDataSetChanged()
        }
        adapter.onDeleteTask = { task ->
            taskList.remove(task)
            adapter.notifyDataSetChanged()
        }
        adapter.onEditTask = { task ->
            val intent = Intent(activity, TaskActivity::class.java)
            intent.putExtra("editedTask", task)
            startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_TASK_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val task = data!!.getSerializableExtra(TaskActivity.TASK_KEY) as Task
            /*System.out.println((taskList))*/
            taskList.add(task)
            adapter.notifyDataSetChanged()
        }

        else if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val task = data!!.getSerializableExtra(TaskActivity.TASK_KEY) as Task
            taskList.find { it.id == task.id }!!.title = task.title
            taskList.find { it.id == task.id }!!.description = task.description
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
            tasksRepository.refresh()
        }
    }
}

class TaskListAdapter(private val taskList: List<Task>) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {
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