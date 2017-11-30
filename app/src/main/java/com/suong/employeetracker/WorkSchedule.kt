package com.suong.employeetracker

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
import com.suong.adapter.AdapterItemListView
import com.suong.adapter.AdapterWorkSchedule
import com.suong.model.ResponseAbsenceForm
import com.suong.model.ResponseShiftWork
import com.suong.model.SharedPreferencesManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by nbhung on 11/30/2017.
 */
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
        dialog.setMessage("Please wait")
        dialog.setTitle("Loading")
        dialog.setCancelable(false)
        getlistWorkSchedule()
        return view
    }


    private fun getlistWorkSchedule() {
        dialog.show()
        IEmployee.getListWorkSchedule(SharedPreferencesManager.getIdUser(activity).toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Log.e("result", result.toString())
                    dialog.cancel()
                    for (i in result.indices) {
                        mList.add(result.get(i).shiftwork)
                    }
                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                    if (mList.isNotEmpty()) {
                        showList()
                    } else {
                        Toast.makeText(activity, "data empty", Toast.LENGTH_SHORT).show()
                    }

                }, { error ->
                    dialog.cancel()
                    Log.e("ádmjk", error.message)
                    Toast.makeText(activity, "error", Toast.LENGTH_SHORT).show()

                })
    }

    fun showList() {
        adapterWorkSchedule = AdapterWorkSchedule(activity, mList)
        view!!.findViewById<ListView>(R.id.mListView).adapter = adapterWorkSchedule
    }
}