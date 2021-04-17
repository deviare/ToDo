package com.example.todo.ui.alarm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.todo.ui.alarm.triggered.AlarmActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class WakeLockService : Service() {

    private val partialWakeLock: PowerManager.WakeLock by lazy {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            javaClass.simpleName
        )
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val extra = intent?.extras
        val msg = extra?.getString("msg")
        val alarmId = extra?.getInt("alarmId")

        GlobalScope.launch(Dispatchers.Main) {
                val tenMin: Long = 10*60*1000
                withTimeout(tenMin){
                    partialWakeLock.acquire(tenMin)
                }

                Intent(applicationContext, AlarmActivity::class.java).apply {
                    putExtra("msg",msg)
                    putExtra("alarmId",alarmId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run {
                    applicationContext.startActivity(this)
                }
            }


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (partialWakeLock.isHeld) {
            partialWakeLock.release()
        }
    }

    override fun onBind(intent: Intent?) = null

}