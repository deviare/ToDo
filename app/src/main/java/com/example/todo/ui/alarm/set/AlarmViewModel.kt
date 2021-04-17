package com.example.todo.ui.alarm.set

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.database.model.AlarmModel
import com.example.todo.repository.MainRepository
import com.example.todo.ui.TAG
import com.example.todo.ui.alarm.service.WakeLockService
import com.example.todo.utils.AlarmsTime
import com.example.todo.utils.CurrentDate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime


@RequiresApi(Build.VERSION_CODES.M)
class AlarmViewModel
@ViewModelInject
constructor(
    private val repo: MainRepository,
    @Assisted val state: SavedStateHandle,
    @ApplicationContext val context: Context
) : ViewModel() {

    private val pm: PowerManager by lazy {
        context.getSystemService(PowerManager::class.java)
    }
    val alarm = state.get<AlarmModel>("alarm")

    private val taskId = state.get<Int>("taskId")
    private val taskMessage = if (state.get<String>("message").isNullOrBlank()) "Important task!" else state.get<String>("message")


    var alarmMinute = alarm?.minutes ?: CurrentDate.getCurrentMinutes()
    var alarmHour = alarm?.hours ?: CurrentDate.getCurrentHours()
    var alarmDays = alarm?.days ?: ""
    var alarmRepeat = alarm?.repeat ?: false

    private val alarmEventChannel = Channel<AlarmEvent>()
    val alarmEventFlow = alarmEventChannel.receiveAsFlow()


    @ExperimentalTime
    @RequiresApi(Build.VERSION_CODES.M)
    fun onSaveAlarmClick() = viewModelScope.launch(Dispatchers.IO) {
        val packageName = context.packageName
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            alarmEventChannel.send(AlarmEvent.ShowSettingsMessage)
        }
        if (alarmDays.isNotEmpty()) {
            val task = repo.queryTaskById(taskId!!).first()
            repo.updateTask(task.copy(alarmActive = true))
            if (alarm != null) {
                val updatedAlarm = alarm.copy(
                    task = taskId,
                    days = alarmDays,
                    hours = alarmHour,
                    minutes = alarmMinute,
                    repeat = alarmRepeat,
                    active = true
                )
                repo.updateAlarm(updatedAlarm)
                repo.deleteAlarmIntent(alarm.id, taskMessage)
                setAlarmIntent(alarmMinute, alarmHour, alarmDays, alarm.id, taskMessage!!)

            } else {
                val newAlarm = AlarmModel(
                    task = taskId,
                    days = alarmDays,
                    hours = alarmHour,
                    minutes = alarmMinute,
                    repeat = alarmRepeat,
                    active = true
                )
                val alarmId = repo.insertAlarm(newAlarm)
                setAlarmIntent(alarmMinute, alarmHour, alarmDays, alarmId, taskMessage!!)
            }
        } else {
            alarmEventChannel.send(AlarmEvent.ShowInvalidAlarmMessage)
        }
    }


    private suspend fun setAlarmIntent(min: Int, hour: Int, days: String, id: Int, message: String) =
        withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            val alarmMills = AlarmsTime.getNextAlarmMills(days, hour, min)
            repo.setAlarmIntent(alarmMills, id, message)
            alarmEventChannel.send(AlarmEvent.ShowSavedAlarmMessage(alarmMills))
        }


    sealed class AlarmEvent {
        object ShowSettingsMessage : AlarmEvent()
        object ShowInvalidAlarmMessage : AlarmEvent()
        data class ShowSavedAlarmMessage(val alarmTime: Long) : AlarmEvent()
    }

}
