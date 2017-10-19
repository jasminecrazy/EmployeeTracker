package com.suong.employeetracker

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.util.*

/**
 * Created by Thu Suong on 10/6/2017.
 */
object Utils {
    fun isNetWorkConnnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val acNetworkInfo = cm.activeNetworkInfo
        return acNetworkInfo != null && acNetworkInfo.isConnectedOrConnecting
    }
    fun convertAddr(lat: LatLng,context: Context): String {
        var geocoder = Geocoder(context, Locale.getDefault())

        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(lat.latitude, lat.longitude, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var streetAddress: String = ""
        if (addresses != null) {
            val returnedAddress = addresses[0]
            streetAddress = returnedAddress.getAddressLine(0)

        }
        Log.e("add", streetAddress)
        return streetAddress
    }

}