package com.suong.employeetracker

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_absence.view.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap.CompressFormat
import android.location.Location
import android.location.LocationManager
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.example.nbhung.testcallapi.DateOfDate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.suong.employeetracker.R.drawable.location
import com.suong.model.SharedPreferencesManager
import com.suong.model.sendEmployeess
import com.suong.model.sendLocation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Thu Suong on 10/6/2017.
 */
class DayOff : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var locationManager: LocationManager? = null
    val CAMERA_REQUEST_CODE = 110
    lateinit var imageFilePath: String
    private lateinit var imageUri: Uri
    private lateinit var imageViewss: ImageView
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var idImg: String
    private lateinit var file: Uri
    private lateinit var mCurrentPhotoPath: String
    private var location: Location? = null
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.fragment_absence, container, false)
        //map
        locationManager = activity.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
        //
        //fiebase
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        //

        //init view
        imageViewss = view.findViewById(R.id.image)
        //
        //event
        view.btnSend.setOnClickListener {
            sendPhoto()
        }
        imageViewss.setOnClickListener {
            takeApicture()
        }
        //


        return view
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return
        }
        mMap.isMyLocationEnabled = true
        if (!checkGps() || !Utils.isNetWorkConnnected(activity)) {
            Toast.makeText(activity, "You need enable GPS and Internet", Toast.LENGTH_SHORT).show()
        } else {
            location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            moveLocation(location!!)
        }
    }

    fun sendPhoto() {
        // sendLocation()
        if (!checkGps() || !Utils.isNetWorkConnnected(activity)) {
            Toast.makeText(activity, "You need enable GPS and Internet", Toast.LENGTH_SHORT).show()
        } else updateImageToFirebase()


    }

    fun takeApicture() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        file = Uri.fromFile(getFile())
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, file)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    @SuppressLint("SimpleDateFormat")
    fun getFile(): File {
        var folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraDemo")
        if (!folder.exists()) {
            folder.mkdir()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        var image_file: File? = null
        try {
            image_file = File.createTempFile(imageFileName, ".jpg", folder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mCurrentPhotoPath = image_file!!.absolutePath

        return image_file
    }

    fun checkGps(): Boolean {
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
//                val bitmap: Bitmap = data.extras.get("data") as Bitmap
//                imageViewss.setImageBitmap(bitmap)
                val options = BitmapFactory.Options()
                val bm: Bitmap = BitmapFactory.decodeFile(file.path, options)
                Log.e("jalsl", file.path)
                imageViewss.setImageBitmap(bm)
            }
        }

    }

    fun moveLocation(locationMap: Location) {

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationMap.latitude, locationMap.longitude), 15f))
        val latlng: LatLng = LatLng(locationMap.latitude, locationMap.longitude)
        mMap.addMarker(MarkerOptions().title(Utils.convertAddr(latlng, activity)).position(LatLng(locationMap.latitude, locationMap.longitude))).showInfoWindow()


    }

    fun updateImageToFirebase() {
        idImg = "user" + UUID.randomUUID()
        var ref: StorageReference = storageReference.child("imgUser/" + idImg)
        ref.putFile(file).addOnSuccessListener {
            Log.e("update", "Success")
        }
                .addOnFailureListener(OnFailureListener {
                    Log.e("update", "failed")
                })
        updateLocation()
    }


    fun sendLocation() {

        var myAdd: String = Utils.convertAddr(LatLng(location!!.longitude, location!!.latitude), activity)
        Log.e("address", myAdd)
        val response = IEmployee
        val userLogin = com.suong.model.sendLocation(sendEmployeess(SharedPreferencesManager.getIdUser(activity)!!.toInt()), location!!.longitude, location!!.latitude, myAdd, DateOfDate.getTimeGloba(), DateOfDate.getDay(), "imgUser/" + idImg)
        response.sendLocation(userLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Toast.makeText(activity, "send success", Toast.LENGTH_SHORT).show()
                    //     Log.e("result", result.id.toString())


                }, { error ->
                    Log.e("error", error.message)
                    Toast.makeText(activity, "send Failed", Toast.LENGTH_SHORT).show()
                })
    }

    fun updateLocation() {
        if (Utils.isNetWorkConnnected(activity)) {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        } else {
            if (checkGps()) {
                location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }


        }
        sendLocation()
    }
}