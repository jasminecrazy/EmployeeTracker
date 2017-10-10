package com.suong.inf

import com.suong.model.Employee
import com.suong.model.Result123
import io.reactivex.Observable
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Created by Thu Suong on 10/8/2017.
 */
interface IEmployee {
    @POST("admin/api/login")
    fun login(@Body employee: Employee):Observable<Result123>

    companion object Factory {
        fun create(): IEmployee {
            val okhttp= OkHttpClient.Builder()
                    .connectTimeout(60,TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60,TimeUnit.SECONDS)
                    .build()
            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okhttp)
                    .baseUrl("https://trackemployee.herokuapp.com/")
                    .build()
            return retrofit.create(IEmployee::class.java)
        }
    }
}