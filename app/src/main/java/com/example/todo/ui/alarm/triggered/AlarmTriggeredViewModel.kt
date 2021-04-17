package com.example.todo.ui.alarm.triggered

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.todo.repository.MainRepository
import com.example.todo.utils.AlarmsTime
import com.example.todo.utils.CurrentDate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


class AlarmTriggeredViewModel
@ViewModelInject
constructor(
    private val repo: MainRepository,
    @ApplicationContext val context: Context
) : ViewModel() {

    private val wakeLock : PowerManager.WakeLock by lazy {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "alarm:wake-up"
        )
    }

    private val vib by lazy {
        context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    private val player: MediaPlayer by lazy {
        val alarmTone: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
            .build()
        MediaPlayer().apply {
            setAudioAttributes(audioAttributes)
            setDataSource(context, alarmTone)
            isLooping = true
            setVolume(100F, 100F)
        }
    }

    private val timing = longArrayOf(100L, 1500L, 250L, 1000L)

    @RequiresApi(Build.VERSION_CODES.O)
    private val vibEffect = VibrationEffect.createWaveform(timing, 0)

    fun turnScreenOn() {
        wakeLock.acquire(5 * 60 * 1000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startSounds() {
        vib.vibrate(vibEffect)
        player.start()
    }

    fun stopSounds() {
        vib.cancel()
        player.stop()
        player.release()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun handleRepeatAlarm(id: Int, msg: String) = GlobalScope.launch(Dispatchers.IO) {
        withTimeout(2 * 60 * 1000) {
            val alarm = repo.queryAlarmById(id).first()
            if (alarm.repeat) {
                AlarmsTime.getNextAlarmMills(
                    alarm.days,
                    alarm.hours,
                    alarm.minutes
                ).also {
                    repo.setAlarmIntent(it, id, msg)
                }
            } else {
                val todayNumber = CurrentDate.getCurrentDayNumber().toString()
                val newDays = alarm.days.replace(todayNumber, "")
                if (newDays.isBlank()) {
                    val task = repo.queryTaskById(alarm.task).first()
                    repo.updateTask(task.copy(alarmActive = false))
                    repo.updateAlarm(alarm.copy(days = newDays, active = false))
                } else {
                    repo.updateAlarm(alarm.copy(days = newDays))
                    AlarmsTime.getNextAlarmMills(
                        newDays,
                        alarm.hours,
                        alarm.minutes
                    ).also {
                        repo.setAlarmIntent(it, id, msg)
                    }
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        if (wakeLock.isHeld) wakeLock.release()
    }

}






