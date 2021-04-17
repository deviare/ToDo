package com.example.todo.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.todo.database.dao.AlarmDao
import com.example.todo.database.dao.TaskDao
import com.example.todo.database.model.AlarmModel
import com.example.todo.database.model.TaskModel
import com.example.todo.ui.TAG
import com.example.todo.ui.alarm.set.AlarmHandler
import com.example.todo.ui.task.PreferenceManager
import com.example.todo.utils.AlarmsTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.time.ExperimentalTime


class MainRepository
@Inject
constructor(
    private val taskDao: TaskDao,
    private val alarmDao: AlarmDao,
    private val am: AlarmHandler
) {

    suspend fun insertNewTask(task: TaskModel): Int = taskDao.insertTask(task).toInt()

    fun queryTaskById(id:Int) = taskDao.queryTaskById(id)

    suspend fun deleteTask(task: TaskModel) {
        GlobalScope.launch(Dispatchers.IO) {
            queryAlarmsForTask(task.id).first().forEach {
                deleteAlarm(it)
            }
            taskDao.deleteTask(task)
        }
    }

    fun queryListTask(
        query: String?,
        sortOrder: PreferenceManager.SortBy,
        hideComplete: Boolean
    ): Flow<List<TaskModel>> {
        return when (sortOrder) {
            PreferenceManager.SortBy.SORT_BY_NAME -> {
                taskDao.queryListByName(query, hideComplete)
            }
            PreferenceManager.SortBy.SORT_BY_DATE -> {
                taskDao.queryListByDate(query, hideComplete)
            }
        }
    }

    fun queryAlarmsForTask(id: Int): Flow<List<AlarmModel>> = alarmDao.queryAlarmsByTask(id)

    suspend fun updateTask(updatedTask: TaskModel) = taskDao.updateTask(updatedTask)

    fun deleteCompletedTask() = taskDao.deleteCompleted()

    suspend fun updateAlarm(updatedAlarm: AlarmModel) = alarmDao.updateAlarm(updatedAlarm)

    suspend fun insertAlarm(alarm: AlarmModel) = alarmDao.insertAlarm(alarm).toInt()

    suspend fun deleteAlarm(alarm: AlarmModel) = GlobalScope.launch(Dispatchers.IO) {
            val id = alarm.id
            val msg = taskDao.queryTaskNameById(alarm.task)
            deleteAlarmIntent(id, msg)
            alarmDao.deleteAlarm(alarm)
        }


    @RequiresApi(Build.VERSION_CODES.M)
    fun setAlarmIntent(time: Long, id: Int, message: String) = am.setAlarm(time, id, message)


    suspend fun deleteAlarmIntent(id: Int, msg: String?) {
        if (msg.isNullOrEmpty()) {
            val taskId = alarmDao.queryAlarmById(id).first().task
            val text = taskDao.queryTaskNameById(taskId)
            am.deleteAlarm(id, text)
        } else {
            am.deleteAlarm(id, msg)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @ExperimentalTime
    suspend fun resetAlarms(): Boolean {
        alarmDao.queryActiveAlarms().collect { list ->
            if (list.isNotEmpty()) {
                list.forEach { alarm ->
                    val message = taskDao.queryTaskNameById(alarm.task)
                    AlarmsTime.getNextAlarmMills(
                        alarm.days,
                        alarm.hours,
                        alarm.minutes
                    ).also {
                        if (message.isNotEmpty()) {
                            am.setAlarm(it, alarm.id, message)
                        }
                    }
                }
            }
        }
        return false
    }

    fun queryAlarmById(id: Int) = alarmDao.queryAlarmById(id)
}
