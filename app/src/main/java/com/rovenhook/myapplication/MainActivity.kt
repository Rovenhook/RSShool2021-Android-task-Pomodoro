package com.rovenhook.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.rovenhook.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.*


import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.ServiceCompat.STOP_FOREGROUND_REMOVE
import androidx.core.app.ServiceCompat.stopForeground
import androidx.lifecycle.*
import androidx.lifecycle.ProcessLifecycleOwner
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {
    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)

    private var stopwatches = ArrayList<Stopwatch>()
    private var nextId: Int = 0
    private var close = false
    private var timer: Job? = null
    private var runningId: Int = -1
    private var isStartPressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stopwatches = intent
            .getParcelableArrayListExtra<Stopwatch>(STOPWATCHES_LIST) ?: ArrayList<Stopwatch>()
        nextId = intent.getIntExtra(STOPWATCHES_NEXT_ID, 0)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
            stopwatchAdapter.submitList(stopwatches.toList())
        }

        binding.addNewStopwatchButton.setOnClickListener {
            val str = binding.editTextNumber.text.toString()
            if (str.isNotEmpty()) {
                val timeInMinsNumber = str.toInt()
                if (timeInMinsNumber > 0) {
                    val maxTimeInMills = timeInMinsNumber.minsTonMillis()
                    stopwatches.add(Stopwatch(nextId++, maxTimeInMills, maxTimeInMills, false, 0, false))
                    stopwatchAdapter.submitList(stopwatches.toList())
                } else {
                    Toast.makeText(
                        this,
                        "Set time has to be grerater than zero",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                Toast.makeText(this, "Set time has to be grerater than zero", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun delete(id: Int) {
        val foundStopwatch = stopwatches.find { it.id == id }
        if (foundStopwatch?.isStarted ?: false) {
            timer?.cancel(null)
        }
        stopwatches.remove(foundStopwatch)
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    override fun showFinishedMessage() {
        Toast.makeText(this, "Time is up!", Toast.LENGTH_LONG).show()
    }

    override fun exchange(timer: Job?, runningId: Int) {
        this.timer = timer
        this.runningId = runningId
    }

    override fun isStartPressed(isPressed: Boolean) {
        isStartPressed = isPressed
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()

        stopwatches.forEach {
            if (it.isFinished) {
                newTimers.add(Stopwatch(it.id, 0, 0, false, 0, true))
            } else if (it.id == id) {
                var startTime: Long = 0

                if (isStarted == true && isStartPressed == true) {
                    startTime = System.currentTimeMillis()
                }

                newTimers.add(
                    Stopwatch(
                        it.id,
                        currentMs ?: it.currentMs,
                        it.maxTimeMs,
                        isStarted,
                        startTime,
                        it.isFinished
                    )
                )
            } else if (isStarted == true) {
                newTimers.add(Stopwatch(it.id, it.currentMs, it.maxTimeMs, false, 0, it.isFinished))
            } else {
                newTimers.add(it)
            }
        }
        isStartPressed = false
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    override fun onStop() {
        super.onStop()
        if (close != true) {
            timer?.cancel()
            var runningIndex: Int = -1
            for ((index, watch) in stopwatches.withIndex()) {
                if (watch.isStarted == true && watch.currentMs > 0) {
                    runningIndex = index
                    break
                }
            }
            if (runningIndex >= 0 && stopwatches[runningIndex].currentMs > 0) {
               val startIntent = Intent(this, ForegroundService::class.java)
                startIntent.putExtra(COMMAND_ID, COMMAND_START)
                startIntent.putParcelableArrayListExtra(STOPWATCHES_LIST, stopwatches)
                startIntent.putExtra(STOPWATCHES_NEXT_ID, nextId)
                startIntent.putExtra(CURRENT_TIME_MILLS, stopwatches[runningIndex].currentMs)
                startIntent.putExtra(RUNNING_INDEX, runningIndex)
                startService(startIntent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }

    override fun onBackPressed() {
        close = true
        super.onBackPressed()
    }
}