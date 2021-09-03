package com.example.countdown.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.countdown.R
import com.example.countdown.utils.Utils
import com.example.countdown.utils.Utils.showNewNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*


class CountActivity : AppCompatActivity() {
    var appRunningBackground: Boolean = false
    lateinit var notificationManager: NotificationManager
    var counterEnter = true
    var job: Job? = null


    companion object {
        var counter = 0
    }

    lateinit var countService: CounterService
    private var mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            var myServiceBinder: CounterService.CounterStartService =
                p1 as CounterService.CounterStartService
            countService = myServiceBinder.myService
            countService.myCount.observe(this@CountActivity) {
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.e("onServiceDisconnected", "onServiceDisconnected: ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            onclick()
        }
        checkAppInBackground()
    }

    private fun onclick() {
        btnStart.setOnClickListener {
            startCounter()
            startService()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Utils.newNotificationChannel(this)
            }
        }
    }

    private fun checkAppInBackground() {
        val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(runningAppProcessInfo)
        appRunningBackground =
            runningAppProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        if (appRunningBackground) {
            Toast.makeText(
                applicationContext,
                "Application is Running in Background",
                Toast.LENGTH_SHORT
            ).show()
        } else {


        }

    }

    override fun onPause() {
        super.onPause()
        counterEnter = true
        notificationManager.notify(
            1001,
            showNewNotification("App running", "Your app will be stop after 10 seconds", this)
        )
        showSecondNotification()
    }

    override fun onResume() {
        super.onResume()
        job?.cancel()
    }

    private fun showSecondNotification() {
        job =   CoroutineScope(Dispatchers.IO).launch {
            delay(10000)
                notificationManager.notify(
                    100,
                    showNewNotification("App stopped", "App stopped", this@CountActivity)
                )
            }
            stopService()
    }

    private fun startService() {
        try {
            var startService = Intent(this, CounterService::class.java)
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
            startService(startService)
            Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("alarmTAG", e.localizedMessage)
        }
    }

    private fun stopService() {
        var stopService = Intent(this, CounterService::class.java)
        unbindService(mServiceConnection)
        stopService(stopService)
    }

    private fun startCounter() {
        object : CountDownTimer(50000, 1000) {

            @SuppressLint("SetTextI18n")
            override fun onTick(p0: Long) {
                counterEnter = true
                tvCount.text = counter.toString()
                Log.e("CounterTag", counter.toString())
                counter++
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
            }
        }.start()
    }

    override fun onStart() {
        super.onStart()
        var intent = Intent(applicationContext, CounterService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }


}