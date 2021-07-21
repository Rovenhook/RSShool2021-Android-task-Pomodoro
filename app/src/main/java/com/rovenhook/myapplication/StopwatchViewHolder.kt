package com.rovenhook.myapplication

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.rovenhook.myapplication.databinding.StopwatchItemBinding
import kotlinx.coroutines.*

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: Job? = null

    fun bind(stopwatch: Stopwatch) {
        listener.printStopwatches()

        if (stopwatch.isFinished) {
            binding.cardViewTimer.setCardBackgroundColor(resources.getColor(R.color.teal_700))
            binding.stopwatchTimer.text = 0L.displayTime()
        } else {
            binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            val current = stopwatch.currentMs
            if (current <= 0L) {
                binding.blinkingIndicator.isInvisible = true
                (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
            }
            binding.cardViewTimer.setCardBackgroundColor(resources.getColor(R.color.teal_200))

        }
            if (stopwatch.isStarted) {
                startTimer(stopwatch)
            }
            else if (!stopwatch.isStarted) {
                stopTimer(stopwatch)
            }
            initButtonsListeners(stopwatch)

            binding.customViewVisual.setPeriod(stopwatch.maxTimeMs)
            binding.customViewVisual.setCurrent(stopwatch.currentMs)

    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        if (!stopwatch.isFinished && stopwatch.currentMs > 0L) {
            binding.startPauseButton.setOnClickListener {
                if (!stopwatch.isFinished && stopwatch.isStarted) {
                    listener.stop(stopwatch.id, stopwatch.currentMs)
                } else if (!stopwatch.isFinished) {
                    listener.isStartPressed(true)
                    listener.start(stopwatch.id)
                }
            }
        } else if (stopwatch.isFinished) {
            binding.startPauseButton.setOnClickListener(null)
        }
        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getText(R.string.label_stop)

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

        timer?.cancel(null)
        timer = getCountDownJob(stopwatch)
        timer?.start()

        listener.exchange(timer, stopwatch.id)
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        binding.startPauseButton.text = resources.getText(R.string.label_start)

        timer?.cancel(null)

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownJob(stopwatch: Stopwatch): Job {
        val job: Job = GlobalScope.launch(Dispatchers.Main) {
            Log.i("TAG", "EXCHANGE 9 isStarted ${stopwatch.isStarted} isFinished ${stopwatch.isFinished} startTime ${stopwatch.startTime} currentMs ${stopwatch.currentMs}")
            val timePassed = System.currentTimeMillis() - stopwatch.startTime
            stopwatch.currentMs -= timePassed
            stopwatch.startTime = System.currentTimeMillis()
            Log.i("TAG", "EXCHANGE 10 isStarted ${stopwatch.isStarted} isFinished ${stopwatch.isFinished} startTime ${stopwatch.startTime} currentMs ${stopwatch.currentMs}")

            while (stopwatch.currentMs > 10L) {
                delay(UNIT_TEN_MS)
                stopwatch.currentMs -= UNIT_TEN_MS
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
                binding.customViewVisual.setCurrent(stopwatch.currentMs)
                stopwatch.startTime = System.currentTimeMillis()
            }
            stopwatch.currentMs = 0L
            Log.i("TAG", "ON FINISH")
            Log.i("TAG", "ON FINISH MIDDLE")
            listener.showFinishedMessage()
            markAsFinished(stopwatch)
            setFinished(stopwatch)
            listener.printStopwatches()
            Log.i("TAG", "ON FINISH FINISH")
        }
        return job
    }

    private fun markAsFinished(stopwatch: Stopwatch) {
        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        binding.cardViewTimer.setCardBackgroundColor(resources.getColor(R.color.teal_700))
        binding.stopwatchTimer.text = 0L.displayTime()
    }

    private fun setFinished(stopwatch: Stopwatch) {
        stopwatch.currentMs = 0L
        stopwatch.startTime = 0L
        stopwatch.isStarted = false
        stopwatch.isFinished = true
    }

    private companion object {
        private const val FINISH_TIME = "00:00:00:00"
        private const val UNIT_TEN_MS = 10L
        private const val INTERVAL = 100L
    }
}