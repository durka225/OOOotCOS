package com.notfound.rescuesignal.retrofit

import android.content.Context
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RestApi(context: Context) {
    companion object {
        private const val DEFAULT_IP = "10.173.96.203"
        private const val PORT = "80"
    }

    private val savedIP = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getString("ip_address", DEFAULT_IP) ?: DEFAULT_IP
    
    private val baseUrl = "http://$savedIP:$PORT"


    var instance: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}