package com.example.countdown.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.*
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.example.countdown.R
import kotlinx.android.synthetic.main.activity_main.*

class CounterService : Service() {
    private var alarmBinder: IBinder = CounterStartService()
    var myCount = MutableLiveData<Float>()
    inner class CounterStartService : Binder() {
        val myService: CounterService
            get() = this@CounterService
    }

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(this, "Counter Service ", Toast.LENGTH_SHORT).show()
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                ""
            }
        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)

    }

    override fun onBind(p0: Intent?): IBinder? {
        return alarmBinder
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val channel = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId

    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
        CountActivity.counter=0
    }

    private fun stopService() {
        stopSelf()
    }



}