package com.neil.reportph

import android.util.Log

class Logger {
    companion object {
        private const val APP_TAG = "ReportPH"

        fun d(tag: String, text: String) {
            Log.d(APP_TAG, "$tag : $text")
        }

        fun i(tag: String, text: String) {
            Log.i(APP_TAG, "$tag : $text")
        }

        fun w(tag: String, text: String, error: Throwable) {
            Log.w(APP_TAG, "$tag : $text", error)
        }
    }
}