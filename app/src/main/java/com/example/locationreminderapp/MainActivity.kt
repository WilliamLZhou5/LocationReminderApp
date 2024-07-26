package com.example.locationreminderapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest

class MainActivity : AppCompatActivity(), ReminderAdapter.OnItemClickListener {
    private lateinit var newReminderLauncher: ActivityResultLauncher<Intent>
    private lateinit var reminders: MutableList<Reminder>
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var noRemindersTextView: TextView

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestLocationPermissions()

        noRemindersTextView = findViewById(R.id.no_reminders)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load all reminders into reminders MutableList
        reminders = Reminder.getReminders(this).toMutableList()

        // Load all reminders onto screen
        loadReminders()

        newReminderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                reminders = Reminder.getReminders(this).toMutableList()
                loadReminders()
            }
        }

        findViewById<Button>(R.id.add_reminder_button).setOnClickListener {
            val intent = Intent(this, NewReminder::class.java)
            newReminderLauncher.launch(intent)
        }
    }

    private fun loadReminders() {
        reminders = Reminder.getReminders(this).toMutableList()

        if (reminders.isEmpty()) {
            noRemindersTextView.visibility = TextView.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            noRemindersTextView.visibility = TextView.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
            reminderAdapter = ReminderAdapter(this, reminders, this, this::onToggleActive)
            recyclerView.adapter = reminderAdapter
        }
    }

    override fun onItemClick(reminder: Reminder) {
        Toast.makeText(this, "Clicked: ${reminder.getTitle()}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ViewReminder::class.java)
        intent.putExtra("title", reminder.getTitle())
        startActivity(intent)
    }

    private fun onToggleActive(reminder: Reminder, isActive: Boolean) {
        val updatedReminder = Reminder.copy(reminder)
        updatedReminder.setActive(isActive)
        Reminder.editReminder(this, reminder.getTitle(), updatedReminder)
        reminders = Reminder.getReminders(this).toMutableList()
        loadReminders()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionsLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ))
        } else {
            startLocationService()
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val notificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        } else {
            true
        }

        if (locationPermissionGranted && notificationPermissionGranted) {
            // Permissions granted
            startLocationService()
        } else {
            // Permissions denied
            // Handle the case where the user denied the permissions
            Toast.makeText(this, "This app will not work properly unless location tracking and post notifications are enabled. Please change your settings to allow location tracking and post notifications", Toast.LENGTH_LONG).show()
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        startService(intent)
    }
}
