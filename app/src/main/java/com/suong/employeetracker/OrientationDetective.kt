package com.suong.employeetracker

import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_0
import android.R.attr.orientation
import android.content.Context
import android.support.v4.view.ViewCompat.getRotation
import android.content.Context.WINDOW_SERVICE
import android.content.res.Configuration
import android.view.WindowManager
import com.suong.employeetracker.R.drawable.lock
import android.view.OrientationEventListener
import android.view.Surface
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90
import java.util.concurrent.locks.ReentrantLock


/**
 * Created by nbhung on 10/25/2017.
 */
abstract class OrientationDetective(private val ctx: Context) : OrientationEventListener(ctx) {
    @Volatile private var defaultScreenOrientation = CONFIGURATION_ORIENTATION_UNDEFINED
    var prevOrientation = OrientationEventListener.ORIENTATION_UNKNOWN
    private val lock = ReentrantLock(true)

    override fun onOrientationChanged(orientation: Int) {
        var currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN
        if (orientation >= 330 || orientation < 30) {
            currentOrientation = Surface.ROTATION_0
        } else if (orientation >= 60 && orientation < 120) {
            currentOrientation = Surface.ROTATION_90
        } else if (orientation >= 150 && orientation < 210) {
            currentOrientation = Surface.ROTATION_180
        } else if (orientation >= 240 && orientation < 300) {
            currentOrientation = Surface.ROTATION_270
        }

        if (prevOrientation != currentOrientation && orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            prevOrientation = currentOrientation
            if (currentOrientation != OrientationEventListener.ORIENTATION_UNKNOWN)
                reportOrientationChanged(currentOrientation)
        }

    }

    private fun reportOrientationChanged(currentOrientation: Int) {
        onSimpleOrientationChanged(currentOrientation)
    }

    /**
     * Must determine what is default device orientation (some tablets can have default landscape). Must be initialized when device orientation is defined.

     * @return value of [Configuration.ORIENTATION_LANDSCAPE] or [Configuration.ORIENTATION_PORTRAIT]
     */
    val deviceDefaultOrientation: Int
        get() {
            if (defaultScreenOrientation == CONFIGURATION_ORIENTATION_UNDEFINED) {
                lock.lock()
                defaultScreenOrientation = initDeviceDefaultOrientation(ctx)
                lock.unlock()
            }
            return defaultScreenOrientation
        }

    /**
     * Provides device default orientation

     * @return value of [Configuration.ORIENTATION_LANDSCAPE] or [Configuration.ORIENTATION_PORTRAIT]
     */
    private fun initDeviceDefaultOrientation(context: Context): Int {

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val config = context.getResources().getConfiguration()
        val rotation = windowManager.defaultDisplay.rotation

        val isLand = config.orientation === Configuration.ORIENTATION_LANDSCAPE
        val isDefaultAxis = rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180

        val result: Int
        if (isDefaultAxis && isLand || !isDefaultAxis && !isLand) {
            result = Configuration.ORIENTATION_LANDSCAPE
        } else {
            result = Configuration.ORIENTATION_PORTRAIT
        }
        return result
    }

    /**
     * Fires when orientation changes from landscape to portrait and vice versa.

     * @param orientation value of [Configuration.ORIENTATION_LANDSCAPE] or [Configuration.ORIENTATION_PORTRAIT]
     */
    abstract fun onSimpleOrientationChanged(orientation: Int)

    companion object {

        val CONFIGURATION_ORIENTATION_UNDEFINED = Configuration.ORIENTATION_UNDEFINED
    }

}