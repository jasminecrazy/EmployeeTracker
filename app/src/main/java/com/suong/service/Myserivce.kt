package com.suong.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.PendingIntent
import android.app.AlarmManager
import android.content.Context
import android.util.Log
import android.content.Context.ALARM_SERVICE
import java.util.*


/**
 * Created by Billy on 11/26/2017.
 */
class Myserivce : Service() {
    private var alarmManager: AlarmManager? = null
    private var pendingIntent: PendingIntent? = null
    private var myIntent: Intent? = null
    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("hello", "onCreate")
    }

    override fun onDestroy() {
        cancelAlarm()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        cancelAlarm()
        stopSelf()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAlarm()
        return START_NOT_STICKY
    }

    private fun startAlarm() {
        // Set the alarm to start at 8:00 a.m.
        val calendar = Calendar.getInstance()
        calendar.setTimeInMillis(System.currentTimeMillis())
        calendar.set(Calendar.MILLISECOND, 5000)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        myIntent = Intent(this, Alarm::class.java)
        pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, myIntent, 0)
        alarmManager!!.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 , pendingIntent)
    }

    private fun cancelAlarm() {
        val sender = PendingIntent.getBroadcast(applicationContext, 0, myIntent, 0)
        alarmManager!!.cancel(sender)
    }
}