package com.example.todo.ui.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.database.model.TaskModel
import com.example.todo.databinding.TaskViewHolderBinding


class TaskAdapter(
    val listener: OnTaskClick
) : ListAdapter<TaskModel, TaskAdapter.TaskViewHolder>(DiffAdapter()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            TaskViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }


    inner class TaskViewHolder(private val binding: TaskViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                textTasks.setOnClickListener {
                    val item = getItem(adapterPosition)
                    listener.onTaskClick(item)
                }
                checkbox.setOnClickListener {
                    val item = getItem(adapterPosition)
                    listener.onCheckBoxClick(item)
                }
            }
        }

        fun bind(task: TaskModel) {
            binding.apply {
                checkbox.isChecked = task.completed
                textTasks.text = task.name
                iconImportant.isVisible = task.important
                alarmActive.isVisible = task.alarmActive
            }
        }
    }


    class DiffAdapter : DiffUtil.ItemCallback<TaskModel>() {
        override fun areItemsTheSame(oldItem: TaskModel, newItem: TaskModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TaskModel, newItem: TaskModel) = oldItem == newItem
    }

    interface OnTaskClick {
        fun onTaskClick(task: TaskModel)
        fun onCheckBoxClick(task: TaskModel)
    }
}