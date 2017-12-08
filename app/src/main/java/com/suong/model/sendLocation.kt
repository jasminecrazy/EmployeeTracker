package com.suong.model


data class sendLocation(
        val employee:sendEmployeess,
        val longtitude:Double,
        val latitude:Double,
        val locationName:String,
        val locationTime:String,
        val date:String,
        val picture:String?
)
