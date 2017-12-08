package com.suong.service

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.nbhung.testcallapi.DateOfDate
import com.google.android.gms.maps.model.LatLng
import com.suong.employeetracker.Utils
import com.suong.model.SharedPreferencesManager
import com.suong.model.sendEmployeess
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.location.LocationManager
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.content.Context.LOCATION_SERVICE



class Alarm : BroadcastReceiver() {
    private var dem: Int = 0
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        sendLocation(context)
    }

    fun sendLocation(context: Context?) {
        val locationManager = context!!.getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return
        }
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val myAdd: String = Utils.convertAddr(LatLng(location!!.latitude, location.longitude), context)
        val response = IEmployee
        val id: Int = SharedPreferencesManager.getIdUser(context)!!.toInt()
        val userLogin = com.suong.model.sendLocation(sendEmployeess(id), location.longitude, location.latitude, myAdd, DateOfDate.getTimeGloba(), DateOfDate.getDay(), "")
        response.sendLocation(userLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Toast.makeText(context, " send location success", Toast.LENGTH_LONG).show()
                    dem = 0

                }, { error ->
                    dem++
                    Toast.makeText(context, "try to send again", Toast.LENGTH_SHORT).show()
                    if (dem <= 2)
                        sendLocation(context)
                })
    }
}