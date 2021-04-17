package com.example.todo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todo.database.dao.AlarmDao
import com.example.todo.database.dao.TaskDao
import com.example.todo.database.model.AlarmModel
import com.example.todo.database.model.TaskModel


@Database(entities = [
    TaskModel::class,
    AlarmModel::class
                     ], version = 1)
abstract class ToDoDatabase: RoomDatabase() {

    abstract fun taskDao():TaskDao
    abstract fun alarmDao():AlarmDao

}