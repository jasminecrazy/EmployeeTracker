package com.suong.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.suong.employeetracker.R
import com.suong.model.ResponseShiftWork
import com.suong.model.ResponseWorkSchedule
import java.security.AccessControlContext


class AdapterWorkSchedule(private val context: Context, list: MutableList<ResponseWorkSchedule>) : BaseAdapter() {
    private var list = ArrayList<ResponseWorkSchedule>()

    init {
        this.list = list as ArrayList<ResponseWorkSchedule>
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_work_schedule, null)
        val Title: TextView = view.findViewById(R.id.tvTitle)
        val TvStartTime: TextView = view.findViewById(R.id.tvStartTime)
        val TvEndTime: TextView = view.findViewById(R.id.tvEndTime)
        val TvLocation: TextView = view.findViewById(R.id.tvLocation)

        Title.text = list.get(position).shiftwork.shiftworkName
        TvStartTime.text = list.get(position).shiftwork.startTime
        TvEndTime.text = list.get(position).shiftwork.endTime
        TvLocation.text = list.get(position).location
        return view
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }
}