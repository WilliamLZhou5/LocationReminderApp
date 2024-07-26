package com.example.locationreminderapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import android.Manifest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class Map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mGoogleMap: GoogleMap? = null
    private lateinit var autocompleteFragment: AutocompleteSupportFragment
    private var markedPoint: LatLng? = null
    private var markedName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Places.initialize(applicationContext, BuildConfig.API_KEY)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autoCompleteFragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                Toast.makeText(this@Map, "Some Error in Search", Toast.LENGTH_SHORT).show()
            }

            override fun onPlaceSelected(place: Place) {
                val add = place.address
                markedName = place.name
                val latLng = place.latLng!!
                val marker = addMarker(latLng)
                marker.title = markedName
                marker.snippet = add
                zoomOnMap(latLng)
            }
        })

        findViewById<Button>(R.id.done_button).setOnClickListener {
            val resultIntent = Intent()
            if (markedPoint == null){
                Toast.makeText(this, "Please select a location before clicking done", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            resultIntent.putExtra("lat", markedPoint!!.latitude)
            resultIntent.putExtra("lng", markedPoint!!.longitude)
            resultIntent.putExtra("locName", markedName)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun zoomOnMap(latLng: LatLng) {
        val newLatLngZoom =
            CameraUpdateFactory.newLatLngZoom(latLng, 12f) //2nd parameter is    is zoom level
        mGoogleMap?.animateCamera(newLatLngZoom)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mGoogleMap = googleMap

        mGoogleMap?.setOnMapClickListener{
            mGoogleMap?.clear()
            addMarker(it)
            markedPoint = it
        }

        getCurrentLocation { location ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                mGoogleMap?.addMarker(MarkerOptions().position(userLatLng).title("You are here"))
            } ?: run {
                // Handle the case where location is not available
            }
        }
    }

    @Suppress("MissingPermission")
    private fun getCurrentLocation(callback: (android.location.Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not already granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                callback(task.result)
            } else {
                callback(null)
            }
        }
    }

    private fun addMarker(position: LatLng): Marker {
        val marker = mGoogleMap?.addMarker(MarkerOptions().position(position))
        return marker!!
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}