package com.suong.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.suong.employeetracker.R
import com.suong.model.ResponseAbsenceForm

/**
 * Created by Asus on 10/22/2017.
 */
class itemListViewAdapter(private val context: Context, list: MutableList<ResponseAbsenceForm>) : BaseAdapter() {
    private var list = ArrayList<ResponseAbsenceForm>()

    init {
        this.list = list as ArrayList<ResponseAbsenceForm>
    }
    @SuppressLint("InflateParams", "ViewHolder")
    override fun getView(i: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
       val vi:View = inflater.inflate(R.layout.item_listview, null)
        val lyDo:TextView = vi.findViewById(R.id.tvLyDoXinNghi)
        val ngayGui:TextView = vi.findViewById(R.id.tvNgayGui)
        val trangThai:TextView = vi.findViewById(R.id.tvTrangThai)
        lyDo.text = list[i].reason
        ngayGui.text = list[i].sendDate
        if(list[i].status){
            trangThai.text ="Đã Duyệt"
            trangThai.setTextColor(Color.GREEN)
        }else{
            trangThai.text ="Chưa Duyệt"
            trangThai.setTextColor(Color.RED)
        }

        return vi
    }

    override fun getItem(p0: Int): Any {
       return p0
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
       return  list.size
    }
}