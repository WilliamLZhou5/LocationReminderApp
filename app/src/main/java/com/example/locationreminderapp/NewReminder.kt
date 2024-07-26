package com.example.locationreminderapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// for making new reminders and editing old reminders
class NewReminder : AppCompatActivity() {

    private lateinit var mapActivityResultLauncher: ActivityResultLauncher<Intent>
    private var locName: String? = null
    private var locLat: Double? = null
    private var locLng: Double? = null
    private lateinit var currentReminder: Reminder
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var selectLocationText: TextView
    private var isEdit: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_reminder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.newReminder)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        titleInput = findViewById(R.id.title_input)
        descriptionInput = findViewById(R.id.description_input)
        selectLocationText = findViewById(R.id.select_location_text)

        if (intent.hasExtra("title")){
            isEdit = true
            val curReminder = Reminder.getReminder(this, intent.getStringExtra("title")!!)
            if (curReminder != null) {
                titleInput.setText(curReminder.getTitle())
                descriptionInput.setText(curReminder.getDescription())
                selectLocationText.text = curReminder.getLocationName()
                locName = curReminder.getLocationName()
                locLat = curReminder.getLatLng().first
                locLng = curReminder.getLatLng().second
            }
            else{
                Toast.makeText(this, "Reminder not found", Toast.LENGTH_SHORT).show()
                finish()
            }

        }

        mapActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                locName = data?.getStringExtra("locName")
                locLat = data?.getDoubleExtra("lat", 200.0)
                locLng = data?.getDoubleExtra("lng", 200.0)
            }
        }

        findViewById<Button>(R.id.select_location_button).setOnClickListener{
            val intent = Intent(this, Map::class.java)
            if (isEdit){
                intent.putExtra("lat", locLat)
                intent.putExtra("lng", locLng)
                intent.putExtra("locName", locName)
                mapActivityResultLauncher.launch(intent)
            } else{
                mapActivityResultLauncher.launch(intent)
            }
        }

        findViewById<ImageButton>(R.id.back_button).setOnClickListener{
            finish()
        }

        findViewById<Button>(R.id.submit_reminder_button).setOnClickListener{
            if (locLat == null || locLng == null){
                Toast.makeText(this@NewReminder, "No Location Selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Reminder.checkUniqueTitle(this,titleInput.text.toString())){
                Toast.makeText(this@NewReminder, "Please choose unique title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            currentReminder = Reminder(
                titleInput.text.toString(),
                descriptionInput.text.toString(),
                Pair(locLat!!,locLng!!),
                selectLocationText.text.toString(),
                true
            )

            if (isEdit){
                Reminder.editReminder(this, intent.getStringExtra("title")!!, currentReminder)
                setResult(2)
            } else {
                Reminder.saveReminder(this, currentReminder)
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
    }
}