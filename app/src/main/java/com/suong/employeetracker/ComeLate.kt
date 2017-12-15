package com.suong.employeetracker

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import com.example.nbhung.testcallapi.DateOfDate
import com.suong.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comelate.*
import kotlinx.android.synthetic.main.fragment_comelate.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ComeLate : Fragment(), DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {
    private lateinit var dialog: ProgressDialog
    private var listCa: List<ResponseShiftWork> = ArrayList<ResponseShiftWork>()
    private var listNameCa: MutableList<String> = ArrayList<String>()
    private var CaLam: Int = 0
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        CaLam = listCa.get(p2).id
    }


    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.fragment_comelate, container, false)
        dialog = ProgressDialog(activity)
        dialog.setMessage("Vui lòng đợi...")
        dialog.setCancelable(false)
        dialog.show()
        loadShiftWork()
        view.spinCaLam.onItemSelectedListener = this
        view.tvDateStart.setOnClickListener {
            openCalendaStart()
        }
        view.tvDateEnd.setOnClickListener {
            openCalendaEnd()
        }
        view.btnSend.setOnClickListener {
            dialog.show()
            sendAbsense()
        }
        return view
    }

    fun loadShiftWork() {
        IEmployee.getshiftwork()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    listCa = result
                    dialog.dismiss()
                    addDateToSpin()
                }, { error ->
                    dialog.dismiss()
                    Toast.makeText(activity, " Lỗi trong quá trình tải dữ liệu", Toast.LENGTH_SHORT).show()
                })
    }

    fun addDateToSpin() {
        for (i in 0..listCa.size - 1) {
            listNameCa.add(listCa.get(i).shiftworkName)
        }
        val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, listNameCa)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view!!.spinCaLam.adapter = adapter
    }

    fun openCalendaStart() {
        val c = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"))
        val years = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(activity,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    if (tvDateEnd.text.toString().equals("")) {
                        val spToday = SimpleDateFormat("yyyy-MM-dd")
                        var toDay = spToday.parse(DateOfDate.getDay())
                        var str: String = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                        var chooseDay = spToday.parse(str)
                        if (chooseDay.time >= toDay.time) {
                            tvDateStart.text = str
                        } else {
                            Toast.makeText(activity, "chọn ngày không hợp lệ", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val spToday = SimpleDateFormat("yyyy-MM-dd")
                        var dayEnd = spToday.parse(tvDateEnd.text.toString())
                        var str: String = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                        var dayStart = spToday.parse(str)
                        if (dayEnd.time >= dayStart.time) {
                            tvDateStart.text = str
                        } else {
                            Toast.makeText(activity, "cần thay đổi ngày kết thúc trước", Toast.LENGTH_SHORT).show()
                        }
                    }


                }, years, month, day)
        datePickerDialog.show()
    }

    fun openCalendaEnd() {
        val c = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"))
        val years = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(activity,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    if (tvDateStart.text.toString().equals("")) {
                        Toast.makeText(activity, "chọn ngày bắt đầu trước", Toast.LENGTH_SHORT).show()
                    } else {

                        val spToday = SimpleDateFormat("yyyy-MM-dd")
                        var dayStart = spToday.parse(tvDateStart.text.toString())
                        var str: String = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                        var dayEnd = spToday.parse(str)
                        if (dayEnd.time >= dayStart.time) {
                            tvDateEnd.text = str
                        } else {
                            Toast.makeText(activity, "chọn ngày không hợp lệ ", Toast.LENGTH_SHORT).show()


                        }

                    }
                }, years, month, day)
        datePickerDialog.show()
    }
    fun sendAbsense() {

        if (tvDateStart != null && contentReason != null && tvDateStart.text != null && contentReason.text != null) {
            val reason: String = contentReason.text.toString()
            val fromdate: String = tvDateStart.text.toString()
            val enddate: String = tvDateEnd.text.toString()
            IEmployee.sendAbsense(sendAbsenseToSever(sendEmployeess(SharedPreferencesManager.getIdUser(activity)!!.toInt()), ShiftWork(CaLam), reason, DateOfDate.getDay(), DateOfDate.getTimeGloba(), 0, fromdate, enddate,""))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        Toast.makeText(activity, "Thành Công", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }, { error ->
                        dialog.dismiss()
                        Toast.makeText(activity, " Thất Bại", Toast.LENGTH_SHORT).show()
                    })
        } else {
            Toast.makeText(activity, "Không được để trống", Toast.LENGTH_SHORT).show()
        }

    }
}


