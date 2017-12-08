package com.suong.model

import android.content.Context
import android.content.SharedPreferences



class SharedPreferencesManager {
    companion object {
        private val APP_SETTINGS = "APP_GETLOCATION"
        // properties
        private val IDUSER = "ID_USER"
        private val STRING_IMAGE = "STRING_IMAGE"
        private val STRING_EMployeeId = "STRING_EMPLOYEEID"
        // other properties...

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
        }

        fun getIdUser(context: Context): String? {
            return getSharedPreferences(context).getString(IDUSER, null)
        }

        fun setIdUser(context: Context, newValue: String) {
            val editor = getSharedPreferences(context).edit()
            editor.putString(IDUSER, newValue)
            editor.apply()
        }

        fun setImageString(context: Context, str: String) {
            val editor = getSharedPreferences(context).edit()
            editor.putString(STRING_IMAGE, str)
            editor.apply()
        }

        fun getStringImage(context: Context): String {
            return getSharedPreferences(context).getString(STRING_IMAGE, null)
        }

        fun setEmployeeId(context: Context, empId: String) {
            val editor = getSharedPreferences(context).edit()
            editor.putString(STRING_EMployeeId, empId)
            editor.apply()
        }
        fun getEmployId(context: Context):String{
            return getSharedPreferences(context).getString(STRING_EMployeeId, null)
        }

    }
}