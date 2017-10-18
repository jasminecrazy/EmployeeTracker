package com.suong.employeetracker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_comelate.*
import java.util.*
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.util.Log
import com.example.nbhung.testcallapi.DateOfDate
import com.suong.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comelate.view.*
import kotlin.collections.ArrayList
import android.R.array
import android.widget.*


/**
 * Created by Thu Suong on 10/6/2017.
 */
class ComeLate : Fragment(), DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {
    private lateinit var dialog: ProgressDialog
    private var listCa: List<ResponseShiftWork> = ArrayList<ResponseShiftWork>()
    private var listNameCa: MutableList<String> = ArrayList<String>()
    private var CaLam: Int = 0
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
       CaLam=listCa.get(p2).id
    }


    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var umbala: Int? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.fragment_comelate, container, false)
        dialog = ProgressDialog(activity)
        dialog.setMessage("Please wait")
        dialog.setTitle("Loading")
        dialog.setCancelable(false)
        dialog.show()
        loadShiftWork()
        view.spinCaLam.onItemSelectedListener=this
        view.tvDateStart.setOnClickListener {
            openCalenda()
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
                    Log.e("error", error.message)
                    dialog.dismiss()
                    Toast.makeText(activity, " Failed to load data", Toast.LENGTH_SHORT).show()
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

    fun openCalenda() {
        val c = Calendar.getInstance(TimeZone.getTimeZone("GMT+7:00"))
        val years = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePickerDialog = DatePickerDialog(activity,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    var str: String = year.toString() + "-" + (monthOfYear + 1) + "-" + dayOfMonth.toString()
                    tvDateStart.text = str
                }, years, month, day)
        datePickerDialog.show()
    }

    fun sendAbsense() {
        if ( tvDateStart != null && contentReason != null && tvDateStart.text != null && contentReason.text != null) {

            val reason: String = contentReason.text.toString()
            val datetime: String = DateOfDate.getTimeNow()
            val dateTimeStart: String = tvDateStart.text.toString()
            IEmployee.sendAbsense(sendAbsenseToSever(sendEmployeess(SharedPreferencesManager.getIdUser(activity)!!.toInt()), ShiftWork(CaLam), reason, dateTimeStart, DateOfDate.getTimeGloba(), false))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ result ->
                        Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }, { error ->
                        dialog.dismiss()
                        Log.e("error", error.message)
                        Toast.makeText(activity, " Failed", Toast.LENGTH_SHORT).show()
                    })
        } else {
            Toast.makeText(activity, "Can be empty", Toast.LENGTH_SHORT).show()
        }

    }
}


