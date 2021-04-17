package com.example.todo.ui.addEdit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.database.model.AlarmModel
import com.example.todo.databinding.AddEditViewHolderBinding


class AddEditAdapter(val listener: OnAlarmClick) :
    ListAdapter<AlarmModel, AddEditAdapter.AddEditViewHolder>(AlarmDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddEditViewHolder {
        val binding =
            AddEditViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddEditViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddEditViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


    inner class AddEditViewHolder(private val binding: AddEditViewHolderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alarm: AlarmModel) {

            val time =
                if (alarm.minutes < 10) "${alarm.hours}:0${alarm.minutes}" else "${alarm.hours}:${alarm.minutes}"

            binding.apply {
                alarmSwitch.isChecked = alarm.active
                alarmText.text = time
                alarmText.setOnClickListener {
                    val itemAlarm = getItem(adapterPosition)
                    listener.onTimeClick(itemAlarm)
                }

                alarmText.setOnLongClickListener {
                    val itemAlarm = getItem(adapterPosition)
                    listener.onLongPressClick(itemAlarm, it)
                    true
                }

                alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                    val itemAlarm = getItem(adapterPosition)
                    listener.onSwitchClick(itemAlarm, isChecked)
                }

            }
        }
    }


    class AlarmDiff : DiffUtil.ItemCallback<AlarmModel>() {
        override fun areItemsTheSame(oldItem: AlarmModel, newItem: AlarmModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: AlarmModel, newItem: AlarmModel) =
            oldItem == newItem
    }


    interface OnAlarmClick {
        fun onTimeClick(alarm: AlarmModel)
        fun onSwitchClick(alarm: AlarmModel, isChecked: Boolean)
        fun onLongPressClick(alarm: AlarmModel, view: View)
    }
}