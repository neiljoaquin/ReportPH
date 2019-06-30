package com.neil.reportph.recaptcha

import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.QueryMap
import retrofit2.http.POST


interface RecaptchaVerificationService {
    @Headers("Content-Type: application/x-www-form-urlencoded; charset=utf-8")
    @POST("/recaptcha/api/siteverify")
    fun verifyResponse(@QueryMap params: Map<String, String>): Call<RecaptchaResponse>
}