package com.example.locationreminderapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView


class ViewReminder : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var editReminderActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var header: TextView
    private lateinit var description: TextView
    private lateinit var locationName: TextView
    private lateinit var currentReminder: Reminder
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_reminder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewReminder)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        header = findViewById(R.id.heading)
        description = findViewById(R.id.description)
        locationName = findViewById(R.id.location_name)
        mapView = findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        loadReminder(intent.getStringExtra("title")!!)

        findViewById<ImageButton>(R.id.back_button).setOnClickListener{
            finish()
        }

        editReminderActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 2){
                loadReminder(intent.getStringExtra("title")!!)
            }
        }

        findViewById<TextView>(R.id.edit_button).setOnClickListener{
            val intent = Intent(this, NewReminder::class.java)
            intent.putExtra("title", currentReminder.getTitle())
            editReminderActivityLauncher.launch(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val location = LatLng(currentReminder.getLatLng().first, currentReminder.getLatLng().second)
        googleMap.addMarker(MarkerOptions().position(location).title(currentReminder.getTitle()))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    private fun loadReminder(title: String){
        currentReminder = Reminder.getReminder(this, title)!!

        header.text = currentReminder.getTitle()
        description.text = currentReminder.getDescription()
        locationName.text = currentReminder.getLocationName()
    }
}