package com.suong.model

/**
 * Created by Thu Suong on 10/21/2017.
 */
data class ResponseAbsenceForm(
        val id: Int,
        val sendDate: String,
        val reason: String,
        val status: Boolean
)