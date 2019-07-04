package com.neil.reportph.viewModels

import android.app.Activity
import android.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.neil.reportph.Constants
import com.neil.reportph.Logger
import com.neil.reportph.R
import com.neil.reportph.firebase.FirebaseStorage
import com.neil.reportph.models.Reports
import com.neil.reportph.recaptcha.RecaptchaVerification
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.schedulers.Schedulers


class ReportsViewModel: ViewModel() {
    private val TAG = "ReportsViewModel"
    val crime: MutableLiveData<String> = MutableLiveData()
    val date: MutableLiveData<String> = MutableLiveData()
    val time: MutableLiveData<String> = MutableLiveData()
    val description: MutableLiveData<String> = MutableLiveData()
    val visibilityProgressBar: MutableLiveData<Int> = MutableLiveData()
    val visibilityLayout: MutableLiveData<Int> = MutableLiveData()
    val visibilityOtherCrime: MutableLiveData<Int> = MutableLiveData()
    private var workerThread: Thread? = null

    private var emitter: ObservableEmitter<String>? = null

    init{
        visibilityProgressBar.value = View.GONE
        visibilityLayout.value = View.VISIBLE
        visibilityOtherCrime.value = View.GONE
    }

    fun onSetCrime(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        if(pos == 7) {
            visibilityOtherCrime.value = View.VISIBLE
            crime.value = Constants.STRING_EMPTY
        } else {
            visibilityOtherCrime.value = View.GONE
            crime.value = parent.getItemAtPosition(pos).toString()
        }
        Logger.i(TAG, "set crime: ${crime.value}")
    }

    fun onSetDate(view: DatePicker, yy: Int, mm: Int, dd: Int, context: Context){
        val date = String.format(context.getString(R.string.date_template), dd, mm+1, yy)
        Logger.i(TAG, "set date: $date")
        this.date.value = date
    }

    fun onSetTime(view: TimePicker, hourOfDay: Int, minute: Int, context: Context) {
        var status = "AM"
        var hourOf12HourFormat = hourOfDay
        var minuteString = minute.toString()

        if(minute < 10) {
            minuteString = "0$minute"
        }
        if(hourOfDay == 0) {
            hourOf12HourFormat = 12
        } else if (hourOfDay > 11) {
            status = "PM"
            if(hourOfDay != 12) {
                hourOf12HourFormat = hourOfDay - 12
            }
        }

        val time = String.format(context.getString(R.string.time_template), hourOf12HourFormat, minuteString, status)
        Logger.i(TAG, "set time: $time")
        this.time.value = time
    }

    private fun isAllFieldsCompleted(): Boolean {
        if(date.value != null && time.value != null && description.value != null
            && date.value!!.isNotEmpty() && time.value!!.isNotEmpty() && description.value!!.isNotEmpty()){
            return true
        }
        return false
    }

    private fun hideKeyboard(activity: Activity) {
        // Check if no view has focus:
        val view = activity.currentFocus
        Logger.i(TAG, "start hide keyboard ${(view==null)}")
        view?.let { v ->
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
            Logger.i(TAG, "hide keyboard")
        }
    }

    fun showPickerDialog(view: View, focused: Boolean, dialog: AlertDialog) {
        if(focused && !dialog.isShowing) {
            hideKeyboard(view.context as Activity)
            dialog.show()
            Logger.i(TAG, "show picker dialog")
        }
    }

    fun onMenuClick(activity: Activity, latLng: LatLng): Boolean{
        if(isAllFieldsCompleted()) {
            hideKeyboard(activity)
            visibilityLayout.value = View.GONE
            visibilityProgressBar.value = View.VISIBLE

            val recaptchaVerification = RecaptchaVerification().apply {
                getObservable()
                    .subscribeOn(Schedulers.io())
                    .subscribe { result ->  processRecaptcha(result, latLng) }
            }

            workerThread = object: Thread() {
                override fun run() {
                    super.run()
                    recaptchaVerification.startRecaptchaTest(activity)
                }
            }
            workerThread?.start()
        } else {
            Toast.makeText(activity, activity.applicationContext.getString(R.string.please_make_sure_you_have_input_on_all_fields),
                Toast.LENGTH_LONG).show()
        }
        Logger.i(TAG, "submit!")
        return true
    }

    private fun processRecaptcha(result: Boolean, latLng: LatLng) {
        if(result) {
            val report = Reports(latLng.latitude,
                latLng.longitude, crime.value.toString(),
                description.value.toString(), time.value.toString(),
                date.value.toString())

            val firebaseStorage = FirebaseStorage().apply {
                getSingleObservable()
                    .subscribeOn(Schedulers.io())
                    .subscribe{result -> processAddReport(result)}
            }

            workerThread = object: Thread() {
                override fun run() {
                    super.run()
                    firebaseStorage.addReportToFireStore(report)
                }
            }
            workerThread?.start()

        } else {
          //TODO error handling
        }
    }

    fun getObservable(): Observable<String> {
        return Observable.create<String>{emitter -> this.emitter = emitter }
    }

    private fun processAddReport(result: Boolean) {
        if(result) {
            emitter?.onComplete()
        } else {
            //TODO error handling
        }
    }

}
