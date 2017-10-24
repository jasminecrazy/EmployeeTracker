@file:Suppress("SENSELESS_COMPARISON")

package com.suong.employeetracker

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.nbhung.testcallapi.DateOfDate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.suong.model.SharedPreferencesManager
import com.suong.model.sendEmployeess
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_absence.view.*
import java.io.File
import java.io.FileOutputStream
import java.util.*


@Suppress("DEPRECATION")
/**
 * Created by Thu Suong on 10/6/2017.
 */
class DayOff : Fragment(), OnMapReadyCallback {
    private lateinit var dialog: ProgressDialog
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private lateinit var mMap: GoogleMap
    private var locationManager: LocationManager? = null
    val CAMERA_REQUEST_CODE = 110
    lateinit var imageFilePath: String
    private lateinit var imageUri: Uri
    private lateinit var imageViewss: ImageView
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var idImg: String
    private lateinit var mCurrentPhotoPath: String
    private var location: Location? = null
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.fragment_absence, container, false)
        //dialog
        dialog = ProgressDialog(activity)
        dialog.setMessage("Please wait")
        dialog.setTitle("Loading")
        dialog.setCancelable(false)
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
        // Create an instance of Camera
        mCamera = getCameraInstance()

        // Create our Preview view and set it as the content of our activity.
        mPreview = CameraPreview(activity, this.mCamera!!)
        view.preview.addView(mPreview)
        //
        //event
        view.btnSend.setOnClickListener {
            //take a picture
           // dialog.show()
            mCamera!!.takePicture(null, null, mPicture)

        }
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

    /** Check if this device has a camera  */
    private fun checkCameraHardware(context: Context): Boolean {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
    }

    /** A safe way to get an instance of the Camera object.  */
    fun getCameraInstance(): Camera? {
        var c: Camera? = null
        try {
            c = Camera.open() // attempt to get a Camera instance
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
        }

        return c // returns null if camera is unavailable
    }

    private val mPicture: Camera.PictureCallback = Camera.PictureCallback(
            { bytes: ByteArray, camera: Camera ->
                val fileTam: File = getFile()
                imageUri = Uri.fromFile(fileTam)
                var filess= File(imageUri.toString())
                Log.e("link", imageUri.path)
                if (fileTam == null) {
                    Log.d(TAG, "Error creating media file, check storage permissions")
                }
                val outPut = FileOutputStream(fileTam)
                outPut.write(bytes)
                outPut.close()
                sendPhoto()
            }

    )

    @SuppressLint("SimpleDateFormat")
    fun getFile(): File {
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraDemo")
        if (!folder.exists()) {
            folder.mkdir()
        }

//        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        val imageFileName = "JPEG_" + timeStamp + "_"
//        var image_file: File? = null
//        try {
//            image_file = File.createTempFile(imageFileName, ".jpg", folder)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        mCurrentPhotoPath = image_file!!.absolutePath

        val mediaFile = File(String.format(folder.toString() + File.separator + "%d.jpg", System.currentTimeMillis()))

        return mediaFile
    }

    fun checkGps(): Boolean {
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun moveLocation(locationMap: Location) {

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationMap.latitude, locationMap.longitude), 15f))
        val latlng: LatLng = LatLng(locationMap.latitude, locationMap.longitude)
        mMap.addMarker(MarkerOptions().title(Utils.convertAddr(latlng, activity)).position(LatLng(locationMap.latitude, locationMap.longitude))).showInfoWindow()


    }

    fun updateImageToFirebase() {

        idImg = "user" + UUID.randomUUID()
        var ref: StorageReference = storageReference.child("imgUser/" + idImg)
        ref.putFile(imageUri!!).addOnSuccessListener {
            Log.e("update", "Success")
        }
                .addOnFailureListener(OnFailureListener {
                    Log.e("update", "failed")
                })
        updateLocation()
    }


    fun sendLocation() {

        var myAdd: String = Utils.convertAddr(LatLng(location!!.latitude, location!!.longitude), activity)
        Log.e("address", myAdd)
        val response = IEmployee
        val userLogin = com.suong.model.sendLocation(sendEmployeess(SharedPreferencesManager.getIdUser(activity)!!.toInt()), location!!.longitude, location!!.latitude, myAdd, DateOfDate.getTimeGloba(), DateOfDate.getDay(), "imgUser/" + idImg)
        response.sendLocation(userLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Toast.makeText(activity, "send success", Toast.LENGTH_SHORT).show()
                    //     Log.e("result", result.id.toString())
                    dialog.dismiss()


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