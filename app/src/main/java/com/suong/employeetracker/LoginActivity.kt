package com.suong.employeetracker

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.view.KeyEventCompat.dispatch
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.ListenerService
import com.cloudinary.utils.ObjectUtils
import com.example.nbhung.testcallapi.DateOfDate
import com.suong.model.Employee
import com.suong.model.SharedPreferencesManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var dialog: ProgressDialog
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }
    private lateinit var clouDinary: Cloudinary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        dialog = ProgressDialog(this)
        dialog.setMessage("Please wait")
        dialog.setTitle("Loading")
        dialog.setCancelable(false)
        var config = HashMap<String, String>()
        config.put("cloud_name", "hcm-city")
        config.put("api_key", "656797319255918")
        config.put("api_secret", "ZkYkWoNlLWDBBcxM_O_0tcoRqgY")
        MediaManager.init(this, config)
        Log.e("get current time", DateOfDate.getTimeNow())
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA), 123)
        btn_login.setOnClickListener {
            MediaManager.get().upload(R.drawable.myavatar3)
                    .option("public_id", "myAvatar232356")
                    .option("invalidate", true)
                    .callback(object : ListenerService() {
                        override fun onStart(requestId: String?) {

                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        }


                        override fun onBind(intent: Intent?): IBinder? {
                            return null
                            //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {


                        }

                        override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                            ShoDialog()
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {

                        }

                    }).dispatch()

            /*   if (Utils.isNetWorkConnnected(applicationContext)) {
                   if (edt_name != null && edt_password != null) {
                       callApi(edt_name.text.toString(), edt_password.text.toString())
                       dialog.show()
                   } else {
                       Toast.makeText(applicationContext, "wrong pass or email", Toast.LENGTH_SHORT).show()
                   }
               } else {
                   Toast.makeText(applicationContext, "no connect internet", Toast.LENGTH_SHORT).show()
               }*/


        }

    }

    fun callApi(user: String, pass: String) {
        val response = IEmployee
        val userLogin = Employee(user, pass)
        response.login(userLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    //  Toast.makeText(applicationContext, "Login Success", Toast.LENGTH_SHORT).show()
                    //     Log.e("result", result.id.toString())
                    SharedPreferencesManager.setIdUser(applicationContext, result.id.toString())
                    dialog.dismiss()

                    startActivity()
                    dialog.dismiss()

                }, { error ->
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                })
    }

    private fun ShoDialog() {
        Toast.makeText(this, "Toast", Toast.LENGTH_SHORT).show()
    }

    fun startActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }


}