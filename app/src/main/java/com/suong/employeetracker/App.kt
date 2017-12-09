package com.suong.employeetracker

import android.app.Application
import android.support.v7.app.AppCompatActivity
import com.cloudinary.android.MediaManager

/**
 * Created by Billy on 12/9/2017.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        var config = HashMap<String, String>()
        config.put("cloud_name", "hcm-city")
        config.put("api_key", "656797319255918")
        config.put("api_secret", "ZkYkWoNlLWDBBcxM_O_0tcoRqgY")
        MediaManager.init(this, config)
    }
}