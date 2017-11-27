@file:Suppress("SENSELESS_COMPARISON")

package com.suong.employeetracker


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.Camera
import android.hardware.camera2.*
import android.location.Location
import android.location.LocationManager
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.ListenerService
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
import com.suong.model.OnclickFinish
import com.suong.model.SharedPreferencesManager
import com.suong.model.sendEmployeess
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_absence.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


@Suppress("DEPRECATION")
/**
 * Created by Thu Suong on 10/6/2017.
 */
class DayOff : Fragment(), OnMapReadyCallback, OnclickFinish {
    override fun finish(str: File) {
        sendPhoto(str)
    }

    private lateinit var dialog: ProgressDialog
    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null
    private lateinit var mMap: GoogleMap
    private var locationManager: LocationManager? = null
    private lateinit var imageUri: Uri
    private lateinit var imageViewss: ImageView
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var idImg: String
    private var location: Location? = null
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }
    //camera api 2/////////////////////////////////////////////////

    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null
    private var mCameraId: String? = null
    private var textureView: TextureView? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewSize: Size
    private var mImageReader: ImageReader? = null

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {

        @SuppressLint("NewApi")
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            //open your camera here
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            // Transform you image captured size according to the surface width and height
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {
        }

    }
    private val mStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(camera: CameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release()
            cameraDevice = camera
            Toast.makeText(activity, "Camera open", Toast.LENGTH_LONG).show()
            //show image on view
            createCameraPreviewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            mCameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            mCameraOpenCloseLock.release()
            camera.close()
            cameraDevice = null
        }

    }
    private var mFile: File? = null
    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader -> mBackgroundHandler!!.post(ImageSaver(reader.acquireNextImage(), mFile!!, this)) }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val activity = activity
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing === CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }

                val map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue

                // For still image captures, we use the largest available size.
                val largest = Collections.max(
                        Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                        CompareSizesByArea())
                mImageReader = ImageReader.newInstance(largest.width, largest.height,
                        ImageFormat.JPEG, /*maxImages*/2)
                mImageReader!!.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler)

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation = activity.windowManager.defaultDisplay.rotation
                val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                var swappedDimensions = false
                when (displayRotation) {
                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (sensorOrientation == 90 || sensorOrientation == 270) {
                        swappedDimensions = true
                    }
                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (sensorOrientation == 0 || sensorOrientation == 180) {
                        swappedDimensions = true
                    }
                    else -> Log.e(TAG, "Display rotation is invalid: " + displayRotation)
                }

                val displaySize = Point()
                activity.windowManager.defaultDisplay.getSize(displaySize)
                var rotatedPreviewWidth = width
                var rotatedPreviewHeight = height
                var maxPreviewWidth = displaySize.x
                var maxPreviewHeight = displaySize.y

                if (swappedDimensions) {
                    rotatedPreviewWidth = height
                    rotatedPreviewHeight = width
                    maxPreviewWidth = displaySize.y
                    maxPreviewHeight = displaySize.x
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                previewSize = chooseOptimalSize(map.getOutputSizes<SurfaceTexture>(SurfaceTexture::class.java),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest)

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                val orientation = resources.configuration.orientation
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    textureView!!.setAspectRatio(
//                            previewSize!!.width, previewSize!!.height)
//                } else {
//                    textureView!!.setAspectRatio(
//                            previewSize!!.height, previewSize!!.width)
//                }

                mCameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            Toast.makeText(activity, "not supported on the device", Toast.LENGTH_LONG).show()
        }

    }

    private class ImageSaver(
            /**
             * The JPEG image
             */
            private val mImage: Image,
            /**
             * The file we save the image into.
             */
            private val mFile: File,
            private val myClick: OnclickFinish) : Runnable {

        override fun run() {
            val buffer = mImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(bytes)
                Log.e("link/////", mFile.name)

            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                if (null != output) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            myClick.finish(mFile)
        }

    }

    internal class CompareSizesByArea : Comparator<Size> {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun compare(lhs: Size, rhs: Size): Int {
            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }

    }

    companion object {

        /**
         * Conversion from screen rotation to JPEG orientation.
         */
        private val ORIENTATIONS = SparseIntArray()
        private val REQUEST_CAMERA_PERMISSION = 1
        private val FRAGMENT_DIALOG = "dialog"

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        /**
         * Tag for the [Log].
         */
        private val TAG = "Camera2BasicFragment"

        /**
         * Camera state: Showing camera preview.
         */
        private val STATE_PREVIEW = 0

        /**
         * Camera state: Waiting for the focus to be locked.
         */
        private val STATE_WAITING_LOCK = 1

        /**
         * Camera state: Waiting for the exposure to be precapture state.
         */
        private val STATE_WAITING_PRECAPTURE = 2

        /**
         * Camera state: Waiting for the exposure state to be something other than precapture.
         */
        private val STATE_WAITING_NON_PRECAPTURE = 3

        /**
         * Camera state: Picture was taken.
         */
        private val STATE_PICTURE_TAKEN = 4

        /**
         * Max preview width that is guaranteed by Camera2 API
         */
        private val MAX_PREVIEW_WIDTH = 1920

        /**
         * Max preview height that is guaranteed by Camera2 API
         */
        private val MAX_PREVIEW_HEIGHT = 1080

        /**
         * Given `choices` of `Size`s supported by a camera, choose the smallest one that
         * is at least as large as the respective texture view size, and that is at most as large as the
         * respective max size, and whose aspect ratio matches with the specified value. If such size
         * doesn't exist, choose the largest one that is at most as large as the respective max size,
         * and whose aspect ratio matches with the specified value.

         * @param choices           The list of sizes that the camera supports for the intended output
         * *                          class
         * *
         * @param textureViewWidth  The width of the texture view relative to sensor coordinate
         * *
         * @param textureViewHeight The height of the texture view relative to sensor coordinate
         * *
         * @param maxWidth          The maximum width that can be chosen
         * *
         * @param maxHeight         The maximum height that can be chosen
         * *
         * @param aspectRatio       The aspect ratio
         * *
         * @return The optimal `Size`, or an arbitrary one if none were big enough
         */
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int,
                                      textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size {

            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough = ArrayList<Size>()
            // Collect the supported resolutions that are smaller than the preview Surface
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight && option.height == option.width * h / w) {
                    if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            // Pick the smallest of those big enough. If there is no one big enough, pick the
            // largest of those not big enough.
            if (bigEnough.size > 0) {
                return Collections.min(bigEnough, CompareSizesByArea())
            } else if (notBigEnough.size > 0) {
                return Collections.max(notBigEnough, CompareSizesByArea())
            } else {
                Log.e(TAG, "Couldn't find any suitable preview size")
                return choices[0]
            }
        }

//        fun newInstance(): Camera2BasicFragment {
//            return Camera2BasicFragment()
//        }
    }

    private val mCameraOpenCloseLock = Semaphore(1)
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun openCamera(width: Int, height: Int) {
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val activity = activity
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity = activity
        if (null == textureView || null == mPreview || null == activity) {
            return
        }
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, mPreview!!.height.toFloat(), mPreview!!.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
            val scale = Math.max(
                    viewHeight.toFloat() / mPreview!!.height,
                    viewWidth.toFloat() / mPreview!!.width)
            matrix.postScale(scale, scale, centerX, centerY)
            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView!!.setTransform(matrix)
    }

    fun closeCamera() {
        if (null != mCaptureSession) {
            mCaptureSession!!.close()
            mCaptureSession = null
        }
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != mImageReader) {
            mImageReader!!.close()
            mImageReader = null
        }
        mCameraOpenCloseLock.release()
    }

    private var mPreviewRequest: CaptureRequest? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    @SuppressLint("NewApi")
    private fun createCameraPreviewSession() {
        try {
            val texture = textureView!!.surfaceTexture!!

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder!!.addTarget(surface)

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice!!.createCaptureSession(Arrays.asList(surface, mImageReader!!.surface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                                // Flash is automatically enabled when necessary.
                                mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AE_MODE,
                                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder!!.build()
                                mCaptureSession!!.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler)
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }

                        }

                        override fun onConfigureFailed(
                                cameraCaptureSession: CameraCaptureSession) {
                            Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show()
                        }
                    }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }


    private var mState = STATE_PREVIEW
    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_PREVIEW -> {
                }// We have nothing to do when the camera preview is working normally.
                STATE_WAITING_LOCK -> {
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        //   captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        // val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState === CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState === CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState !== CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult) {
            process(partialResult)
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult) {
            process(result)
        }

    }

    @SuppressLint("NewApi")
    private fun captureStillPicture() {
        try {
            val activity = activity
            if (null == activity || null == cameraDevice) {
                return
            }
            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(mImageReader!!.surface)

            // Use the same AE and AF modes as the preview.
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)

            // Orientation
            val rotation = activity.windowManager.defaultDisplay.rotation
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))

            val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult) {
                    unlockFocus()
                }
            }

            mCaptureSession!!.stopRepeating()
            mCaptureSession!!.capture(captureBuilder.build(), CaptureCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun takePicture() {
        mFile = getFile()
        lockFocus()
    }

    @SuppressLint("NewApi")
    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START)
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback,
                    mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    @SuppressLint("NewApi")
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback,
                    mBackgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW
            mCaptureSession!!.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    ///////////////////////////
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.fragment_absence, container, false)
        //set setOrientationDetective

        //dialog
        dialog = ProgressDialog(activity)
        dialog.setMessage("Please wait")
        dialog.setTitle("Loading")
        dialog.setCancelable(false)
        //  dialog.show()
        //map
        locationManager = activity.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)
        //
        //fiebase
        /*    storage = FirebaseStorage.getInstance()
            storageReference = storage.reference*/
        //


        // Create an instance of Camera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //init view
            textureView = view.findViewById(R.id.tvTextTure)
            view.tvTextTure.visibility = View.VISIBLE
            view.preview.visibility = View.GONE
        } else {
            setOrientationDetective()
            mCamera = getCameraInstance()
            view.preview.visibility = View.VISIBLE
            view.tvTextTure.visibility = View.GONE
            // Create our Preview view and set it as the content of our activity.
            mPreview = CameraPreview(activity, this.mCamera!!)
            view.preview.addView(mPreview)
        }


        //
        //event
        view.btnSend.setOnClickListener {
            //take a picture
           // dialog.show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                takePicture()
            } else {
                //    mCamera!!.takePicture(null, null, mPicture)
            }
            //init cloudinary.com

            var config = HashMap<String, String>()
            config.put("cloud_name", "hcm-city")
            config.put("api_key", "656797319255918")
            config.put("api_secret", "ZkYkWoNlLWDBBcxM_O_0tcoRqgY")
            MediaManager.init(activity, config)
        }
        return view
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onResume() {
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera(textureView!!.width, textureView!!.height)
        } else {
            textureView!!.surfaceTextureListener = mSurfaceTextureListener
        }
        super.onResume()

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return
        }
        if (dialog.isShowing) {
            dialog.dismiss()
        }
        mMap.isMyLocationEnabled = true
        if (!checkGps() || !Utils.isNetWorkConnnected(activity)) {
            Toast.makeText(activity, "You need enable GPS and Internet", Toast.LENGTH_SHORT).show()
        } else {
            location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            moveLocation(location!!)
        }
    }

    private fun setOrientationDetective() {
        val orientationDetective = object : OrientationDetective(activity) {
            override fun onSimpleOrientationChanged(orientation: Int) {
                if (mPreview != null && mCamera != null) {
                    // this send rotation count to camera surface object
                    mPreview!!.setOrientationDetective(orientation)
                    // then call to reset param to Camera object
                    mPreview!!.refreshCamera(mCamera!!)

                    mPreview!!.setDefaultOrientation(getDeviceDefaultOrientation())

                }
            }
        }
        orientationDetective.enable()
    }

    fun getDeviceDefaultOrientation(): Int {
        var windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var config: Configuration = resources.configuration
        var rotation = windowManager.defaultDisplay.rotation
        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            Log.e("rotation", "Landscape")
            return Configuration.ORIENTATION_LANDSCAPE

        } else {
            Log.e("rotation", "Portrait")
            return Configuration.ORIENTATION_PORTRAIT
        }
    }

    fun sendPhoto(str: File) {
        // sendLocation()
        /*  imageUri = Uri.fromFile(mFile)
          var filess = File(imageUri.toString())*/
        imageUri = Uri.fromFile(str)
   /*     MediaManager.get().upload(imageUri)
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
                        Log.e("onReschedule","onReschedule")

                    }

                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                        Log.e("requestId",requestId)
                        Log.e("requestId",resultData.toString())
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.e("onError","onError")

                    }

                }).dispatch()*/
        MediaManager.get().upload(imageUri)
                .option("3131313", "myAvatar2")

        /*      if (!checkGps() || !Utils.isNetWorkConnnected(activity)) {
                  Toast.makeText(activity, "You need enable GPS and Internet", Toast.LENGTH_SHORT).show()
              } else updateLocation()*/


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

    /* private val mPicture: Camera.PictureCallback = Camera.PictureCallback(
             { bytes: ByteArray, camera: Camera ->
                 val fileTam: File = getFile()
                 imageUri = Uri.fromFile(fileTam)
                 var filess = File(imageUri.toString())
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
 */
    @SuppressLint("SimpleDateFormat")
    fun getFile(): File {
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/CameraDemo")
        if (!folder.exists()) {
            folder.mkdir()
        }


        val mediaFile = File(String.format(folder.toString() + File.separator + "%d.jpg", System.currentTimeMillis()))

        return mediaFile
    }

    fun checkGps(): Boolean {
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun moveLocation(locationMap: Location) {
        mMap.clear()
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(locationMap.latitude, locationMap.longitude), 15f))
        val latlng: LatLng = LatLng(locationMap.latitude, locationMap.longitude)
        mMap.addMarker(MarkerOptions().title(Utils.convertAddr(latlng, activity)).position(LatLng(locationMap.latitude, locationMap.longitude))).showInfoWindow()


    }

    /* fun updateImageToFirebase() {

         idImg = "user" + UUID.randomUUID()
         var ref: StorageReference = storageReference.child("imgUser/" + idImg)
         ref.putFile(imageUri).addOnSuccessListener {
             Log.e("update", "Success")
         }
                 .addOnFailureListener(OnFailureListener {
                     Log.e("update", "failed")
                 })
         updateLocation()
     }
 */

    fun sendLocation() {

        var myAdd: String = Utils.convertAddr(LatLng(location!!.latitude, location!!.longitude), activity)
        Log.e("address", myAdd)
        val response = IEmployee
        val id: Int = SharedPreferencesManager.getIdUser(activity)!!.toInt()
        val userLogin = com.suong.model.sendLocation(sendEmployeess(id), location!!.longitude, location!!.latitude, myAdd, DateOfDate.getTimeGloba(), DateOfDate.getDay(), "imgUser/" + idImg)
        response.sendLocation(userLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Toast.makeText(activity, "send success", Toast.LENGTH_SHORT).show()
                    Log.e("send", "success")
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }


                }, { error ->
                    Log.e("error", error.message)
                    Toast.makeText(activity, "send Failed", Toast.LENGTH_SHORT).show()
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                })
        //   refreshCamera()
    }

    fun refreshCamera() {
        mPreview!!.refreshCamera(mCamera!!)
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

    override fun onDestroy() {
        super.onDestroy()
        if (mCamera != null) {
            mCamera!!.release()
            mCamera = null
        }
    }

    override fun onStop() {
        closeCamera()
        stopBackgroundThread()
        super.onStop()
    }

    override fun onPause() {

        super.onPause()
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

}