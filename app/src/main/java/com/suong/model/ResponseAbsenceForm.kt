package com.suong.model


data class ResponseAbsenceForm(
        val id: Int,
        val sendDate: String,
        val reason: String,
        val status: Int,
        val lydo: String
)