package com.example.todo.di

import android.app.Application
import androidx.room.Room
import com.example.todo.database.ToDoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModules {

    @Singleton
    @Provides
    fun provideDatabase(app :Application):ToDoDatabase {
       return Room.databaseBuilder(
            app, ToDoDatabase::class.java,
            "ToDoDatabase"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideTaskDao(db: ToDoDatabase) = db.taskDao()


    @Singleton
    @Provides
    fun provideAlarmDao(db: ToDoDatabase) = db.alarmDao()


    @Singleton
    @Provides
    fun provideScope() = CoroutineScope(SupervisorJob())

}
