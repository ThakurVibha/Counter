package com.example.countdown.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.countdown.activities.CountActivity

object  Utils {
    var CHANNEL_ID = "ForegroundService Kotlin"
    var NEW_CHANNEL_ID="id"
    @RequiresApi(Build.VERSION_CODES.O)
    fun notificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Counter channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun newNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NEW_CHANNEL_ID, "Counter channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = ContextCompat.getSystemService(context, NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

     fun showNewNotification(title: String, msg: String, context: Context): Notification {
        val notificationIntent = Intent(context, CountActivity::class.java)
        notificationIntent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = Intent(context, CountActivity::class.java)
        val buttonPendingIntent = PendingIntent.getBroadcast(context, 0, pendingIntent, 0)
        return NotificationCompat.Builder(context, Utils.NEW_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(msg)
            .setSmallIcon(android.R.drawable.checkbox_on_background)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // or NotificationCompat.PRIORITY_MAX
            .addAction(
                android.R.drawable.sym_def_app_icon,
                "ForegroundService",
                buttonPendingIntent
            ).build()
    }
}