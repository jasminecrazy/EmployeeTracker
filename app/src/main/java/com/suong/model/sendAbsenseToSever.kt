package com.suong.model


data class sendAbsenseToSever(val employee:sendEmployeess,
                              val shiftwork: ShiftWork,
                              val reason:String,
                              val sendDate:String,
                              val timeSend:String?,
                              val status:Int,
                              val fromDate:String?,
                              val toDate:String?,
                              val lydo:String?)