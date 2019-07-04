package com.neil.reportph.viewModels

import android.Manifest
import android.app.Activity
import androidx.lifecycle.ViewModel
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.neil.reportph.Constants
import com.neil.reportph.Logger
import com.neil.reportph.R
import com.neil.reportph.firebase.FirebaseStorage
import com.neil.reportph.models.Reports
import com.neil.reportph.activities.ReportCrimeActivity
import io.reactivex.schedulers.Schedulers

class MapsViewModel: ViewModel() {
    private val TAG = "MapsViewModel"
    val MIN_ZOOM = 15f
    val TEMP_PLACE_TEST = LatLng(14.84, 120.28)
    val BUFFER = 0.4
    val REQUEST_FINE_LOCATION = 0
    val visibilityReport: MutableLiveData<Int> = MutableLiveData()
    val visibilityCancel: MutableLiveData<Int> = MutableLiveData()
    var map: GoogleMap? = null
    private var workerThread: Thread? = null

    init {
        visibilityCancel.value = View.GONE
        visibilityReport.value = View.VISIBLE
    }

    fun initMap(googleMap: GoogleMap, activity: Activity) {
        map = googleMap
        getCurrentLocation(activity)
        onCameraIdle(true)
    }

    fun addMarker(markerOptions: MarkerOptions): Marker? {
        return map?.addMarker(markerOptions)
    }

    fun moveCamera(latLng: LatLng?) {
        map?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    fun animateCamera(cameraUpdate: CameraUpdate, latLng: LatLng?) {
        map?.animateCamera(cameraUpdate, object: GoogleMap.CancelableCallback{
            override fun onFinish() {
                moveCamera(latLng)
            }
            override fun onCancel() {

            }
        })
    }

    fun onCameraIdle(isFromInit: Boolean) {
        val zoom = map?.cameraPosition?.zoom ?: -1f
        if(isFromInit || zoom >= MIN_ZOOM) {
            Logger.d(TAG, "Map moving stopped: " + map?.cameraPosition)
            val latLng = map?.cameraPosition?.target
            //needs better handling of null values
            val latitude = latLng?.latitude ?: 0.0
            val longtitude = latLng?.longitude ?: 0.0

            val firebaseStorage = FirebaseStorage().apply {
                getObservable()
                    .subscribeOn(Schedulers.io())
                    .subscribe{nextReport -> onNextReport(nextReport)}
            }

            workerThread = object: Thread() {
                override fun run() {
                    super.run()
                    firebaseStorage.searchLatLngRange(latitude - BUFFER, latitude + BUFFER,
                        longtitude - BUFFER, longtitude + BUFFER)
                }
            }
            workerThread?.start()

        }
    }

    fun setOnMapClickListener(view: View, isReport: Boolean) {

        if(isReport && visibilityReport.value != View.GONE) {
            switchVisibilityReportButton(isReport)
            Snackbar.make(view, view.context.getString(R.string.click_on_the_map_where_the_incident_happened),
                Snackbar.LENGTH_LONG).show()
        } else if(!isReport && visibilityReport.value != View.VISIBLE) {
            switchVisibilityReportButton(isReport)
        }
    }

    fun switchVisibilityReportButton(isReport: Boolean) {
        visibilityReport.value = if (isReport) View.GONE else View.VISIBLE
        visibilityCancel.value = if (isReport) View.VISIBLE else View.GONE
    }

    private fun onNextReport(report: Reports) {
        val latLng = LatLng(report.latitude, report.longtitude)
        addMarker(MarkerOptions()
            .position(latLng)
            .title(report.crime)
            .snippet(report.date))
    }

    fun getCurrentLocation(activity: Activity) {
        if(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION)
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                Logger.d(TAG, "Get current location ${location?.latitude} ${location?.longitude}")
                //TODO add dot in place of current user
                animateCamera(CameraUpdateFactory.zoomTo(MIN_ZOOM), LatLng(location?.latitude ?: 2.0, location?.longitude ?: 2.0))
            }
        }
    }

    fun onMapClick(p0: LatLng, activity: Activity) {
        map?.setOnMapClickListener(null)
        showReportForm(p0, activity)
    }

    fun showReportForm(latLng: LatLng, activity: Activity) {
        Logger.d(TAG, "start activity")
        val intent = Intent(activity, ReportCrimeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Constants.LAT_LNG, latLng)
        }
        activity.startActivity(intent)
    }
}