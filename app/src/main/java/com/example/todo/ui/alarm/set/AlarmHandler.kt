package com.example.todo.ui.alarm.set

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.example.todo.receivers.AlarmReceiver
import com.example.todo.ui.TAG
import com.example.todo.ui.alarm.triggered.AlarmActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class AlarmHandler
@Inject
constructor(
    @ApplicationContext val context: Context
) {
    private val am by lazy {
        getSystemService(context, AlarmManager::class.java) as AlarmManager
    }

    fun setAlarm(time: Long, requestCode: Int, message: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("msg", message)
        intent.putExtra("alarmId", requestCode)
        intent.action = "com.example.todo.CUSTOM_INTENT"
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val pendingShowList = PendingIntent.getActivity(
            context,
            25025,
            Intent(context, AlarmActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmType = AlarmManager.RTC_WAKEUP
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                am.setAlarmClock(AlarmManager.AlarmClockInfo(time, pendingShowList), pendingIntent)
            }
            else -> {
                am.setExact(alarmType, time, pendingIntent)
            }
        }
    }

    fun deleteAlarm(requestCode: Int, message: String?) {
        val msg = message ?: ""
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("msg", msg)
        intent.putExtra("alarmId", requestCode)
        intent.action = "com.example.todo.CUSTOM_INTENT"
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        try {
            for (x in 0..20) {
                am.cancel(pendingIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, " Error deleting the alarm Intent", e.cause)
            e.printStackTrace()
        }
    }
}