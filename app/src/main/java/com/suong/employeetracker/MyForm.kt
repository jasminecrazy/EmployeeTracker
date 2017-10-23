package com.suong.employeetracker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import com.suong.adapter.itemListViewAdapter
import com.suong.model.ResponseAbsenceForm
import com.suong.model.SharedPreferencesManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_myform.view.*

/**
 * Created by Thu Suong on 10/21/2017.
 */
class MyForm : Fragment() {
    private var adapter:itemListViewAdapter?= null
    private var mList: MutableList<ResponseAbsenceForm> = ArrayList<ResponseAbsenceForm>()
    val IEmployee by lazy {
        com.suong.Api.ApiApp.create()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = layoutInflater.inflate(R.layout.fragment_myform, container, false)
       // getListAbsence()
        mList.add(ResponseAbsenceForm(1,"20-17-10","dau bung",true))
        mList.add(ResponseAbsenceForm(2,"20-17-10","dau chan",true))
        mList.add(ResponseAbsenceForm(3,"20-17-10","dau tay",false))
        adapter=itemListViewAdapter(activity,mList)
        view.myListView.adapter=adapter
        return view
    }

    fun getListAbsence() {
        Log.e("idUser",SharedPreferencesManager.getIdUser(activity))
        IEmployee.getListDayAbsence(SharedPreferencesManager.getIdUser(activity)!!)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()

        }, { error ->
                    Log.e("error", error.message)
                    Toast.makeText(activity, " Failed", Toast.LENGTH_SHORT).show()
                })
    }
}