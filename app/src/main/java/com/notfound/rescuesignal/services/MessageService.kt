package com.notfound.rescuesignal.services

import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Body

interface MessageService {

    @POST("/command")
    fun sendMessage(@Body text: String): Call<Response>
}

data class Response(
    val status: String
)