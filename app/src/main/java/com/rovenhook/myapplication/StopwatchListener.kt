package com.rovenhook.myapplication

import android.os.CountDownTimer
import kotlinx.coroutines.Job

interface StopwatchListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun delete(id: Int)

    fun showFinishedMessage()

    fun exchange(timer: Job?, runningId: Int)

    fun isStartPressed(isPressed: Boolean)

    fun printStopwatches()
}