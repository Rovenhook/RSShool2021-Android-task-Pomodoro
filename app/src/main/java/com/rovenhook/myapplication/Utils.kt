package com.rovenhook.myapplication

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val START_TIME = "00:00:00:00"
const val INVALID = "INVALID"
const val COMMAND_START = "COMMAND_START"
const val COMMAND_STOP = "COMMAND_STOP"
const val COMMAND_ID = "COMMAND_ID"
const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
const val MAX_TIME_MS = "MAX_TIME_MS"
const val STOPWATCHES_LIST = "STOPWATCHES_LIST"
const val STOPWATCHES_NEXT_ID = "STOPWATCHES_NEXT_ID"
const val RUNNING_INDEX = "RUNNING_INDEX"
const val CURRENT_TIME_MILLS = "CURRENT_TIME_MILLS"


fun Long.displayTime(): String {
    if (this <= 0L) {
        return START_TIME
    }
    val h = this / 1000 / 3600
    val m = this / 1000 % 3600 / 60
    val s = this / 1000 % 60
    val ms = this % 1000 / 10

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}

fun Int.minsTonMillis(): Long {
    return this.toLong() * 60 * 1000
}
