package com.example.locationreminderapp
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Reminder(
    private var title: String,
    private var description: String,
    private var latLng: Pair<Double, Double>,
    private var locName: String,
    private var active: Boolean
) {

    companion object {
        private const val PREFERENCES_NAME = "Reminders"
        private const val REMINDERS_KEY = "reminders"

        fun copy(toCopy: Reminder): Reminder{
            return Reminder(toCopy.title,toCopy.description,toCopy.latLng,toCopy.locName,toCopy.active)
        }

        fun saveReminder(context: Context, newReminder: Reminder) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            val gson = Gson()

            // Get existing reminders
            val existingRemindersJson = sharedPreferences.getString(REMINDERS_KEY, "[]")
            val type = object : TypeToken<List<Reminder>>() {}.type
            val existingReminders: MutableList<Reminder> = gson.fromJson(existingRemindersJson, type) ?: mutableListOf()

            // Add the new reminder to the existing reminders
            existingReminders.add(newReminder)

            // Save the updated list back to SharedPreferences
            val updatedRemindersJson = gson.toJson(existingReminders)
            editor.putString(REMINDERS_KEY, updatedRemindersJson)
            editor.apply()
        }


        fun getReminders(context: Context): List<Reminder> {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            val gson = Gson()

            // Get existing reminders
            val existingRemindersJson = sharedPreferences.getString(REMINDERS_KEY, "[]")
            val type = object : TypeToken<List<Reminder>>() {}.type
            return gson.fromJson(existingRemindersJson, type) ?: listOf()
        }

        fun checkUniqueTitle(context: Context, title: String): Boolean {
            val reminders = getReminders(context)
            return reminders.none { it.title == title }
        }

        fun getReminder(context: Context, title: String): Reminder? {
            val reminders = getReminders(context)
            return reminders.find { it.title == title }
        }
        fun editReminder(context: Context, prevTitle: String, newReminder: Reminder): Boolean {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            val gson = Gson()

            // Get existing reminders
            val existingRemindersJson = sharedPreferences.getString(REMINDERS_KEY, "[]")
            val type = object : TypeToken<List<Reminder>>() {}.type
            val existingReminders: MutableList<Reminder> = gson.fromJson(existingRemindersJson, type) ?: mutableListOf()

            // Find the reminder with the previous title and update it
            val reminderIndex = existingReminders.indexOfFirst { it.title == prevTitle }
            if (reminderIndex != -1) {
                existingReminders[reminderIndex] = newReminder

                // Save the updated list back to SharedPreferences
                val updatedRemindersJson = gson.toJson(existingReminders)
                editor.putString(REMINDERS_KEY, updatedRemindersJson)
                editor.apply()
                return true
            }
            return false
        }
    }

    // Getter functions
    fun getTitle(): String {
        return title
    }

    fun getDescription(): String {
        return description
    }

    fun getLatLng(): Pair<Double, Double> {
        return latLng
    }

    fun getLocationName(): String {
        return locName
    }

    fun isActive(): Boolean {
        return active
    }

    // Setter functions
    fun setTitle(title: String) {
        this.title = title
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun setLatLng(latLng: Pair<Double, Double>) {
        this.latLng = latLng
    }

    fun setLocName(locName: String) {
        this.locName = locName
    }

    fun setActive(active: Boolean) {
        this.active = active
    }
}
