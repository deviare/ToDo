package com.example.todo.ui.alarm.set

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.AlarmFragmentBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlin.time.ExperimentalTime


@AndroidEntryPoint
class AlarmFragment : Fragment(R.layout.alarm_fragment) {

    val viewModel: AlarmViewModel by viewModels()

    @ExperimentalTime
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity?)!!.supportActionBar!!.hide()

        val binding = AlarmFragmentBinding.bind(view)
        val navController = findNavController()

        binding.apply {

            timePicker.apply {

                setOnTimeChangedListener { _, hourOfDay, minute ->
                    viewModel.alarmHour = hourOfDay
                    viewModel.alarmMinute = minute
                }

                setIs24HourView(true)
                currentHour = viewModel.alarmHour
                currentMinute = viewModel.alarmMinute
            }

            root.children.forEach { btn ->
                if (btn is ToggleButton) {
                    when (btn.id) {
                        R.id.monday -> {
                            val value = "2"
                            btn.isChecked = viewModel.alarmDays.contains(value)
                            btn.setOnCheckedChangeListener { _, isChecked ->
                                handleDayValue(isChecked, value)
                            }
                        }

                        R.id.tuesday -> {
                            val value = "3"
                            btn.isChecked = viewModel.alarmDays.contains(value)
                            btn.setOnCheckedChangeListener { _, isChecked ->
                                handleDayValue(isChecked, value)
                            }
                        }

                        R.id.wednesday -> {
                            val value = "4"
                            btn.isChecked = viewModel.alarmDays.contains(value)
                            btn.setOnCheckedChangeListener { _, isChecked ->
                                handleDayValue(isChecked, value)
                            }
                        }

                        R.id.thursday -> {
                            val value = "5"
                            btn.isChecked = viewModel.alarmDays.contains(value)
                            btn.setOnCheckedChangeListener { _, isChecked ->
                                handleDayValue(isChecked, value)
                            }
                        }

                        R.id.friday -> {
                            val value = "6"
                            btn.isChecked = viewModel.alarmDays.contains(value)
                            btn.setOnCheckedChangeListener { _, isChecked ->
                                handleDayValue(isChecked, value)
                            }
                        }

                        R.id.saturday -> {
                            val value = "7"
                            btn.isChecked = viewModel.alarmDays.contains(value)
                            btn.setOnCheckedChangeListener { _, isChecked ->
                                handleDayValue(isChecked, value)
                            }
                        }

                        R.id.sunday -> {
                            val value = "1"
                            btn.isChecked = viewModel.alarmDays.contains(value)
                            btn.setOnCheckedChangeListener { _, isChecked ->
                                handleDayValue(isChecked, value)
                            }
                        }
                    }
                }
            }



            repeatCheckbox.isChecked = viewModel.alarmRepeat
            repeatCheckbox.setOnCheckedChangeListener { _, isChecked ->
                viewModel.alarmRepeat = isChecked
            }

            fabSaveAlarm.setOnClickListener {
                viewModel.onSaveAlarmClick()
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.alarmEventFlow.collect { event ->
                when (event) {

                    is AlarmViewModel.AlarmEvent.ShowInvalidAlarmMessage -> {
                        Snackbar.make(
                            requireView(),
                            "Please insert some time and day",
                            Snackbar.LENGTH_SHORT
                        )
                            .show()
                    }
                    is AlarmViewModel.AlarmEvent.ShowSavedAlarmMessage -> {
                        setFragmentResult(
                            "alarm",
                            bundleOf(
                                "time" to event.alarmTime
                                )
                        )
                        navController.navigateUp()
                    }

                    is AlarmViewModel.AlarmEvent.ShowSettingsMessage -> {
                        sendBatterySettings()
                    }
                }
            }
        }


    }


    fun sendBatterySettings() {

        val settingIntent = Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS")
        settingIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        AlertDialog.Builder(requireContext())
            .setTitle("Permission")
            .setMessage("Due to the battery optimization of Android, if you like to receive the reminder with correct timing, add this app to the not optimized group")
            .setPositiveButton(
                "modify settings"
            ) { _, _ ->
                requireContext().applicationContext.startActivity(settingIntent)
            }
            .setNegativeButton("cancel") { _, _ -> null }
            .show()

    }


    private fun handleDayValue(isChecked: Boolean, value: String) {
        if (isChecked) {
            viewModel.alarmDays += value
        } else {
            viewModel.alarmDays = viewModel.alarmDays.replace(value, "", true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity?)!!.supportActionBar!!.show()


    }

}