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


/**
 * Created by Billy on 11/26/2017.
 */
class Alarm : BroadcastReceiver() {
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        Toast.makeText(context, "Alarm ", Toast.LENGTH_SHORT).show()
       // sendLocation(context)
    }

    fun sendLocation(context: Context?) {
        val locationManager = context!!.getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return
        }
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val myAdd: String = Utils.convertAddr(LatLng(location!!.latitude, location!!.longitude), context)
        Log.e("address", DateOfDate.getTimeNow())
        val response = IEmployee
        val id: Int = SharedPreferencesManager.getIdUser(context)!!.toInt()
        val userLogin = com.suong.model.sendLocation(sendEmployeess(id), location!!.longitude, location!!.latitude, myAdd, DateOfDate.getTimeNow(), DateOfDate.getDay(), "")
        response.sendLocation(userLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Toast.makeText(context, "mapsacitivity send success", Toast.LENGTH_SHORT).show()


                }, { error ->
                    Toast.makeText(context, " mapsacitivity send Failed", Toast.LENGTH_SHORT).show()
                })
    }
}