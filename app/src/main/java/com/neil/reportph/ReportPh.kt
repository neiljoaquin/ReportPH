package com.neil.reportph

import android.app.Application
import com.google.firebase.FirebaseApp

class ReportPh: Application() {
    override fun onCreate() {
        super.onCreate()
        //firebase needs to initialize before using firebase apis
        FirebaseApp.initializeApp(this)
    }
}