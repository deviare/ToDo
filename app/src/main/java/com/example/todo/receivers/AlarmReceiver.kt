package com.example.todo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.todo.ui.alarm.service.WakeLockService
import com.example.todo.ui.alarm.triggered.AlarmActivity


class AlarmReceiver :BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {

        val extra = intent?.extras
        val msg = extra?.getString("msg")
        val alarmId = extra?.getInt("alarmId")

        Intent(context, WakeLockService::class.java).apply {
            putExtra("msg",msg)
            putExtra("alarmId",alarmId)
        }.run {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
                context.startForegroundService(this)
            }
            else {
                context.startService(this)
            }
        }
    }
}