package com.suong.employeetracker

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import com.example.nbhung.testcallapi.DateOfDate
import com.suong.adapter.AdapterItemListView
import com.suong.adapter.AdapterWorkSchedule
import com.suong.model.ResponseAbsenceForm
import com.suong.model.ResponseShiftWork
import com.suong.model.SharedPreferencesManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_work_schedule.*
import kotlinx.android.synthetic.main.activity_work_schedule.view.*
import kotlinx.android.synthetic.main.fragment_comelate.*
import java.text.SimpleDateFormat
import java.util.*


class WorkSchedule : Fragment() {
    private lateinit var dialog: ProgressDialog
    private var adapterWorkSchedule: AdapterWorkSchedule? = null
    private var mList: MutableList<ResponseShiftWork> = ArrayList<ResponseShiftWork>()

    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        var view: View = layoutInflater.inflate(R.layout.activity_work_schedule, container, false)
        dialog = ProgressDialog(activity)
        dialog.setMessage("Vui lòng đợi...")
        dialog.setCancelable(false)
        view.tvChooseDate.setOnClickListener {
            openCalendaStart()
        }
        view.btnTim.setOnClickListener {
            if (view.tvChooseDate.text.toString().isEmpty()) {
                Toast.makeText(activity, "Bạn chưa chọn ngày", Toast.LENGTH_SHORT).show()
            } else {
                mList.clear()
                getlistWorkSchedule()
            }
        }

        return view
    }

    fun openCalendaStart() {
        val c = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"))
        val years = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(activity,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                    val spToday = SimpleDateFormat("yyyy-MM-dd")
                    var toDay = spToday.parse(DateOfDate.getDay())
                    var str: String = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                    var chooseDay = spToday.parse(str)
                    if (chooseDay.time >= toDay.time) {
                        tvChooseDate.text = str
                        Toast.makeText(activity, tvChooseDate.text.toString(), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "chọn ngày không hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                }, years, month, day)
        datePickerDialog.show()
    }

    private fun getlistWorkSchedule() {
        dialog.show()
        val id: Int = SharedPreferencesManager.getIdUser(activity)!!.toInt()
        IEmployee.getListWorkSheculeFromDate(tvChooseDate.text.toString(), id.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    dialog.cancel()
                    for (i in result.indices) {
                        mList.add(result.get(i).shiftwork)
                    }
                    Toast.makeText(activity, "Thành Công", Toast.LENGTH_SHORT).show()
                    if (mList.isNotEmpty()) {
                        showList()
                    } else {
                        Toast.makeText(activity, "Không Có Dữ liệu", Toast.LENGTH_SHORT).show()
                    }

                }, { error ->
                    dialog.cancel()
                    Toast.makeText(activity, "Thất Bại", Toast.LENGTH_SHORT).show()

                })
    }

    fun showList() {
        adapterWorkSchedule = AdapterWorkSchedule(activity, mList)
        view!!.findViewById<ListView>(R.id.mListView).adapter = adapterWorkSchedule
    }
}