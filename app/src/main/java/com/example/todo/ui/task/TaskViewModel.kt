package com.example.todo.ui.task

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.example.todo.database.model.TaskModel
import com.example.todo.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TaskViewModel
@ViewModelInject
constructor(
    private val repo: MainRepository,
    private val prefManager: PreferenceManager,
    @Assisted val state: SavedStateHandle
) : ViewModel() {

    val preferencesFlow = prefManager.preferenceFlow

    var searchQuery = state.getLiveData("searchQuery", "")
        set(value) {
            field = value
            state.set("searchQuery", value)
        }

    private val _taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, prefs ->
        Pair(query, prefs)
    }.flatMapLatest {
        repo.queryListTask(it.first, it.second.sortOrder, it.second.hideComplete)
    }

    val taskFlow = _taskFlow

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEventFlow = taskEventChannel.receiveAsFlow()


    fun updatePrefs(prefs: Any) = viewModelScope.launch(Dispatchers.IO) {
        when (prefs) {
            is PreferenceManager.SortBy -> {
                prefManager.onSortOrderSelect(prefs)
            }
            is Boolean -> {
                prefManager.onHideCompletedSelect(prefs)
            }
        }
    }


    fun addTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateNewTaskFragment)
    }

    fun deleteTask(task: TaskModel) = viewModelScope.launch {
        repo.deleteTask(task)
        taskEventChannel.send(TaskEvent.ShowTaskDeleteMessage(task))
    }

    fun insertTask(task: TaskModel) = viewModelScope.launch {
        repo.insertNewTask(task)
    }

    fun taskClick(task: TaskModel) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateEditTaskFragment(task))
    }

    fun checkboxClick(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        val updatedTask = task.copy(completed = !task.completed)
        repo.updateTask(updatedTask)
    }

    fun deleteCompleted() = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteCompletedTask()
    }


    sealed class TaskEvent {
        object NavigateNewTaskFragment : TaskEvent()
        class ShowTaskDeleteMessage(val task: TaskModel) : TaskEvent()
        class NavigateEditTaskFragment(val task: TaskModel) : TaskEvent()
    }

}







