package com.example.todo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todo.ui.alarm.service.ResetAlarmService

class BootReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED){
            val resetIntent = Intent(context,ResetAlarmService::class.java)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
                context.startForegroundService(resetIntent)
            }
            else {
                context.startService(resetIntent)
            }
        }
    }
}