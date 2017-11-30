package com.suong.employeetracker

import android.app.ProgressDialog
import android.content.Intent
import android.location.LocationManager
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
    private var locationManager: LocationManager? = null

    private lateinit var clouDinary: Cloudinary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        dialog = ProgressDialog(this)
        dialog.setMessage("Please wait")
        dialog.setTitle("Loading")
        dialog.setCancelable(false)
        locationManager = getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA), 123)
        btn_login.setOnClickListener {
            if (Utils.isNetWorkConnnected(applicationContext)) {
                if (checkGps()) {
                    if (edt_name != null && edt_password != null) {
                        callApi(edt_name.text.toString(), edt_password.text.toString())
                        dialog.show()
                    } else {
                        Toast.makeText(applicationContext, "wrong pass or email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(applicationContext, "please enable GPS ", Toast.LENGTH_SHORT).show()

                }

            } else {
                Toast.makeText(applicationContext, "no connect internet", Toast.LENGTH_SHORT).show()
            }

        }

    }

    fun callApi(user: String, pass: String) {

        val userLogin = Employee(user, pass)
        IEmployee.login(userLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    //  Toast.makeText(applicationContext, "Login Success", Toast.LENGTH_SHORT).show()
                         Log.e("result", result.id.toString())
                    SharedPreferencesManager.setIdUser(applicationContext, result.id.toString())
                    dialog.dismiss()
                    Toast.makeText(applicationContext, "Login Success", Toast.LENGTH_SHORT).show()

                    startActivity()
                    dialog.dismiss()

                }, { error ->
                    Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                })
    }

    fun startActivity() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
    }

    fun checkGps(): Boolean {
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

}