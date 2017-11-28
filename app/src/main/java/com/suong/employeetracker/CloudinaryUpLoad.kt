package com.suong.employeetracker

import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.ListenerService

/**
 * Created by nbhung on 11/27/2017.
 */
 class CloudinaryUpLoad : AsyncTask<String, String, String>() {

    override fun doInBackground(vararg params: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        MediaManager.get().upload(params[0])
                .option("3131313", "myAvatar2")
                .callback(object : ListenerService() {
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onStart(requestId: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onBind(intent: Intent?): IBinder {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.e("onReschedule", "onReschedule")

                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        Log.e("requestId", requestId)
                        Log.e("requestId", resultData.toString())
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("onError", "onError")

                    }

                }).dispatch()
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
    }
}