package com.example.nbhung.testcallapi

import android.util.Log
import java.util.*
import java.text.SimpleDateFormat


/**
 * Created by nbhung on 10/11/2017.
 */
class DateOfDate {
    companion object {
        fun getDay(): String {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            var dayNew: String
            var monthNew: String
            var yearNew: String
            if (day < 10) {
                dayNew = "0" + day
            } else dayNew = day.toString()
            if ((month + 1) < 10) {
                monthNew = "0" + (month + 1)
            } else monthNew = (month + 1).toString()
            if (year < 10) {
                yearNew = "0" + year
            } else yearNew = (year.toString())

            return yearNew + "-" + monthNew + "-" + dayNew
        }

        fun getTimeGloba(): String {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val second = c.get(Calendar.SECOND)
            val getDay: String = getDay()
            var hourNew: String
            var minuteNew: String
            if (hour < 10) {
                hourNew = "0" + 1
            } else hourNew = hour.toString()
            if (minute < 10) {
                minuteNew = "0" + minute
            } else minuteNew = minute.toString()
            val getGlobla: String = getDay + "T" + hourNew + ":" + minuteNew + ":" + "00" + ""

            return getGlobla
        }

        fun getTimeNow(): String {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val second = c.get(Calendar.SECOND)
            var hourNew: String
            var minuteNew: String
            if (hour < 10) {
                hourNew = "0" + 1
            } else hourNew = hour.toString()
            if (minute < 10) {
                minuteNew = "0" + minute
            } else minuteNew = minute.toString()
            return hourNew + ":" + minuteNew + ":" + second
        }
    }
}