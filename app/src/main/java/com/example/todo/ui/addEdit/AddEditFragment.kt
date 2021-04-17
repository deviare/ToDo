package com.example.todo.ui.addEdit

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.database.model.AlarmModel
import com.example.todo.databinding.AddEditFragmentBinding
import com.example.todo.ui.TAG
import com.example.todo.utils.CurrentDate
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class AddEditFragment : Fragment(R.layout.add_edit_fragment), AddEditAdapter.OnAlarmClick {

    private val viewModel: AddEditViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = AddEditFragmentBinding.bind(view)
        val navController = findNavController()
        val alarmAdapter = AddEditAdapter(this)

        binding.apply {

            editTask.setText(viewModel.taskName)
            editTask.doOnTextChanged { text, _, _, _ ->
                btnAddAlarm.isEnabled = !text.isNullOrBlank()
                viewModel.taskName = text.toString()
            }

            switchImportant.isChecked = viewModel.taskImportant
            switchImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportant = isChecked
            }

            dateText.isVisible = viewModel.task != null
            dateText.text = viewModel.task?.getFormattedDate() ?: ""

            addEditRecycler.apply {
                adapter = alarmAdapter
            }

            btnAddAlarm.isEnabled = !editTask.text.isNullOrBlank()

            btnAddAlarm.setOnClickListener {
                lifecycleScope.launchWhenStarted {
                    viewModel.addEditTask(true).join()
                    viewModel.onAddAlarmClick()
                }
            }

            fabSaveTask.setOnClickListener {
                viewModel.addEditTask()
            }

        }

        listenOnChannel(navController)

        setFragmentResultListener("alarm") { _, bundle ->
                viewModel.resetTask()
                showAlarmSnack((bundle["time"] as Long))
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.alarmsFlow.collect {
                alarmAdapter.submitList(it)
            }
        }
    }

    private fun listenOnChannel(navController: NavController) =
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditEventFlow.collect { event ->
                when (event) {

                    is AddEditEvent.ShowMessageTaskSuccess -> {
                        val action = if (event.new) "added" else "edited"
                        setFragmentResult("inserted", bundleOf("action" to action))
                        navController.navigateUp()
                    }

                    is AddEditEvent.ShowMessageInvalid -> {
                        Snackbar.make(
                            requireView(),
                            "Task name can not be left black",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    is AddEditEvent.NavigateAlarmFragment -> {
                        if (event.new) {
                            val directions =
                                AddEditFragmentDirections.actionAddEditFragmentToAlarmFragment(
                                    null,
                                    viewModel.taskId,
                                    viewModel.taskName
                                )
                            navController.navigate(directions)
                        } else {
                            val directions =
                                AddEditFragmentDirections.actionAddEditFragmentToAlarmFragment(
                                    event.alarm,
                                    viewModel.taskId,
                                    viewModel.taskName
                                )
                            navController.navigate(directions)
                        }
                    }

                    is AddEditEvent.ShowAlarmMessage -> {
                        showAlarmSnack(event.time)
                    }
                }
            }
        }

    override fun onTimeClick(alarm: AlarmModel) {
        viewModel.onAlarmTimeClick(alarm)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onSwitchClick(alarm: AlarmModel, isChecked: Boolean) {
        viewModel.onAlarmSwitchClick(alarm, isChecked)
    }
    override fun onLongPressClick(alarm: AlarmModel, view: View) {
        showPopUp(alarm, view)
    }

    private fun showPopUp(alarm: AlarmModel, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.pop_up_menu)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.delete_alarm_menu -> {
                    viewModel.deleteAlarm(alarm)
                    true
                }

                R.id.edit_alarm_menu -> {
                    viewModel.onAlarmTimeClick(alarm)
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }
        popup.gravity = Gravity.AXIS_Y_SHIFT
        popup.show()
    }

    private fun showAlarmSnack(time: Long) {
        val now = System.currentTimeMillis()
        val diff = time.minus(now)
        val h = diff/3600000
        val hMills = h * 3600000
        val minMills = diff - hMills
        val m = minMills/60000
        Snackbar.make(
            requireView(),
            "Set alarm on $h hours and $m minutes",
            Snackbar.LENGTH_LONG
        ).show()
    }
}