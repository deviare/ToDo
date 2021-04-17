package com.example.todo.utils

import java.util.*

object CurrentDate {
    private val cal = Calendar.getInstance().apply {
        timeInMillis=System.currentTimeMillis()
    }
    fun getCurrentMinutes() = cal.get(Calendar.MINUTE)
    fun getCurrentHours() = cal.get(Calendar.HOUR_OF_DAY)
    fun getCurrentDayNumber() = cal.get(Calendar.DAY_OF_WEEK)
    fun getPastMidNight() =  (  cal.timeInMillis -
                                cal.get(Calendar.HOUR_OF_DAY) * 3600 * 1000 -
                                cal.get(Calendar.MINUTE) * 60 * 1000)
}