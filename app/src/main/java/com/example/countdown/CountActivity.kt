package com.example.countdown

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.ankushgrover.hourglass.Hourglass
import com.example.countdown.Utils.showNewNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*


class CountActivity : AppCompatActivity() {

    var appRunningBackground: Boolean = false
    lateinit var notificationManager: NotificationManager
    var counterEnter = true
    private var beforeTime = 0
    private val t = Timer()
    var resumeCounter = 0
    var isAppInBackground = true
    private var countDownTimer: CountDownTimer? = null
    private var resumeCountDown: CountDownTimer? = null

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

    private fun setTimer() {
        if (beforeTime != 1) {
            if (beforeTime == 12) {
                t.scheduleAtFixedRate(
                    object : TimerTask() {
                        override fun run() {
                            if (counter == 10) {
                                Log.e("counterTimer", "run: ")
                                CoroutineScope(Dispatchers.IO).launch {
                                    delay(10000)
                                    notificationManager.notify(
                                        200,
                                        showNewNotification(
                                            "App stopped",
                                            "App stopped",
                                            this@CountActivity
                                        )
                                    )
                                }
                            }
                        }
                    },
                    0,
                    1000
                )
            }
        }

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
        } else {
        }
    }

    override fun onResume() {
        super.onResume()
        if (counter < 10) {
            resumeCounter = counter
            tvCount.text = resumeCounter.toString()
        }
        else{

        }
        Log.e("onResume", "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        startCounter()
        isAppInBackground = true
        if (counter != 0) {
            notificationManager.notify(
                1001,
                showNewNotification("App running", "Your app will be stop after 10 seconds", this)
            )
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        countDownTimer!!.cancel()
    }

    private fun showSecondNotification() {
        notificationManager.notify(
            200,
            showNewNotification("App stopped", "App stopped", this@CountActivity)
        )
    }

    private fun startService() {
        try {
            var startService = Intent(this, CounterService::class.java)
            bindService(intent, mServiceConnection, BIND_AUTO_CREATE)
            startService(startService)
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
        countDownTimer = object : CountDownTimer(50000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(p0: Long) {
                counterEnter = true
                isAppInBackground = false
                tvCount.text = counter.toString()
                Log.e("myCounter", counter.toString())
                counter++
                resumeCounter = counter
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {

            }
        }.start()
    }


//    private fun resumeCounter() {
//        resumeCountDown = object : CountDownTimer(50000, 1000) {
//            @SuppressLint("SetTextI18n")
//            override fun onTick(p0: Long) {
//                tvCount.text = counter.toString()
//                resumeCounter++
//            }
//
//            @SuppressLint("SetTextI18n")
//            override fun onFinish() {
//
//            }
//        }.start()
//    }

    override fun onStart() {
        super.onStart()
        var intent = Intent(applicationContext, CounterService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }


}