package com.rovenhook.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ForegroundService : Service() {

    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var timer: Job? = null
    private var stopwatches: ArrayList<Stopwatch> = ArrayList()
    private var nextId: Int = 0
    private var runningIndex = -1
    private var currentMs = 0L

    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Simple Timer")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_baseline_access_alarm_24)
//            .setAutoCancel(true)
//            .setOngoing(false)
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("TAG", "onCreate()")
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("TAG", "onStartCommand()")
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {
                runningIndex = intent?.extras?.getInt(RUNNING_INDEX, -1) ?: return
                currentMs = intent.extras?.getLong(CURRENT_TIME_MILLS, 0L) ?: return
                if (runningIndex < 0) {
                    return
                }
                stopwatches = intent.extras
                    ?.getParcelableArrayList<Stopwatch>(STOPWATCHES_LIST) ?: ArrayList<Stopwatch>()
                nextId = intent.extras?.getInt(STOPWATCHES_NEXT_ID) ?: 0
                Log.i("TAG", "EXCHANGE 2 isStarted ${stopwatches[0].isStarted} isFinished ${stopwatches[0].isFinished} startTime ${stopwatches[0].startTime} currentMs ${stopwatches[0].currentMs}")

                commandStart()
            }
            COMMAND_STOP -> {
                commandStop()
            }
            INVALID -> return
        }
    }

    private fun commandStart() {
        if (isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStart()")
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer()
        } finally {
            isServiceStarted = true
        }
    }

    private fun continueTimer() {
        timer = GlobalScope.launch(Dispatchers.Main) {
//            Log.i("TAG", "EXCHANGE 3 isStarted ${stopwatches[0].isStarted} isFinished ${stopwatches[0].isFinished} startTime ${stopwatches[0].startTime} currentMs ${stopwatches[0].currentMs}")
//
//            var runningStopwatch = stopwatches[runningIndex]
            var text: String
//
//            val timePassed = System.currentTimeMillis() - stopwatches[runningIndex].startTime
//            Log.i("TAG", "Received 2 timePassed ${timePassed}")
//            Log.i("TAG", "EXCHANGE 4 isStarted ${stopwatches[0].isStarted} isFinished ${stopwatches[0].isFinished} startTime ${stopwatches[0].startTime} currentMs ${stopwatches[0].currentMs}")
//            stopwatches[runningIndex].currentMs -= timePassed
//            Log.i("TAG", "Received 2 currentMs after calc ${stopwatches[runningIndex].currentMs}")
//            Log.i("TAG", "EXCHANGE 5 isStarted ${stopwatches[0].isStarted} isFinished ${stopwatches[0].isFinished} startTime ${stopwatches[0].startTime} currentMs ${stopwatches[0].currentMs}")

//            if (stopwatches[runningIndex].currentMs <= 0L) {
//                stopwatches[runningIndex].isFinished = true
//                stopwatches[runningIndex].currentMs = 0L
//                stopwatches[runningIndex].isStarted = false
//            }

//            while (!stopwatches[runningIndex].isFinished) {
            while (currentMs > 0) {
 //               text = stopwatches[runningIndex].currentMs.displayTime().dropLast(3)
                text = currentMs.displayTime().dropLast(3)
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(text)
                )
                delay(INTERVAL)
//                stopwatches[runningIndex].currentMs -= INTERVAL
//                Log.i("TAG", "Received 2 currentMs after delay ${stopwatches[runningIndex].currentMs}")
//                Log.i("TAG", "EXCHANGE 6 isStarted ${stopwatches[0].isStarted} isFinished ${stopwatches[0].isFinished} startTime ${stopwatches[0].startTime} currentMs ${stopwatches[0].currentMs}")

//                if (stopwatches[runningIndex].currentMs <= 0L) {
//                    stopwatches[runningIndex].isFinished = true
//                    stopwatches[runningIndex].currentMs = 0L
//                    stopwatches[runningIndex].isStarted = false
//                }
                currentMs -= INTERVAL
            }
            text = "Time's up!"
            notificationManager?.notify(
                NOTIFICATION_ID,
                getNotification(text)
            )
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        Log.i("TAG", "commandStop()")
        try {
            timer?.cancel()
            Log.i("TAG", "Stop timer in foreground")

            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("TAG", "moveToStartedState(): Running on Android O or higher")
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            Log.d("TAG", "moveToStartedState(): Running on Android N or lower")
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        Log.i("TAG", "Received 3 sending from service isStarted ${stopwatches[0].isStarted} isFinished ${stopwatches[0].isFinished} startTime ${stopwatches[0].startTime} currentMs ${stopwatches[0].currentMs}")

        Log.i("TAG", "EXCHANGE 7 isStarted ${stopwatches[0].isStarted} isFinished ${stopwatches[0].isFinished} startTime ${stopwatches[0].startTime} currentMs ${stopwatches[0].currentMs}")
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.putParcelableArrayListExtra(STOPWATCHES_LIST, stopwatches)
        resultIntent.putExtra(STOPWATCHES_NEXT_ID, nextId)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private companion object {

        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}