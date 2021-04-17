package com.example.todo.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/*
@Entity(tableName = "alarm_table", foreignKeys =[
    ForeignKey(
    entity = TaskModel::class,
    parentColumns = ["id"],
    childColumns = ["task"],
    onDelete = CASCADE)
])
 */


@Parcelize
@Entity(tableName = "alarm_table", foreignKeys =[
    ForeignKey(
        entity = TaskModel::class,
        parentColumns = ["id"],
        childColumns = ["task"],
        onDelete = CASCADE)
])
data class AlarmModel(
    val task: Int,
    val days: String,
    val hours: Int,
    val minutes: Int,
    val active: Boolean = true,
    val repeat: Boolean = false,

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
): Parcelable

