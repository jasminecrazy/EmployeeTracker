package com.suong.model

/**
 * Created by Asus on 10/16/2017.
 */
data class sendAbsenseToSever(val employee:sendEmployeess,
                              val shiftwork: ShiftWork,
                              val reason:String,
                              val sendDate:String,
                              val timeSend:String?,
                              val status:Boolean)