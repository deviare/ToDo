package com.example.todo.utils

import android.util.Log
import com.example.todo.ui.TAG
import java.util.*

object AlarmsTime {

    fun getNextAlarmMills(days: String, hour: Int, min: Int): Long {

        var nextDay = 0
        val today = CurrentDate.getCurrentDayNumber()
        val pastMidNight = CurrentDate.getPastMidNight()


        val calAlarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
        }

        val daysInt = mutableListOf<Int>().apply {
            days.forEach {
                this.add(Integer.parseInt(it.toString()))
            }
        }.toList().sorted()

        for (x in 1..7) {
            var tmpDay = today + x
            if (tmpDay > 7) tmpDay -= 7
            if (tmpDay in daysInt){
                nextDay = tmpDay
                break
            }
        }

        return when {
            today == nextDay -> {
                return getAlarmTime( calAlarm, pastMidNight)
            }
            today < nextDay -> {
                val dayDiff = nextDay - today
                return (pastMidNight +
                        (24 * 3600 * 1000 * dayDiff) +
                        calAlarm.get(Calendar.HOUR_OF_DAY) * 3600 * 1000 +
                        calAlarm.get(Calendar.MINUTE) * 60 * 1000)
            }

            today > nextDay -> {
                val dayDiff = 7 - today + nextDay
                return (pastMidNight +
                        (24 * 3600 * 1000 * dayDiff) +
                        calAlarm.get(Calendar.HOUR_OF_DAY) * 3600 * 1000 +
                        calAlarm.get(Calendar.MINUTE) * 60 * 1000)
            }

            else -> 0L
        }
    }


    private fun getAlarmTime(calAlarm: Calendar, pastMidNight: Long): Long {
        val oneDayInMills = 24 * 3600 * 1000
        val oneWeekInMills = oneDayInMills * 7
        val hourNow = CurrentDate.getCurrentHours()
        val hour = calAlarm.get(Calendar.HOUR_OF_DAY)
        val min = calAlarm.get(Calendar.MINUTE)

        return when {
            hourNow < hour -> {
                Calendar.getInstance().apply {
                    timeInMillis = pastMidNight + hour * 3600 * 1000 + min * 60 * 1000
                    set(Calendar.SECOND, 0)
                }.timeInMillis

            }
            hourNow > hour -> {
                Calendar.getInstance().apply {
                    val nextWeekMidNight = pastMidNight + oneWeekInMills
                    timeInMillis = nextWeekMidNight + hour * 3600 * 1000 + min * 60 * 1000
                    set(Calendar.SECOND, 0)
                }.timeInMillis
            }

            else -> {
                return if (CurrentDate.getCurrentMinutes() < min) {
                    Calendar.getInstance().apply {
                        timeInMillis = pastMidNight + hour * 3600 * 1000 + min * 60 * 1000
                        set(Calendar.SECOND, 0)
                    }.timeInMillis
                } else {
                    Calendar.getInstance().apply {
                        timeInMillis =
                            pastMidNight + oneWeekInMills + hour * 3600 * 1000 + min * 60 * 1000
                        set(Calendar.SECOND, 0)
                    }.timeInMillis
                }
            }
        }
    }
}
