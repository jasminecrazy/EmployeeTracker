package com.suong.model

/**
 * Created by Asus on 10/11/2017.
 */
data class sendLocation(
        val employee:sendEmployeess,
        val longtitude:Double,
        val latitude:Double,
        val locationName:String,
        val locationTime:String,
        val date:String
)
