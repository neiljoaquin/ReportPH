package com.neil.reportph.firebase

import android.content.Context
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.neil.reportph.Logger
import com.neil.reportph.models.Reports
import java.util.concurrent.TimeUnit


class UserVerifier(context: Context, report: Reports) : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
    private val TAG = "UserVerifier"
    private val TIMEOUT_DURATION = 60L
    private val context = context
    private val report = report
    private val MAX_TRY = 3
    private var try_counter = 0
    private lateinit var phoneNumber: String
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        // This callback will be invoked in two situations:
        // 1 - Instant verification. In some cases the phone number can be instantly
        //     verified without needing to send or enter a verification code.
        // 2 - Auto-retrieval. On some devices Google Play services can automatically
        //     detect the incoming verification SMS and perform verification without
        //     user action.
        Logger.d(TAG, "onVerificationCompleted:$credential")
        FirebaseStorage().addReportToFireStore(report)
    }

    override fun onVerificationFailed(e: FirebaseException) {
        // This callback is invoked in an invalid request for verification is made,
        // for instance if the the phone number format is not valid.
        Logger.d(TAG, "onVerificationFailed " + e)
        //TODO add error screen


        // Show a message and update the UI
        // ...
    }

    override fun onCodeSent(verificationId: String?,
        token: PhoneAuthProvider.ForceResendingToken) {
        // The SMS verification code has been sent to the provided phone number, we
        // now need to ask the user to enter the code and then construct a credential
        // by combining the code with a verification ID.
        Logger.d(TAG, "onCodeSent:" + verificationId!!)

        // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        // ...
    }

    override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
        Logger.d(TAG, "Time out")
        if(try_counter < MAX_TRY) {
            Logger.d(TAG, "resend verification code")
            resendVerificationCode(phoneNumber)
            try_counter++
        } else {
            //TODO finish activity and toast Time out! Was not able to receive code
        }
    }

    fun resendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            TIMEOUT_DURATION, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            TaskExecutors.MAIN_THREAD, // Activity (for callback binding)
            this, // OnVerificationStateChangedCallbacks
            resendToken
        )             // ForceResendingToken from callbacks
    }

    fun textUserWithCode(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,      // Phone number to verify
            TIMEOUT_DURATION,               // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            TaskExecutors.MAIN_THREAD,             // Activity (for callback binding)
            this
        ) // OnVerificationStateChangedCallbacks
    }
}