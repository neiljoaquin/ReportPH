package com.neil.reportph.activities

import androidx.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.neil.reportph.Logger
import com.neil.reportph.R
import com.neil.reportph.viewModels.MapsViewModel
import com.neil.reportph.viewModels.MapsViewModel.Companion.REQUEST_FINE_LOCATION
import com.neil.reportph.viewModels.MapsViewModel.Companion.MIN_ZOOM
import java.util.*


class MapsActivity : AppCompatActivity() {
    private val TAG = "MapsActivity"
    private var test_position = LatLng(12.8, 122.77)
    private lateinit var viewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate")

        viewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)

        setContentView(R.layout.activity_maps)

        displayAutoCorrectUI()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync{googleMap ->  onMapReady(googleMap)}
    }

    private fun onMapReady(googleMap: GoogleMap) {
        viewModel.initMap(googleMap, this)
        viewModel.map?.setOnMarkerClickListener{p0 -> onMarkerClickListener(p0)}
        viewModel.map?.setOnInfoWindowClickListener{p0 -> onInfoWindowClickListener(p0)}
        viewModel.map?.setOnCameraIdleListener { viewModel.onCameraIdle(false) }
    }

    private fun onMarkerClickListener(p0: Marker?): Boolean {
        Logger.d(TAG, "pressed "+p0?.title)
        return false
    }

    private fun onInfoWindowClickListener(p0: Marker?) {
        Logger.d(TAG, "windowClicked "+p0?.title)
    }

    fun setMapOnClickListener(v: View) {
        viewModel.map?.setOnMapClickListener{p0 -> onMapClick(p0)}
    }

    fun getCurrentLocation(v: View){
        viewModel.getCurrentLocation(this)
    }

    private fun onMapClick(p0: LatLng) {
        viewModel.onMapClick(p0, this)
    }


    fun displayAutoCorrectUI() {
        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        // Create a new Places client instance.
        val placesClient = Places.createClient(this)

        var autocompleteFragment: AutocompleteSupportFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                viewModel.animateCamera(CameraUpdateFactory.zoomTo(MIN_ZOOM), place.latLng)
                Logger.i(TAG, "Place: " + place.name + ", " + place.id+", "+place.latLng)
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Logger.i(TAG, "An error occurred: $status")
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                }
                return
            }
        }

    }
}