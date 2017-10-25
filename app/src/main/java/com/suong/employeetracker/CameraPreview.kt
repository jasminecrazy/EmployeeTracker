package com.suong.employeetracker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Configuration
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.Surface.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException
import android.hardware.Camera.getCameraInfo




/**
 * Created by Asus on 10/22/2017.
 */

open class CameraPreview(context: Context, private var mCamera: Camera) : SurfaceView(context), SurfaceHolder.Callback {
    private val CAMERA_PARAM_ORIENTATION = "orientation"
    private val CAMERA_PARAM_LANDSCAPE = "landscape"
    private val CAMERA_PARAM_PORTRAIT = "portrait"
    private val mHolder: SurfaceHolder
    private var mOrientationDetective: Int = 0
    private var defaultOrientation: Int = 0
    private var mCameraId: Int = 0
    fun getOrientationDetective(): Int {
        return mOrientationDetective
    }

    fun setOrientationDetective(mOrientationDetective: Int) {
        this.mOrientationDetective = mOrientationDetective
    }

    fun getDefaultOrientation(): Int {
        return defaultOrientation
    }

    fun setDefaultOrientation(defaultOrientation: Int) {
        this.defaultOrientation = defaultOrientation
    }

    init {

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = this.holder
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder)
            refreshCamera(mCamera)
            mCamera.startPreview()
        } catch (e: IOException) {
            Log.d(TAG, "Error setting camera preview: " + e.message)
        }

    }


    fun refreshCamera(camera: Camera) {

        if (mHolder.surface == null) {

            // preview surface does not exist

            return

        }

        // stop preview before making changes

        try {

            mCamera.stopPreview()

        } catch (e: Exception) {

            // ignore: tried to stop a non-existent preview

        }

        // set preview size and make any resize, rotate or

        // reformatting changes here

        // start preview with new settings

        setCamera(camera)

        try {
            val cameraParams = mCamera.parameters
            /*boolean portrait = isPortrait();
            configureCameraParameters(cameraParams, portrait);*/

            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(mCameraId, cameraInfo)
            if (getDefaultOrientation() === Configuration.ORIENTATION_LANDSCAPE) {
                //Default lanscape: SamSung Tab: 0 but must set display orientation = 90
                mCamera.setDisplayOrientation((cameraInfo.orientation + 90) % 360)
            } else {
                //Default portrait: SamSung Phone: 90, Nexus 5: 270
                mCamera.setDisplayOrientation(cameraInfo.orientation)
            }

            cameraParams.setRotation(getImageRotation(cameraInfo))
            mCamera.parameters = cameraParams
            ///
            mCamera.setPreviewDisplay(holder)
            mCamera.startPreview()
        } catch (e: Exception) {

            Log.d("VIEW_LOG_TAG", "Error starting camera preview: " + e.message)

        }

    }

    private fun getImageRotation(cameraInfo: Camera.CameraInfo): Int {
        // set Image rotate angle when camera auto rotate
        var imageRotate: Int

        /*LogUtil.e(TAG, "getImageRotationFrontCamera", getOrientationDetective() + " " + cameraInfo.orientation);*/
        when (getOrientationDetective()) {
            0 -> imageRotate = cameraInfo.orientation
            1 -> imageRotate = (cameraInfo.orientation + 90) % 360
            2 -> imageRotate = (cameraInfo.orientation + 180) % 360
            else -> imageRotate = (cameraInfo.orientation + 270) % 360
        }
        if (mCameraId !== Camera.CameraInfo.CAMERA_FACING_BACK) {
            imageRotate -= 360
        }
        if (imageRotate < 0) {
            imageRotate *= -1
        }
        return imageRotate
    }

  /*  @SuppressLint("ObsoleteSdkInt")
    protected fun configureCameraParameters(cameraParams: Camera.Parameters, portrait: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) { // for 2.1 and before
            if (portrait) {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT)
            } else {
                cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE)
            }
        } else { // for 2.2 and later
            val angle: Int
            var activity: Activity = context as Activity
            val display = activity.windowManager.defaultDisplay
            angle = when (display.rotation) {
                ROTATION_0 -> 90 // This is camera orientation
                ROTATION_90 -> 0
                ROTATION_180 -> 270
                ROTATION_270 -> 180
                else -> 90
            }
            Log.v("LOG_TAG", "angle: " + angle)
            mCamera.setDisplayOrientation(angle)
        }
    }*/

    fun setCamera(camera: Camera) {

        //method to set a camera instance
        mCamera = camera

    }


    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mCamera.stopPreview();
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        try {
            mCamera.setPreviewDisplay(holder)
            mCamera.startPreview()
        } catch (e: Exception) {
            // intentionally left blank for a test
        }

    }
}