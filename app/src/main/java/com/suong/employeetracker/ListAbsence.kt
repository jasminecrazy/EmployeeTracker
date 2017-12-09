package com.suong.employeetracker

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import com.suong.adapter.AdapterItemListView
import com.suong.model.ResponseAbsenceForm
import com.suong.model.SharedPreferencesManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class ListAbsence : Fragment() {
    private lateinit var dialog: ProgressDialog
    private var adapterItemListView: AdapterItemListView? = null
    private var mList: MutableList<ResponseAbsenceForm> = ArrayList<ResponseAbsenceForm>()
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.fragment_myform, container, false)
        dialog = ProgressDialog(activity)
        dialog.setMessage("Vui Lòng đợi")
        dialog.setCancelable(false)
        getListAbsence()
        return view
    }

    fun getListAbsence() {
        dialog.show()
        IEmployee.getListDayAbsence(SharedPreferencesManager.getIdUser(activity).toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    /*Log.e("result", result.size.toString())
                    Log.e("result", result.get(0).toString())
                    Log.e("result", result.get(1).toString())*/
                    Toast.makeText(activity, " Thành Công ", Toast.LENGTH_SHORT).show()
                    mList.addAll(result)
                    if (mList.size != 0) {
                        showList()
                    }


                }, { error ->
                    dialog.dismiss()
                    Toast.makeText(activity, " Thất Bại", Toast.LENGTH_SHORT).show()
                })
    }

    fun showList() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
        adapterItemListView = AdapterItemListView(activity, mList)
        view!!.findViewById<ListView>(R.id.myListView).adapter = adapterItemListView
    }
}