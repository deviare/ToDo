package com.example.todo.database.dao

import androidx.room.*
import com.example.todo.database.model.AlarmModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Insert
    suspend fun insertAlarm(alarm: AlarmModel): Long

    @Delete
    suspend fun deleteAlarm(alarm: AlarmModel)

    @Update
    suspend fun updateAlarm(alarm: AlarmModel)

    @Query("SELECT * FROM alarm_table")
    fun queryAlarms(): Flow<List<AlarmModel>>

    @Query("SELECT * FROM alarm_table WHERE task = :id")
    fun queryAlarmsByTask(id: Int): Flow<List<AlarmModel>>

    @Query("SELECT * FROM alarm_table WHERE id = :id ")
    fun queryAlarmById(id: Int): Flow<AlarmModel>

    @Query("SELECT * FROM alarm_table where active = 1")
    fun queryActiveAlarms(): Flow<List<AlarmModel>>

}