package com.suong.employeetracker

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_absence.view.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap.CompressFormat
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.nbhung.testcallapi.DateOfDate
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
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
class DayOff : Fragment() {
    val CAMERA_REQUEST_CODE = 110
    lateinit var imageFilePath: String
    private lateinit var imageUri: Uri
    private lateinit var imageViewss: ImageView
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var idImg: String
    private lateinit var file: Uri
    private lateinit var mCurrentPhotoPath: String
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.fragment_absence, container, false)
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        imageViewss = view.findViewById(R.id.image)
        view.btnSend.setOnClickListener {
            sendPhoto()
        }
        view.btnTakeAphoto.setOnClickListener {
            takeApicture()
        }

        return view
    }

    fun sendPhoto() {
       // sendLocation()
        updateImageToFirebase()
    }

    fun takeApicture() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        file= Uri.fromFile(getFile())
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,file)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    fun getFile(): File {
        var folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"CameraDemo")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE ) {
//                val bitmap: Bitmap = data.extras.get("data") as Bitmap
//                imageViewss.setImageBitmap(bitmap)
                val options = BitmapFactory.Options()
              val  bm:Bitmap=BitmapFactory.decodeFile(file.path,options)
                Log.e("jalsl",file.path)
                imageViewss.setImageBitmap(bm)
            }
        }

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
    }


    //gửi lat long api
    fun sendLocation() {


//        val imageBytes = Base64.decode("[B@8b080ff", 0)
//        val bm:Bitmap=BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.size)
//        imageViewss.setImageBitmap(getImage("[B@8b080ff".toByteArray()))
//        var drawa: BitmapDrawable = imageViewss.drawable as BitmapDrawable
//        var bm: Bitmap = drawa.bitmap
//        val b = getBytes(bm)
//        Log.e("date of date", b.toString())
//        SharedPreferencesManager.setImageString(activity, b.toString())


//        val response = IEmployee
//        val userLogin = com.suong.model.sendLocation(sendEmployeess(SharedPreferencesManager.getIdUser(activity)!!.toInt()), 107.6457984, 10.7942232, "My location", DateOfDate.getTimeGloba(), DateOfDate.getDay(),null)
//        response.sendLocation(userLogin)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ result ->
//                    Toast.makeText(activity, "send success", Toast.LENGTH_SHORT).show()
//                    //     Log.e("result", result.id.toString())
//
//
//                }, { error ->
//                    Log.e("error", error.message)
//                    Toast.makeText(activity, "send Failed", Toast.LENGTH_SHORT).show()
//                })
    }

    fun getImage(image: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(image, 0, image.size)
    }

    fun getBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }
}