package com.neil.reportph.recaptcha

import com.google.gson.annotations.SerializedName

data class RecaptchaResponse(
    val success: Boolean,
    val challenge_ts: String,
    val hostname: String,
    @SerializedName("error-codes")
    val errorCodes: List<String>
)