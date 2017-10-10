package com.suong.employeetracker

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by Thu Suong on 10/6/2017.
 */
object Utils {
    fun isNetWorkConnnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val acNetworkInfo = cm.activeNetworkInfo
        return acNetworkInfo != null && acNetworkInfo.isConnectedOrConnecting
    }
}