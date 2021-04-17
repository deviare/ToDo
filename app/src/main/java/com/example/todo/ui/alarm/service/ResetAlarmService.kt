package com.example.todo.ui.alarm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import com.example.todo.repository.MainRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.time.ExperimentalTime


@AndroidEntryPoint
class ResetAlarmService : Service() {
    @Inject
    lateinit var repo: MainRepository
    private val wl: PowerManager.WakeLock by lazy {
        val powMan = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        return@lazy powMan.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "todo.com:Wakelock")
    }

    @ExperimentalTime
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wl.setReferenceCounted(true)
        wl.acquire(5 * 60 * 1000L /*30 minutes*/)

        GlobalScope.launch(Dispatchers.IO) {
            launch(Dispatchers.IO) { repo.resetAlarms() }.join()
            wl.release()
        }
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        if (wl.isHeld) wl.release()
    }

    override fun onBind(intent: Intent?) = null
}
