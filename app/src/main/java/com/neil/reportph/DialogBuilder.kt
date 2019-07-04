package com.neil.reportph

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import java.util.*

class DialogBuilder {
    private val calendar = Calendar.getInstance()
    private val yy = calendar.get(Calendar.YEAR)
    private val mm = calendar.get(Calendar.MONTH)
    private val dd = calendar.get(Calendar.DAY_OF_MONTH)
    private val hour = calendar.get(Calendar.HOUR_OF_DAY)
    private val minute = calendar.get(Calendar.MINUTE)

    private lateinit var datePickerDialog: DatePickerDialog
    private lateinit var timePickerDialog: TimePickerDialog

    fun getDatePickerDialog(view: View, listener: DatePickerDialog.OnDateSetListener):  DatePickerDialog{
        if(!::datePickerDialog.isInitialized) {
            datePickerDialog = DatePickerDialog(
                view.context,
                R.style.Base_Theme_MaterialComponents_Light_Dialog, listener, yy, mm, dd
            )
        }
        return datePickerDialog
    }

    fun getTimePickerDialog(view: View, listener: TimePickerDialog.OnTimeSetListener): TimePickerDialog {
        if(!::timePickerDialog.isInitialized) {
            timePickerDialog =  TimePickerDialog(view.context,
                R.style.Base_Theme_MaterialComponents_Light_Dialog, listener, hour, minute, false)
        }
        return timePickerDialog
    }
}