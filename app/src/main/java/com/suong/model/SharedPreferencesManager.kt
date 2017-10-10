package com.suong.model

import android.content.Context
import android.content.SharedPreferences



/**
 * Created by Thu Suong on 10/10/2017.
 */
class SharedPreferencesManager {
   companion object {
       private val APP_SETTINGS = "APP_GETLOCATION"
       // properties
       private val IDUSER = "ID_USER"
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

   }
}