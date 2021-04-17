package com.example.todo.ui.task

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.database.model.TaskModel
import com.example.todo.databinding.TaskFragmentBinding
import com.example.todo.utils.setOnTextListener
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect


@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.task_fragment), TaskAdapter.OnTaskClick {

    private val viewModel: TaskViewModel by viewModels()
    private val taskAdapter = TaskAdapter(this)
    private lateinit var searchBar: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val binding = TaskFragmentBinding.bind(view)
        binding.apply {
            taskRecycler.apply {
                adapter = taskAdapter
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                makeFlag(
                    1,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                )
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val task = taskAdapter.currentList[position]
                    viewModel.deleteTask(task)
                }
            }).attachToRecyclerView(taskRecycler)

            fabAddTask.setOnClickListener { viewModel.addTaskClick() }
        }

        listenChannel(navController)
        setHasOptionsMenu(true)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskFlow.collect {
                taskAdapter.submitList(it)
            }
        }

        setFragmentResultListener("inserted") { _, bundle ->
            Snackbar.make(
                requireView(),
                "task successfully ${bundle.get("action")}",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun listenChannel(navController: NavController) =
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEventFlow.collect { event ->
                when (event) {

                    is TaskViewModel.TaskEvent.NavigateNewTaskFragment -> {
                        val directions =
                            TaskFragmentDirections.actionTaskFragmentToAddEditFragment(null)
                        navController.navigate(directions)
                    }

                    is TaskViewModel.TaskEvent.ShowTaskDeleteMessage -> {
                        Snackbar.make(
                            requireView(),
                            "Task deleted successfully",
                            Snackbar.LENGTH_LONG
                        )
                            .setAction("UNDO") {
                                viewModel.insertTask(event.task)
                            }
                            .show()
                    }

                    is TaskViewModel.TaskEvent.NavigateEditTaskFragment -> {
                        val directions =
                            TaskFragmentDirections.actionTaskFragmentToAddEditFragment(event.task)
                        navController.navigate(directions)
                    }
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_menu, menu)
        val search = menu.findItem(R.id.search_menu)
        val hideCompleted = menu.findItem(R.id.hide_completed)

        lifecycleScope.launchWhenStarted {
            viewModel.preferencesFlow.collect {
                hideCompleted.isChecked = it.hideComplete
            }
        }

        searchBar = search.actionView as SearchView
        viewModel.searchQuery.value?.let { query ->
            if (query.isNotBlank()) {
                search.expandActionView()
                searchBar.setQuery(query, false)
            }
        }

        searchBar.setOnTextListener { query ->
            viewModel.searchQuery.value = query
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.sort_by_date -> {
                viewModel.updatePrefs(PreferenceManager.SortBy.SORT_BY_DATE)
                true
            }

            R.id.sort_by_name -> {
                viewModel.updatePrefs(PreferenceManager.SortBy.SORT_BY_NAME)
                true
            }

            R.id.hide_completed -> {
                item.isChecked = !item.isChecked
                viewModel.updatePrefs(item.isChecked)
                true
            }

            R.id.delete_completed -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete all completed tasks")
                    .setMessage("Are you sure you want to delete all tasks mark as completed?")
                    .setPositiveButton("yes") { _, _ -> viewModel.deleteCompleted() }
                    .setNegativeButton("cancel", null)
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onTaskClick(task: TaskModel) {
        viewModel.taskClick(task)
    }

    override fun onCheckBoxClick(task: TaskModel) {
        viewModel.checkboxClick(task)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchBar.setOnQueryTextListener(null)
    }
}