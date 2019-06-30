package com.neil.reportph.recaptcha

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.TaskExecutors
import com.neil.reportph.BuildConfig
import com.neil.reportph.Logger
import io.reactivex.Single
import io.reactivex.SingleEmitter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RecaptchaVerification: OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>,
        OnFailureListener {
    companion object{
        val TAG = "RecaptchaVerification"
        val GOOGLE_URL = "https://www.google.com/"
        val RESPONSE = "response"
        val SECRET = "secret"
    }
    private var emitter: SingleEmitter<Boolean>? = null

    fun getObservable(): Single<Boolean> {
        return Single.create<Boolean>{emitter -> this.emitter = emitter }
    }

    fun startRecaptchaTest(context: Context) {
        Logger.i(TAG, "Start captcha verification")
        SafetyNet.getClient(context).verifyWithRecaptcha(BuildConfig.ReportPH_Recaptcha_Site_Key)
            .addOnSuccessListener(TaskExecutors.MAIN_THREAD, this)
            .addOnFailureListener(TaskExecutors.MAIN_THREAD, this)
    }


    fun startRecaptchaVerification(response: String) {
        Logger.i(TAG, "Start captcha verification via site")
        val params = HashMap<String, String>()
        params.put(RESPONSE, response)
        params.put(SECRET, BuildConfig.ReportPH_Recaptcha_Secret_Key)

        val retrofit = Retrofit.Builder()
            .baseUrl(GOOGLE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val verifyObject = retrofit.create(RecaptchaVerificationService::class.java)
        val callVerify = verifyObject.verifyResponse(params)
        callVerify.enqueue(object: Callback<RecaptchaResponse>{
            override fun onResponse(call: Call<RecaptchaResponse>,
                                    response: Response<RecaptchaResponse>) {
                if(response.isSuccessful){
                    emitter?.onSuccess(true)
                    Logger.d(TAG, "Success captcha!")

                } else {
                    emitter?.onSuccess(false)
                    Logger.d(TAG, "$response.errorBody()")
                }
            }

            override fun onFailure(call: Call<RecaptchaResponse>, t: Throwable) {
                emitter?.onSuccess(false)
                Logger.w(TAG, "Error", t)
            }
        })
    }

    //startRecaptchaTest results
    override fun onSuccess(response: SafetyNetApi.RecaptchaTokenResponse) {
        if (response.tokenResult?.isNotEmpty() == true) {
            // Validate the user response token using the
            // reCAPTCHA site verify API.
            Logger.i(TAG, "Success initial captcha verification")
            startRecaptchaVerification(response.tokenResult)
        }
    }

    override fun onFailure(e: Exception) {
        emitter?.onSuccess(false)
        if (e is ApiException) {
            // An error occurred when communicating with the
            // reCAPTCHA service. Refer to the status code to
            // handle the error appropriately.
            Logger.d(TAG, "Error: ${CommonStatusCodes.getStatusCodeString(e.statusCode)}")
        } else {
            // A different, unknown type of error occurred.
            Logger.d(TAG, "Error: ${e.message}")
        }
    }
    //startRecaptchaTest results

}