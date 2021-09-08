package com.example.countdown

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.*
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.countdown.CountActivity.Companion.secondNotificationPop
import com.example.countdown.CountActivity.Companion.userCome
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class CounterService : LifecycleService() {
    private var alarmBinder: IBinder = CounterStartService()
    var myCount = MutableLiveData<Float>()
    lateinit var timerJob: Job
    var myCountMain = MutableLiveData<String>()


    inner class CounterStartService : Binder() {
        val myService: CounterService
            get() = this@CounterService
    }

    override fun onCreate() {
        super.onCreate()
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                ""
            }
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)

//        startMyCounter {
//            myCount.value = it
//            if(CountActivity.counter==10){
//                CountActivity().showSecondNotification()
//            }
//            Log.e("TAG", "onCreate: ", )
//        }

    }
    fun startTimer(){
        calculateTime(SystemClock.uptimeMillis(), lifecycleScope) {

        }
    }

    fun calculateTime(
        startTime: Long,
        lifecycleScope: LifecycleCoroutineScope,
        time: (String) -> Unit
    ) {
        var timeInMilliseconds: Long = 0L
        val elsp = 0L
        var updated = 0L
        var timerCompare = 1
        timerJob = lifecycleScope.launch(CoroutineExceptionHandler(){ _, _ ->
            time("00:00:00")
        }) {
            while (isActive) {
                delay(1000)
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime
                updated = timeInMilliseconds + elsp
                var seconds = (updated / 1000)
                var minutes = seconds / 60
                val hours = minutes / 60
                seconds %= 60
                minutes %= 60
                val timeString = String.format(
                    "%d:%s:%s",
                    hours,
                    String.format("%02d", minutes),
                    String.format(
                        "%02d",
                        seconds
                    )
                )
                timerCompare++

                CountActivity.counter = seconds.toInt()
                Log.e("myCounter", CountActivity.counter.toString())
                val format = String.format(
                    resources.getString(R.string.modify_time_string),
                    timeString
                )
                time(format)
//                if (secondNotificationPop == false) {
                    if (CountActivity.inResumeCome == 1 || (CountActivity.inResumeCome == 0 && CountActivity.userCome == 1)) {
                        if (CountActivity.counter == CountActivity.inBackgroundCounter + 10) {
                            CountActivity.notificationManager.notify(
                                200,
                                Utils.showNewNotification(
                                    "App stopped",
                                    "App stopped",
                                    this@CounterService
                                )

                            )

                            stopSelf()

                            secondNotificationPop = true

                        }
//                    }
                }

                myCountMain.value = format
            }
            time("00:00:00")
        }


    }

    fun startMyCounter(callback: (Float) -> Unit) {
        object : CountDownTimer(50000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(p0: Long) {
                CountActivity.counter++
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {

            }
        }.start()
        callback(CountActivity.counter.toFloat())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return alarmBinder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("serviceDestroy","serviceDestroy")
        stopService()
        CountActivity.counter = 0
    }

    private fun stopService() {
        stopSelf()
    }

}