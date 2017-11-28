package com.suong.employeetracker

import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.support.v4.view.KeyEventCompat.dispatch
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.ListenerService
import com.cloudinary.utils.ObjectUtils
import java.io.File

/**
 * Created by nbhung on 11/27/2017.
 */
class CloudinaryUpLoad(cloudinary2: Cloudinary) : AsyncTask<String, String, String>() {
    private var clouDinary: Cloudinary

    init {
        clouDinary = cloudinary2
    }

    override fun doInBackground(vararg params: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
    }
}