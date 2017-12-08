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


class AdapterWorkSchedule(private val context: Context, list: MutableList<ResponseShiftWork>) : BaseAdapter() {
    private var list = ArrayList<ResponseShiftWork>()

    init {
        this.list = list as ArrayList<ResponseShiftWork>
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.item_work_schedule, null)
        val Title: TextView = view.findViewById(R.id.tvTitle)
        val TvStartTime: TextView = view.findViewById(R.id.tvStartTime)
        val TvEndTime: TextView = view.findViewById(R.id.tvEndTime)

        Title.text = list.get(position).shiftworkName
        TvStartTime.text = list.get(position).startTime
        TvEndTime.text = list.get(position).endTime
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