package com.example.todo.ui.alarm.triggered

import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.todo.databinding.AlarmActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {

    private val viewModel: AlarmTriggeredViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.turnScreenOn()

        val binding = AlarmActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val msg = intent?.extras?.getString("msg")
        val id = intent?.extras?.getInt("alarmId")

        viewModel.handleRepeatAlarm(id!!, msg!!)
        viewModel.startSounds()

        binding.apply {
            msgAlarm.text = msg.toString()
            dismissBtn.setOnClickListener {
                viewModel.stopSounds()
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopSounds()
    }
}
