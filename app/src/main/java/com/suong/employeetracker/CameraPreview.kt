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


/**
 * Created by Asus on 10/22/2017.
 */

open class CameraPreview(context: Context, private var mCamera: Camera) : SurfaceView(context), SurfaceHolder.Callback {
    private val CAMERA_PARAM_ORIENTATION = "orientation"
    private val CAMERA_PARAM_LANDSCAPE = "landscape"
    private val CAMERA_PARAM_PORTRAIT = "portrait"
    private val mHolder: SurfaceHolder

    init {

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = holder
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder)
            setCameraPrams()
            mCamera.startPreview()
        } catch (e: IOException) {
            Log.d(TAG, "Error setting camera preview: " + e.message)
        }

    }

    fun setCameraPrams() {
        val parameters = mCamera.parameters
        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        mCamera.parameters = parameters
        mCamera.parameters.supportedPictureSizes

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
            val portrait = isPortrait()
            configureCameraParameters(cameraParams, portrait)
            mCamera.setPreviewDisplay(mHolder)
            mCamera.startPreview()

        } catch (e: Exception) {

            Log.d("VIEW_LOG_TAG", "Error starting camera preview: " + e.message)

        }

    }

    @SuppressLint("ObsoleteSdkInt")
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
    }

    fun setCamera(camera: Camera) {

        //method to set a camera instance
        mCamera = camera

    }

    fun isPortrait(): Boolean {
        return context.resources.configuration.orientation === Configuration.ORIENTATION_PORTRAIT
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mCamera.stopPreview();
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

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
        try {
            mCamera.setPreviewDisplay(mHolder)
            mCamera.startPreview()

        } catch (e: Exception) {
            Log.d(TAG, "Error starting camera preview: " + e.message)
        }

    }
}