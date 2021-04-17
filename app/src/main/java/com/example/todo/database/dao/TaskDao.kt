package com.example.todo.database.dao

import androidx.room.*
import com.example.todo.database.model.TaskModel
import kotlinx.coroutines.flow.Flow


@Dao
interface TaskDao {

    @Insert
    suspend fun insertTask(task: TaskModel): Long

    @Delete
    suspend fun deleteTask(task: TaskModel)

    @Update
    suspend fun updateTask(task: TaskModel)


    @Query("SELECT * FROM task_table WHERE (completed != :hideComplete or completed = 0) AND name LIKE '%' || :query || '%' ORDER BY important DESC, name")
    fun queryListByName(query: String?, hideComplete: Boolean): Flow<List<TaskModel>>

    @Query("SELECT * FROM task_table WHERE (completed != :hideComplete or completed = 0) AND name LIKE '%' || :query || '%' ORDER BY important DESC, date")
    fun queryListByDate(query: String?, hideComplete: Boolean): Flow<List<TaskModel>>

    @Query("SELECT name FROM task_table WHERE id = :id")
    fun queryTaskNameById(id: Int): String


    @Query(" SELECT * FROM task_table WHERE id = :id")
    fun queryTaskById(id: Int): Flow<TaskModel>

    @Query("DELETE FROM task_table WHERE completed = 1")
    fun deleteCompleted()


}