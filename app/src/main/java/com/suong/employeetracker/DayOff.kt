package com.suong.employeetracker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.net.URL

/**
 * Created by Thu Suong on 10/6/2017.
 */
class DayOff : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = layoutInflater.inflate(R.layout.fragment_absence,container,false)
        addControl()
        return view
    }
    fun addControl(){
         val result = URL("<api call>").readText()
    }
}