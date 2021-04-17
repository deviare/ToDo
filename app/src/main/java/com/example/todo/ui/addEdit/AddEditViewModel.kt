package com.example.todo.ui.addEdit

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.database.model.AlarmModel
import com.example.todo.database.model.TaskModel
import com.example.todo.repository.MainRepository
import com.example.todo.ui.TAG
import com.example.todo.utils.AlarmsTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddEditViewModel
@ViewModelInject
constructor(
    private val repo: MainRepository,
    @Assisted val state: SavedStateHandle
) : ViewModel() {

    var task = state.get<TaskModel>("task")
        set(value) {
            field = value
            state.set("task", value)
        }

    var taskName = task?.name ?: ""
    var taskImportant = task?.important ?: false

    var taskId = task?.id ?: 0
    private val taskIdFlow = flow {
        emit(taskId)
    }

    private val addEditEventChannel = Channel<AddEditEvent>()
    val addEditEventFlow = addEditEventChannel.receiveAsFlow()

    private val _alarmFlow = taskIdFlow.flatMapLatest {
        repo.queryAlarmsForTask(it)
    }
    val alarmsFlow = _alarmFlow


    fun addEditTask(goToAlarm: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if (taskName.isNotBlank()) {
            if (task == null) {
                val newTask = TaskModel(taskName, taskImportant)
                val newTaskId = repo.insertNewTask(newTask)
                taskId = newTaskId
                task = newTask.copy(id = newTaskId)
                if (!goToAlarm) addEditEventChannel.send(AddEditEvent.ShowMessageTaskSuccess(true))

            } else {
                val updatedTask = task!!.copy(name = taskName, important = taskImportant)
                repo.updateTask(updatedTask)
                if (!goToAlarm) addEditEventChannel.send(AddEditEvent.ShowMessageTaskSuccess(false))
            }
        } else {
            addEditEventChannel.send(AddEditEvent.ShowMessageInvalid)
        }
    }

    fun onAddAlarmClick() = viewModelScope.launch {
        addEditEventChannel.send(AddEditEvent.NavigateAlarmFragment(true, null))
    }

    fun onAlarmTimeClick(alarm: AlarmModel) = viewModelScope.launch {
        addEditEventChannel.send(AddEditEvent.NavigateAlarmFragment(false, alarm))
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onAlarmSwitchClick(alarm: AlarmModel, checked: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {

            if (checked) {
                if (alarm.days.isBlank()) {
                    addEditEventChannel.send(AddEditEvent.NavigateAlarmFragment(false, alarm))
                } else {
                    val updatedAlarm = alarm.copy(active = checked)
                    repo.updateAlarm(updatedAlarm)
                    val updatedTask = task!!.copy(alarmActive = true)
                    repo.updateTask(updatedTask)
                    task = updatedTask
                    AlarmsTime.getNextAlarmMills(alarm.days, alarm.hours, alarm.minutes).also {
                        repo.setAlarmIntent(it, alarm.id, taskName)
                        addEditEventChannel.send(AddEditEvent.ShowAlarmMessage(it))
                    }
                }
            } else {
                repo.deleteAlarmIntent(alarm.id, null)
                val updatedAlarm = alarm.copy(active = checked)
                repo.updateAlarm(updatedAlarm)
                checkLastAlarm()
            }
        }



    private suspend fun checkLastAlarm () = withContext(Dispatchers.IO) {
        var activeAlarms = false
        repo.queryAlarmsForTask(taskId).first().forEach {
            if (it.active) {
                activeAlarms = true
            }
        }
        activeAlarms
    }.also { activeAlarms ->
        if (!activeAlarms) {
            val updatedTask = task!!.copy(alarmActive = false)
            repo.updateTask(updatedTask)
            task = updatedTask
        }
    }

    fun deleteAlarm(alarm: AlarmModel) = viewModelScope.launch(Dispatchers.IO) {
        repo.deleteAlarm(alarm)
        checkLastAlarm()
    }

    fun resetTask() = viewModelScope.launch(Dispatchers.IO){
        repo.queryTaskById(taskId).first().run {
            task = this
        }
    }
}

sealed class AddEditEvent {
    data class ShowMessageTaskSuccess(val new: Boolean) : AddEditEvent()
    object ShowMessageInvalid : AddEditEvent()
    data class NavigateAlarmFragment(val new: Boolean, val alarm: AlarmModel?) : AddEditEvent()
    data class ShowAlarmMessage(val time: Long) : AddEditEvent()
}