package com.example.countdown

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.countdown.Utils.showNewNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*
import android.app.Service.START_NOT_STICKY
import android.app.Service.START_STICKY
import android.app.KeyguardManager
import android.os.Build

import android.os.PowerManager


class CountActivity : AppCompatActivity() {

    var appRunningBackground: Boolean = false
    private var beforeTime = 0
    private val t = Timer()
    var isAppInBackground = true
    lateinit var countService: CounterService

    companion object {
        var counter = 0
        var userCome = 0
        var inResumeCome = 0
        var inBackgroundCounter = 0
        var secondNotificationPop = false
        lateinit var notificationManager: NotificationManager
    }


    private var mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val myServiceBinder: CounterService.CounterStartService =
                p1 as CounterService.CounterStartService
            countService = myServiceBinder.myService

            countService.myCountMain.observe(this@CountActivity) {
                tvCount.text = it.toString()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e("onStop", "onStop")
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
            counter = 0
            userCome = 0
            inResumeCome = 0
            inBackgroundCounter = 0
            startService()
            countService.startTimer()
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
        secondNotificationPop = false
        if (counter == inBackgroundCounter + 10) {
            if (userCome == 1) {
                inResumeCome = 1
            }

        } else {
            userCome = 0
            inResumeCome = 0
        }

    }


    override fun onPause() {
        super.onPause()

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val isScreenOn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager.isInteractive
        } else {
            powerManager.isScreenOn
        }

        if (isScreenOn != false){
            isAppInBackground = true
            if (counter != 0) {
                notificationManager.notify(
                    1001,
                    showNewNotification("App running", "Your app will be stop after 10 seconds", this)
                )
                inBackgroundCounter = counter
                userCome = 1
            }

            Log.e("onPause","onPause")

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.e("destroy", "destroy")

    }

    private fun showSecondNotification() {
        notificationManager.notify(
            200,
            showNewNotification("App stopped", "App stopped", this@CountActivity)
        )
    }

    private fun startService() {
        try {
            val startService = Intent(this, CounterService::class.java)
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


//    fun calculateTime(
//        startTime: Long,
//        lifecycleScope: LifecycleCoroutineScope,
//        time: (String) -> Unit
//        ) {
//            var timeInMilliseconds: Long = 0L
//            val elsp = 0L
//            var updated = 0L
//            timerJob = lifecycleScope.launch(CoroutineExceptionHandler(){ _, _ ->
//                time("00:00:00")
//            }) {
//                while (isActive) {
//                    delay(1000)
//                    timeInMilliseconds = SystemClock.uptimeMillis() - startTime
//                    updated = timeInMilliseconds + elsp
//                    var seconds = (updated / 1000)
//                    var minutes = seconds / 60
//                    val hours = minutes / 60
//                    seconds %= 60
//                    minutes %= 60
//                    val timeString = String.format(
//                        "%d:%s:%s",
//                        hours,
//                        String.format("%02d", minutes),
//                        String.format(
//                            "%02d",
//                            seconds
//                        )
//                    )
//                    val format = String.format(
//                        resources.getString(R.string.modify_time_string),
//                        timeString
//                    )
//                    time(format)
//                }
//                time("00:00:00")
//            }
//
//
//        }


//        countDownTimer = object : CountDownTimer(50000, 1000) {
//            @SuppressLint("SetTextI18n")
//            override fun onTick(p0: Long) {
//                counterEnter = true
//                isAppInBackground = false
//                tvCount.text = counter.toString()
//                Log.e("myCounter", counter.toString())
//                counter++
//                resumeCounter = counter
//            }
//
//            @SuppressLint("SetTextI18n")
//            override fun onFinish() {
//
//            }
//        }.start()


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