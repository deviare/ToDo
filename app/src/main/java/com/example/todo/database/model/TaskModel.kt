package com.example.todo.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat


@Parcelize
@Entity(tableName = "task_table")
data class TaskModel(
    val name: String,
    val important: Boolean = false,
    val completed: Boolean = false,
    val date: Long = System.currentTimeMillis(),
    val alarmActive: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
) : Parcelable {
    fun getFormattedDate(): String =
        "Task created: ${SimpleDateFormat("dd/MM/yy HH:mm").format(date)}"
}