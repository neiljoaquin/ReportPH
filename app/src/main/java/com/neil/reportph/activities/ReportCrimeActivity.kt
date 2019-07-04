package com.neil.reportph.activities


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.android.gms.maps.model.LatLng
import com.neil.reportph.Constants
import com.neil.reportph.DialogBuilder
import com.neil.reportph.Logger
import com.neil.reportph.R
import com.neil.reportph.databinding.ReportCrimeActivityBinding
import com.neil.reportph.viewModels.ReportsViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.report_crime_activity.*


class ReportCrimeActivity : AppCompatActivity(),TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {
    private val TAG = "ReportCrimeActivity"
    private lateinit var latLng: LatLng
    private lateinit var viewModel: ReportsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.d(TAG, "onCreate")

        viewModel = ViewModelProviders.of(this).get(ReportsViewModel::class.java)

        val binding: ReportCrimeActivityBinding = DataBindingUtil.setContentView(this, R.layout.report_crime_activity)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.dialogBuilder = DialogBuilder()
        binding.activity = this

        latLng = intent.getParcelableExtra(Constants.LAT_LNG)

        //add listener in check in toolbar
        toolbar.setOnMenuItemClickListener { viewModel.onMenuClick(this, latLng) }
        //add cancel dialog
        toolbar.inflateMenu(R.menu.menu_report)

        addValueToSpinners(R.id.spinner_crime, R.array.crime_array)

        //add listeners to spinner
        spinner_crime.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if(view != null) {
                    viewModel.onSetCrime(parent, view, pos, id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        viewModel.getObservable().subscribe(object: Observer<String> {
            override fun onComplete() {
                Logger.d(TAG, "Finish activity")
                finish()
            }

            override fun onError(e: Throwable) {
            }

            override fun onNext(t: String) {
            }

            override fun onSubscribe(d: Disposable) {
            }
        })
    }

    private fun addValueToSpinners(id: Int, range_array: Int) {
        val spinner: Spinner? = findViewById(id)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            range_array,
            R.layout.spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner?.adapter = adapter
        }
    }

    override fun onDateSet(view: DatePicker, yy: Int, mm: Int, dd: Int) {
        viewModel.onSetDate(view, yy, mm, dd, this)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        viewModel.onSetTime(view, hourOfDay, minute, this)
    }
}

